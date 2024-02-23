package io.hejow.user;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long save(String name, String email) {
        User user = new User(name, email);
        return userRepository.save(user).getId();
    }

    public List<User> loadAll() {
        return userRepository.findAll();
    }

    public User loadById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
    }
}
