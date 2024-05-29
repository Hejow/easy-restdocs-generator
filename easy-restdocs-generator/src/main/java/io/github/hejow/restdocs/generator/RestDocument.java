package io.github.hejow.restdocs.generator;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.ResultActions;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static java.util.Objects.requireNonNull;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

/**
 * Builders class to prevent human error such as typos.
 * When class safely created can call `generateDocs()` to create `RestDocumentationResultHandler`
 */
public class RestDocument {
	private final String identifier;
	private final String tag;
	private final String summary;
	private final String description;
	private final MockHttpServletRequest request;
	private final MockHttpServletResponse response;

	private RestDocument(String identifier, String tag, String summary, String description, ResultActions result) {
		requireNonBlankIdentifier(identifier);
		requireNonNull(tag, "Tag cannot be null");
		requireNonNull(result, "ResultActions cannot be null");
		this.identifier = identifier;
		this.tag = tag;
		this.summary = summary;
		this.description = description;
		this.request = result.andReturn().getRequest();
		this.response = result.andReturn().getResponse();
	}

	public static Builder builder() {
		return new Builder();
	}

	public RestDocumentationResultHandler generateDocs() {
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

	private void requireNonBlankIdentifier(String identifier) {
		if (identifier == null || identifier.isBlank()) {
			throw new IllegalArgumentException("Identifier cannot be empty");
		}
	}

	public static class Builder {
		private String identifier;
		private ApiTag apiTag;
		private String summary;
		private String description;
		private ResultActions result;

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

		public RestDocument build() {
			return new RestDocument(identifier, apiTag.getName(), summary, description, result);
		}

		public RestDocumentationResultHandler generateDocs() {
			return build().generateDocs();
		}
	}
}
