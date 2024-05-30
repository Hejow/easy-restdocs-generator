# Easy Spring Rest-docs Generator
[![Maven Central](https://img.shields.io/maven-central/v/io.github.hejow/easy-restdocs-generator.svg)](https://central.sonatype.com/artifact/io.github.hejow/easy-restdocs-generator)
[![GitHub license](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/Hejow/easy-restdocs-generator/blob/main/LICENSE)

### This is a generator that suggest easier way to use rest-docs.

## Install
- `JDK 17` or higher is required
- `Spring Boot 3.X` is required

### Gradle

```groovy
testImplementation("io.github.hejow:easy-restdocs-generator:0.0.8")
```

### Maven

```xml
<dependency>
    <groupId>io.github.hejow</groupId>
    <artifactId>easy-restdocs-generator</artifactId>
    <version>0.0.8</version>
    <scope>test</scope>
</dependency>
```

## How to use
Only you have to do is **Customize tags** and **Use builder**.

### Customize tags
To specify your api, easy-restdoc use `ApiTag` to generate documents.  

```java
// example
public enum MyTag implements ApiTag {
  USER("user api");

  private final String content;  

  // ... constructor

  @Override
  public String getName() {
    return this.content;
  }
}
```

### Use builder
After test with `mockMvc` just use builder to generate as like below.

Planning to support `RestAssured`.

> ### 💡 Tips
> 
> To generate documents you MUST put `tag`, `result` on `Builder`.
> 
> If you don’t put `identifier` on `Builder`, Method name of the test you wrote will be used as `identifier`
> 
> Tests MUST run with rest-docs settings such as `@ExtendWith(RestDocumentationExtension.class)` ([see here](https://github.com/Hejow/easy-restdocs-generator/blob/f25657a5aa20f813d9814d00b661bf6e11d300dd/sample/src/test/java/com/simplerestdocs/user/UserControllerTest.java#L45))

```java
// example
@Test
void myTest() throws Exception {
  // given
  
  // when
  var result = mockMvc.perform(...);

  // then
  result.andExpectAll(
    status().isOk(),
    ...
  );

  // docs
  result.andDo(
    RestDocument.builder()
      .identifier("identifier of your API") // Can skip
      .tag(MyTag.USER) // Custom tags
      .summary("this will be name of API")
      .description("write description about your API")
      .result(result) // Test result
      .generateDocs()
  );
}
```
