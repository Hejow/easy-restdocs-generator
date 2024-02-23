package io.hejow.restdocs.document;

/**
 * This interface use for specify tag.
 *
 * <pre>
 * public enum CustomTag implements ApiTag {
 *  USER("user api"),
 *  ...
 *  ;
 *
 *  private final String name;
 *
 *  &#064;Override
 *  String getName() {
 *    return this.name;
 *  }
 * }
 * </pre>
 */
public interface ApiTag {
    String getName();
}
