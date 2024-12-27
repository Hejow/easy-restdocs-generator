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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

final class DocsGenerateUtil {
  private static final Function<List<ParameterDescriptorWithType>, List<String>> PARAMETER_NAME_PARSER = parameters -> parameters.stream()
    .map(ParameterDescriptorWithType::getName)
    .toList();

  private static final Function<List<FieldDescriptor>, List<String>> PATH_PARSER = fields -> fields.stream()
    .map(FieldDescriptor::getPath)
    .toList();

  private static final Predicate<String> IS_SUPPORT_TYPE = "application/json;charset=UTF-8"::contains;

  private static final String NULL_RESPONSE_BODY = "Response Body cannot be NULL unless HTTP status is 204.";
  private static final int NO_CONTENT = 204;
  private static final String BLANK = "";

  private DocsGenerateUtil() {
    throw new AssertionError("Can't be initialize!");
  }

  public static List<FieldDescriptor> requestFields(MockHttpServletRequest request, List<FieldDescriptor> customRequestFields) {
    var tree = JsonParser.readTree(request::getContentAsString);

    Stream<FieldDescriptor> requestFieldStream = tree != null ? toObjectDescriptors(tree, BLANK) : Stream.empty();

    return merge(() -> requestFieldStream, customRequestFields);
  }

  public static List<FieldDescriptor> responseFields(MockHttpServletResponse response, List<FieldDescriptor> customResponseFields) {
    if (isNotJsonOrNoContent(response)) {
      return Collections.emptyList();
    }

    var tree = Objects.requireNonNull(JsonParser.readTree(response::getContentAsString), NULL_RESPONSE_BODY);

    return merge(() -> toObjectDescriptors(tree, BLANK), customResponseFields);
  }

  private static List<FieldDescriptor> merge(Supplier<Stream<FieldDescriptor>> fields, List<FieldDescriptor> customFields) {
    var customPaths = PATH_PARSER.apply(customFields);

    return Stream.concat(
      customFields.stream(),
      fields.get().filter(it -> !customPaths.contains(it.getPath()))
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
    var customs = PARAMETER_NAME_PARSER.apply(customRequestParameters);

    var queryParamterStream = request.getParameterMap().entrySet().stream()
      .filter(entry -> !customs.contains(entry.getKey()))
      .map(entry -> parameterWithName(entry.getKey()).description(String.join(BLANK, entry.getValue())));

    return Stream.concat(customRequestParameters.stream(), queryParamterStream).toList();
  }

  public static List<ParameterDescriptorWithType> pathVariables(
    MockHttpServletRequest request,
    List<ParameterDescriptorWithType> customPathVariables
  ) {
    var customs = PARAMETER_NAME_PARSER.apply(customPathVariables);

    var pathVariableStream = ((Map<?, ?>) request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE)).entrySet().stream()
      .filter(entry -> !customs.contains(entry.getKey()))
      .map(entry -> parameterWithName(String.valueOf(entry.getKey())).description(entry.getValue()));

    return Stream.concat(customPathVariables.stream(), pathVariableStream).toList();
  }

  private static Stream<FieldDescriptor> toObjectDescriptors(JsonNode tree, String parentPath) {
    return StreamSupport.stream(spliteratorUnknownSize(tree.fields(), ORDERED), true)
      .flatMap(it -> dispatch(it, parentPath));
  }

  private static Stream<FieldDescriptor> dispatch(Map.Entry<String, JsonNode> entry, String parentPath) {
    var node = entry.getValue();
    var path = nextObjectPath(entry.getKey(), parentPath);

    return switch (node.getNodeType()) {
      case OBJECT -> toObjectDescriptors(node, path);
      case ARRAY -> toArrayDescriptors(node, path);
      default -> toFieldDescriptor(node, path);
    };
  }

  private static Stream<FieldDescriptor> toArrayDescriptors(JsonNode node, String parentPath) {
    var path = nextArrayPath(parentPath);

    return node.isEmpty()
      ? Stream.of(fieldWithPath(path).description(node.asText()).optional())
      : StreamSupport.stream(spliteratorUnknownSize(node.elements(), ORDERED), true)
      .flatMap(it -> it.isObject() ? toObjectDescriptors(it, path) : toFieldDescriptor(it, path));
  }

  private static Stream<FieldDescriptor> toFieldDescriptor(JsonNode node, String path) {
    var text = node.asText();

    var fieldDescriptor = fieldWithPath(path).description(text).type(node.getNodeType());

    return Stream.of(text.isBlank() ? fieldDescriptor.optional() : fieldDescriptor);
  }

  private static String nextArrayPath(String parentPath) {
    return "%s[]".formatted(parentPath);
  }

  private static String nextObjectPath(String currentField, String parentPath) {
    return parentPath.isBlank() ? currentField : "%s.%s".formatted(parentPath, currentField);
  }
}
