package io.github.hejow.restdocs.generator;

/**
 * This interface for indicate tag information to swagger.
 *
 * <br>
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
 *    return this.name; // this.name() also possible
 *  }
 * }
 * </pre>
 * @see RestDocument
 */
public interface ApiTag {
    String getName();
}
