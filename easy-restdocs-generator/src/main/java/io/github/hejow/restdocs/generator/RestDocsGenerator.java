package io.github.hejow.restdocs.generator;

import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static java.util.Collections.emptyList;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class RestDocsGenerator {
	private static final String INITIAL_PATH = "";

	private RestDocsGenerator() {
		throw new AssertionError("can't be initialize!");
	}

	public static RestDocumentationResultHandler generate(
		String identifier,
		String tag,
		String summary,
		String description,
		MockHttpServletRequest request,
		MockHttpServletResponse response
	) {
		List<FieldDescriptor> requestFields = generateRequestFields(request);
		List<FieldDescriptor> responseFields = generateResponseFields(response);

		List<ParameterDescriptorWithType> queryParameters = generateQueryParameters(request);
		List<ParameterDescriptorWithType> pathVariables = generatePathVariables(request);

		return document(
			identifier,
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			resource(ResourceSnippetParameters.builder()
				.tag(tag)
				.summary(summary)
				.description(description)
				.requestFields(requestFields)
				.responseFields(responseFields)
				.queryParameters(queryParameters)
				.pathParameters(pathVariables)
				.build()
			)
		);
	}

	private static List<FieldDescriptor> generateRequestFields(MockHttpServletRequest request) {
		JsonNode tree = JsonParser.readTree(request::getContentAsString);
		return tree != null ? generateDescriptors(tree, INITIAL_PATH) : emptyList();
	}

	private static List<FieldDescriptor> generateResponseFields(MockHttpServletResponse response) {
		return response.getStatus() == 204 ? emptyList() : generateNonNullResponseDescriptors(response);
	}

	private static List<FieldDescriptor> generateNonNullResponseDescriptors(MockHttpServletResponse response) {
		JsonNode tree = JsonParser.readTree(response::getContentAsString);
		Objects.requireNonNull(tree, "Response Body cannot be NULL");
		return generateDescriptors(tree, INITIAL_PATH);
	}

	private static List<FieldDescriptor> generateDescriptors(JsonNode tree, String parentPath) {
		return StreamSupport.stream(spliteratorUnknownSize(tree.fields(), ORDERED), false)
			.flatMap(entry -> {
				String key = entry.getKey();
				JsonNode value = entry.getValue();
				JsonNodeType type = value.getNodeType();
				String path = parentPath.isBlank() ? key : parentPath.concat(String.format(".%s", key));
				return createDescriptorByType(value, path, type);
			})
			.toList();
	}

	private static Stream<FieldDescriptor> createDescriptorByType(JsonNode value, String path, JsonNodeType type) {
		return switch (type) {
			case OBJECT -> generateDescriptors(value, path).stream();
			case ARRAY -> createArrayDescriptors(value, path);
			case BOOLEAN, NUMBER, STRING -> Stream.of(fieldWithPath(path).description(value.asText()));
			default -> throw new UnsupportedOperationException("Not Support Type. Object, Array, Boolean, Number, String are supported");
		};
	}

	private static Stream<FieldDescriptor> createArrayDescriptors(JsonNode value, String path) {
		return StreamSupport.stream(spliteratorUnknownSize(value.elements(), ORDERED), false)
			.flatMap(item -> item.isObject()
				? generateDescriptors(item, path.concat(".[].")).stream()
				: Stream.of(fieldWithPath(path.concat(".[].")).description("element of array"))
			);
	}

	private static List<ParameterDescriptorWithType> generateQueryParameters(MockHttpServletRequest request) {
		Map<String, String[]> queryParameters = request.getParameterMap();
		return queryParameters.entrySet().stream()
			.map(entry -> parameterWithName(entry.getKey()).description(String.join("", entry.getValue())))
			.toList();
	}

	private static List<ParameterDescriptorWithType> generatePathVariables(MockHttpServletRequest request) {
		Object pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		return ((Map<?, ?>)pathVariables).entrySet().stream()
			.map(entry -> parameterWithName(String.valueOf(entry.getKey())).description(entry.getValue()))
			.toList();
	}
}
