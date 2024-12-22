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
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

final class DocsGenerateUtil {
  private static final Predicate<String> IS_SUPPORT_TYPE = "application/json;charset=UTF-8"::contains;
  private static final String NULL_RESPONSE_BODY = "Response Body cannot be NULL unless HTTP status is 204.";

  private static final int NO_CONTENT = 204;
  private static final String BLANK = "";

  private DocsGenerateUtil() {
    throw new AssertionError("Can't be initialize!");
  }

  public static List<FieldDescriptor> requestFields(MockHttpServletRequest request, List<FieldDescriptor> customRequestFields) {
    var tree = JsonParser.readTree(request::getContentAsString);

    Stream<FieldDescriptor> requestFieldStream = tree != null ? createDescriptors(tree, BLANK) : Stream.empty();

    var customs = customRequestFields.stream()
      .map(FieldDescriptor::getPath)
      .toList();

    return Stream.concat(
      customRequestFields.stream(),
      requestFieldStream.filter(it -> !customs.contains(it.getPath()))
    ).toList();
  }

  public static List<FieldDescriptor> responseFields(MockHttpServletResponse response, List<FieldDescriptor> customResponseFields) {
    if (isNotJsonOrNoContent(response)) {
      return Collections.emptyList();
    }

    var tree = Objects.requireNonNull(JsonParser.readTree(response::getContentAsString), NULL_RESPONSE_BODY);

    var customs = customResponseFields.stream()
      .map(FieldDescriptor::getPath)
      .toList();

    return Stream.concat(
      customResponseFields.stream(),
      createDescriptors(tree, BLANK).filter(it -> !customs.contains(it.getPath()))
    ).toList();
  }

  private static boolean isNotJsonOrNoContent(MockHttpServletResponse response) {
    var contentType = Objects.requireNonNullElse(response.getContentType(), BLANK);
    return response.getStatus() == NO_CONTENT || !IS_SUPPORT_TYPE.test(contentType);
  }

  public static List<ParameterDescriptorWithType> queryParameters(
    MockHttpServletRequest request,
    List<ParameterDescriptorWithType> customRequestParameters
  ) {
    var customs = customRequestParameters.stream()
      .map(ParameterDescriptorWithType::getName)
      .toList();

    var queryParamterStream = request.getParameterMap().entrySet().stream()
      .filter(entry -> !customs.contains(entry.getKey()))
      .map(entry -> parameterWithName(entry.getKey()).description(String.join(BLANK, entry.getValue())));

    return Stream.concat(customRequestParameters.stream(), queryParamterStream).toList();
  }

  public static List<ParameterDescriptorWithType> pathVariables(
    MockHttpServletRequest request,
    List<ParameterDescriptorWithType> customPathVariables
  ) {
    var customs = customPathVariables.stream()
      .map(ParameterDescriptorWithType::getName)
      .toList();

    var pathVariableStream = ((Map<?, ?>) request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE)).entrySet().stream()
      .filter(entry -> !customs.contains(entry.getKey()))
      .map(entry -> parameterWithName(String.valueOf(entry.getKey())).description(entry.getValue()));

    return Stream.concat(customPathVariables.stream(), pathVariableStream).toList();
  }

  private static Stream<FieldDescriptor> createDescriptors(JsonNode tree, String parentPath) {
    return StreamSupport.stream(spliteratorUnknownSize(tree.fields(), ORDERED), false)
      .flatMap(entry -> create(entry, parentPath));
  }

  private static Stream<FieldDescriptor> create(Map.Entry<String, JsonNode> entry, String parentPath) {
    var node = entry.getValue();
    var path = nextPath(parentPath, entry.getKey());

    return switch (node.getNodeType()) {
      case OBJECT -> createDescriptors(node, path);
      case ARRAY -> toArrayDescriptors(node, path);
      default -> Stream.of(toFieldDescriptor(node, path));
    };
  }

  private static Stream<FieldDescriptor> toArrayDescriptors(JsonNode node, String path) {
    return StreamSupport.stream(spliteratorUnknownSize(node.elements(), ORDERED), false)
      .flatMap(it -> it.isObject() ? createDescriptors(it, nextPath(path)) : Stream.of(toFieldDescriptor(it, nextPath(path))));
  }

  private static FieldDescriptor toFieldDescriptor(JsonNode node, String path) {
    return fieldWithPath(path).description(node.asText()).type(node.getNodeType());
  }

  private static String nextPath(String path) {
    return path.concat("[].");
  }

  private static String nextPath(String path, String currentField) {
    return path.isBlank() ? currentField : path.concat(currentField);
  }
}
