package io.github.hejow.restdocs.generator;

import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static java.util.Objects.requireNonNull;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

/**
 * Builders class to prevent human error such as typos. <br>
 * If class safely created can call `generateDocs()` to create `RestDocumentationResultHandler`
 *
 * @see ApiTag
 */
public class Document {
  private static final String DEFAULT_IDENTIFIER = "{method_name}";

  private final String identifier;
  private final String tag;
  private final String summary;
  private final String description;
  private final MockHttpServletRequest request;
  private final MockHttpServletResponse response;

  public Document(
    String identifier,
    String tag,
    String summary,
    String description,
    MockHttpServletRequest request,
    MockHttpServletResponse response
  ) {
    requireNonNull(tag, "Tag cannot be null");
    this.identifier = identifier == null ? DEFAULT_IDENTIFIER : identifier;
    this.tag = tag;
    this.summary = summary;
    this.description = description;
    this.request = request;
    this.response = response;
  }

  public static Builder builder() {
    return new Builder();
  }

  public RestDocumentationResultHandler generate() {
    return document(
      identifier,
      preprocessRequest(prettyPrint()),
      preprocessResponse(prettyPrint()),
      resource(
        ResourceSnippetParameters.builder()
          .tag(tag)
          .summary(summary)
          .description(description)
          .requestFields(DocsGenerateUtil.requestFields(request))
          .responseFields(DocsGenerateUtil.responseFields(response))
          .queryParameters(DocsGenerateUtil.queryParameters(request))
          .pathParameters(DocsGenerateUtil.pathVariables(request))
          .build()
      )
    );
  }

  public static class Builder {
    private String identifier;
    private ApiTag apiTag;
    private String summary;
    private String description;
    private ResultActions result;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private List<ParameterDescriptorWithType> requestParameters;
    private List<ParameterDescriptorWithType> pathVariables;
    private List<FieldDescriptor> responseFields;

    private Builder() {
    }

    public Builder identifier(String identifier) {
      this.identifier = identifier;
      return this;
    }

    public Builder tag(ApiTag tag) {
      this.apiTag = tag;
      return this;
    }

    public Builder summary(String summary) {
      this.summary = summary;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder result(ResultActions result) {
      this.result = result;
      return this;
    }

    public Builder request(MockHttpServletRequest request) {
      this.request = request;
      return this;
    }

    public Builder response(MockHttpServletResponse response) {
      this.response = response;
      return this;
    }

    public Builder requestParameters(List<ParameterDescriptorWithType> requestParameters) {
      this.requestParameters = requestParameters;
      return this;
    }

    public Builder pathVariables(List<ParameterDescriptorWithType> pathVariables) {
      this.pathVariables = pathVariables;
      return this;
    }

    public Builder responseFields(List<FieldDescriptor> responseFields) {
      this.responseFields = responseFields;
      return this;
    }

    public Document build() {
      if (result == null) {
        requireNonNull(request, "Request cannot be null");
        requireNonNull(response, "Response cannot be null");

        return new Document(identifier, apiTag.getName(), summary, description, request, response);
      }

      var mvcResult = result.andReturn();

      return new Document(identifier, apiTag.getName(), summary, description, mvcResult.getRequest(), mvcResult.getResponse());
    }

    public RestDocumentationResultHandler buildAndGenerate() {
      return build().generate();
    }
  }
}
