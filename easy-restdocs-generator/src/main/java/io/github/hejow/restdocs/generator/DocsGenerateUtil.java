package io.github.hejow.restdocs.generator;

import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.payload.FieldDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

final class DocsGenerateUtil {
	private static final String BASIC_PATH = "";
	private static final int NO_CONTENT = 204;

	private DocsGenerateUtil() {
		throw new AssertionError("Can't be initialize!");
	}

	public static List<FieldDescriptor> requestFields(MockHttpServletRequest request) {
		var tree = JsonParser.readTree(request::getContentAsString);
		return tree != null ? createDescriptors(tree, BASIC_PATH).toList() : Collections.emptyList();
	}

	public static List<FieldDescriptor> responseFields(MockHttpServletResponse response) {
		return response.getStatus() == NO_CONTENT ? Collections.emptyList() : createResponseDescriptors(response);
	}

	public static List<ParameterDescriptorWithType> queryParameters(MockHttpServletRequest request) {
		return request.getParameterMap().entrySet().stream()
			.map(entry -> parameterWithName(entry.getKey()).description(String.join("", entry.getValue())))
			.toList();
	}

	public static List<ParameterDescriptorWithType> pathVariables(MockHttpServletRequest request) {
		return ((Map<?, ?>)request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE)).entrySet().stream()
			.map(entry -> parameterWithName(String.valueOf(entry.getKey())).description(entry.getValue()))
			.toList();
	}

	private static List<FieldDescriptor> createResponseDescriptors(MockHttpServletResponse response) {
		var tree = JsonParser.readTree(response::getContentAsString);
		Objects.requireNonNull(tree, "Response Body cannot be NULL");
		return createDescriptors(tree, BASIC_PATH).toList();
	}

	private static Stream<FieldDescriptor> createDescriptors(JsonNode tree, String parentPath) {
		return StreamSupport.stream(spliteratorUnknownSize(tree.fields(), ORDERED), false)
			.flatMap(entry -> create(entry, parentPath));
	}

	private static Stream<FieldDescriptor> create(Map.Entry<String, JsonNode> entry, String parentPath) {
		var node = entry.getValue();
		var path = toNextPath(parentPath, entry.getKey());

		return switch (node.getNodeType()) {
			case OBJECT -> createDescriptors(node, path);
			case ARRAY -> toArrayDescriptors(node, path);
			default -> Stream.of(toFieldDescriptor(node, path));
		};
	}

	private static Stream<FieldDescriptor> toArrayDescriptors(JsonNode node, String path) {
		return StreamSupport.stream(spliteratorUnknownSize(node.elements(), ORDERED), false)
			.flatMap(it -> it.isObject() ? createDescriptors(it, toNextPath(path)) : Stream.of(toFieldDescriptor(it, toNextPath(path))));
	}

	private static FieldDescriptor toFieldDescriptor(JsonNode node, String path) {
		return fieldWithPath(path).description(node.asText()).type(node.getNodeType());
	}

	private static String toNextPath(String path) {
		return path.concat(".[].");
	}

	private static String toNextPath(String path, String currentField) {
		return path.isBlank() ? currentField : String.format("%s.%s", path, currentField);
	}
}
