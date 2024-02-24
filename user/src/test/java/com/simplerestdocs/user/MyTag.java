package com.simplerestdocs.user;

import io.github.hejow.restdocs.document.ApiTag;

public enum MyTag implements ApiTag {
  USER("user api"),
  ;

  private final String content;

  MyTag(String content) {
    this.content = content;
  }

  @Override
  public String getName() {
    return this.content;
  }
}
