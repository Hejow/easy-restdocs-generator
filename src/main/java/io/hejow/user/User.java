package io.hejow.user;

public class User {
    private static Long currentId = 0L;

    private Long id;
    private String name;
    private String email;

    public User(String name, String email) {
        this.id = ++currentId;
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
