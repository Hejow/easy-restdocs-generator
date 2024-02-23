package io.hejow.restdocs.document;

public enum Tag implements ApiTag{
    USER("user api"),
    ;

    private final String name;

    Tag(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
