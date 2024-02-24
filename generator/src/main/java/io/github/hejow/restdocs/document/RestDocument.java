package io.github.hejow.restdocs.document;

import io.github.hejow.restdocs.generator.RestDocsGenerator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.ResultActions;

import static java.util.Objects.requireNonNull;

/**
 * Builders class to prevent human error such as typos.
 * When class safely created can use `generateDocs()` to create `RestDocumentationResultHandler`
 * @see RestDocsGenerator
 */
public class RestDocument {
	private final String identifier;
	private final String tag;
	private final String summary;
	private final String description;
	private final MockHttpServletRequest request;
	private final MockHttpServletResponse response;

	private RestDocument(
		String identifier,
		String tag,
		String summary,
		String description,
		ResultActions result
	) {
		requireNonBlank(identifier);
		requireNonNull(tag, "Tag cannot be null");
		requireNonNull(result, "ResultActions cannot be null");
		this.identifier = identifier;
		this.tag = tag;
		this.summary = summary;
		this.description = description;
		this.request = result.andReturn().getRequest();
		this.response = result.andReturn().getResponse();
	}

	public static RestDocumentBuilder builder() {
		return new RestDocumentBuilder();
	}

	public RestDocumentationResultHandler generateDocs() {
		return RestDocsGenerator.generate(identifier, tag, summary, description, request, response);
	}

	private void requireNonBlank(String identifier) {
		if (identifier == null || identifier.isBlank()) {
			throw new IllegalArgumentException("ID cannot be empty");
		}
	}

	public static class RestDocumentBuilder {
		private String identifier;
		private ApiTag apiTag;
		private String summary;
		private String description;
		private ResultActions result;

		private RestDocumentBuilder() {
		}

		public RestDocumentBuilder identifier(String identifier) {
			this.identifier = identifier;
			return this;
		}

		public RestDocumentBuilder tag(ApiTag tag) {
			this.apiTag = tag;
			return this;
		}

		public RestDocumentBuilder summary(String summary) {
			this.summary = summary;
			return this;
		}

		public RestDocumentBuilder description(String description) {
			this.description = description;
			return this;
		}

		public RestDocumentBuilder result(ResultActions result) {
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
