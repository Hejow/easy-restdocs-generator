package com.simplerestdocs.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/users")
  public ResponseEntity<?> save(CreateDto request) {
    Long id = userService.save(request.name, request.email);
    return ResponseEntity.created(URI.create("/users/" + id)).build();
  }

  @GetMapping("/users")
  public ResponseEntity<?> findAll() {
    return ResponseEntity.ok(userService.loadAll());
  }

  public static class CreateDto {
    private final String name;
    private final String email;

    public CreateDto(String name, String email) {
      this.name = name;
      this.email = email;
    }
  }
}
