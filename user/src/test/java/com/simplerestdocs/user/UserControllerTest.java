package com.simplerestdocs.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hejow.restdocs.document.RestDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setup(
    WebApplicationContext webApplicationContext,
    RestDocumentationContextProvider restDocumentation
  ) {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
      .alwaysDo(print())
      .apply(documentationConfiguration(restDocumentation))
      .addFilter(new CharacterEncodingFilter("UTF-8", true))
      .build();
  }

  @Test
  @Order(2)
  void saveTest() throws Exception {
    // given
    UserController.CreateDto request = new UserController.CreateDto("john", "no-reply@gmail.com");

    // when
    ResultActions result = mockMvc.perform(post("/users")
      .contentType(MediaType.APPLICATION_JSON)
      .content(objectMapper.writeValueAsString(request)));

    // then
    result.andExpect(status().isCreated());

    // docs
    result.andDo(
      RestDocument.builder()
        .identifier("user-create-success")
        .tag(MyTag.USER)
        .summary("user-create-api")
        .description("save new user")
        .result(result)
        .generateDocs()
    );
  }

  @Test
  @Order(1)
  void loadAllTest() throws Exception {
    // given
    User john = new User("john", "no-reply@google.com");
    User mike = new User("mike", "no-reply@apple.com");

    userRepository.saveAll(List.of(john, mike));

    // when
    ResultActions result = mockMvc.perform(get("/users")
      .contentType(MediaType.APPLICATION_JSON));

    // then
    result.andExpectAll(
      status().isOk(),
      jsonPath("$[0].id").value(john.getId()),
      jsonPath("$[0].name").value(john.getName()),
      jsonPath("$[0].email").value(john.getEmail())
    );

    // docs
    result.andDo(
      RestDocument.builder()
        .identifier("load-all-users-success")
        .tag(MyTag.USER)
        .summary("get-all-users-api")
        .description("load all saved users")
        .result(result)
        .generateDocs()
    );
  }
}
