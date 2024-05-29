package io.github.hejow.restdocs.generator;

import java.io.UnsupportedEncodingException;

@FunctionalInterface
interface ContentSupplier {
  String get() throws UnsupportedEncodingException;
}
