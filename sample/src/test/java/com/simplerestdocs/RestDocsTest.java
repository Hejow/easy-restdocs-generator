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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
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
    var request = new TestDto(
      "first",
      "second",
      List.of(
        new TestDto2(
          "first",
          List.of(
            new TestDto3(
              "first",
              List.of(),
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

  @Test
  void sample2() throws Exception {
    // given

    // when
    var result = mockMvc.perform(get("/samples2"));

    // then
    result.andExpect(status().isOk());

    // docs
    result.andDo(
      Document.builder()
        .tag(() -> "Sample")
        .summary("Get Sample")
        .result(result)
        .buildAndGenerate()
    );
  }

  public static class TestDto {
    private final String first;
    private final String second;
    private final List<TestDto2> third;

    public TestDto(String first, String second, List<TestDto2> third) {
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

    public List<TestDto2> getThird() {
      return third;
    }
  }

  public static class TestDto2 {
    private final String first;
    private final List<TestDto3> second;

    public TestDto2(String first, List<TestDto3> second) {
      this.first = first;
      this.second = second;
    }

    public String getFirst() {
      return first;
    }

    public List<TestDto3> getSecond() {
      return second;
    }
  }

  public static class TestDto3 {
    private final String first;
    private final List<String> second;
    private final List<String> third;

    public TestDto3(String first, List<String> second, List<String> third) {
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
    public void sample(@RequestBody TestDto request) {
    }

    @GetMapping("/samples2")
    @ResponseStatus(HttpStatus.OK)
    public Response<?> sample2() {
      var response = new TestDto(
        "first",
        "second",
        List.of(
          new TestDto2(
            "first",
            List.of(
              new TestDto3(
                "first",
                List.of(),
                List.of("third")
              )
            )
          )
        )
      );

      return new Response<>(response);
    }
  }

  public static class Response<T> {
    private final T data;

    public Response(T data) {
      this.data = data;
    }

    public T getData() {
      return data;
    }
  }
}
