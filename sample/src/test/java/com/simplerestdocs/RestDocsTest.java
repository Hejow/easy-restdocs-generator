package com.simplerestdocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hejow.restdocs.generator.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestDocsTest {
  @Autowired
  private RestDocumentationContextProvider restDocumentation;
  @Autowired
  private ObjectMapper objectMapper;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders
      .standaloneSetup(new TestController())
      .alwaysDo(print())
      .apply(documentationConfiguration(restDocumentation))
      .addFilter(new CharacterEncodingFilter(UTF_8.name(), true))
      .build();
  }

  @Test
  void sample1() throws Exception {
    // given
    var request = new TestRequest(
      "first",
      "second",
      List.of(
        new TestReqeust2(
          "first",
          List.of(
            new TestReqeust3(
              "first",
              List.of("second"),
              List.of("third")
            )
          )
        )
      )
    );

    // when
    var result = mockMvc.perform(post("/samples")
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(request)));

    // then
    result.andExpect(status().isOk());

    // docs
    result.andDo(
      Document.builder()
        .tag(() -> "Sample")
        .summary("Post Sample")
        .result(result)
        .buildAndGenerate()
    );
  }

  public static class TestRequest {
    private final String first;
    private final String second;
    private final List<TestReqeust2> third;

    public TestRequest(String first, String second, List<TestReqeust2> third) {
      this.first = first;
      this.second = second;
      this.third = third;
    }

    public String getFirst() {
      return first;
    }

    public String getSecond() {
      return second;
    }

    public List<TestReqeust2> getThird() {
      return third;
    }
  }

  public static class TestReqeust2 {
    private final String first;
    private final List<TestReqeust3> second;

    public TestReqeust2(String first, List<TestReqeust3> second) {
      this.first = first;
      this.second = second;
    }

    public String getFirst() {
      return first;
    }

    public List<TestReqeust3> getSecond() {
      return second;
    }
  }

  public static class TestReqeust3 {
    private final String first;
    private final List<String> second;
    private final List<String> third;

    public TestReqeust3(String first, List<String> second, List<String> third) {
      this.first = first;
      this.second = second;
      this.third = third;
    }

    public String getFirst() {
      return first;
    }

    public List<String> getSecond() {
      return second;
    }

    public List<String> getThird() {
      return third;
    }
  }

  @RestController
  public static class TestController {
    @SuppressWarnings("unused")
    @PostMapping("/samples")
    @ResponseStatus(HttpStatus.OK)
    public void test(@RequestBody TestRequest request) {
    }
  }
}
