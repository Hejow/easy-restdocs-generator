# Easy Spring Rest-docs Generator
[![Maven central](https://img.shields.io/badge/maven_central-%200.0.3-green.svg)](https://central.sonatype.com/artifact/io.github.hejow/easy-restdocs-generator)
[![GitHub license](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/Hejow/easy-restdocs-generator/tree/main)

This is a generator that suggest easier way to use rest-docs.

## Install
> JDK 17 or higher is required

#### Gradle

```groovy
testImplementation("io.github.hejow:easy-restdocs-generator:0.0.3")
```

#### Maven

```xml
<dependency>
    <groupId>io.github.hejow</groupId>
    <artifactId>easy-restdocs-generator</artifactId>
    <version>0.0.3</version>
    <scope>test</scope>
</dependency>
```

## How to use
Only you have to do is **customize tags** and **use builder**.

### Customize tags
To specify your api, easy-restdoc use `ApiTag` to generate documents.  

```java
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

> 💡 Tips
> 
> To generate documents you MUST put `identifier`, `tag`, `result` on builder. <br> 
> Tests MUST run with rest-docs settings such as `@ExtendWith(RestDocumentationExtension.class)` ([see here](https://github.com/Hejow/easy-restdocs-generator/blob/f25657a5aa20f813d9814d00b661bf6e11d300dd/sample/src/test/java/com/simplerestdocs/user/UserControllerTest.java#L45))

```java
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
      .identifier("identifier of your API")
      .tag(MyTag.USER) // put your custom tags
      .summary("this will be name of API")
      .description("write description about your API")
      .result(result) // put result action here
      .generateDocs()
  );
}
```
