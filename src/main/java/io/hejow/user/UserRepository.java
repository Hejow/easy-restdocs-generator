package io.hejow.user;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {
    private static final Map<Long, User> storage = new ConcurrentHashMap<>();

    public User save(User user) {
        storage.put(user.getId(), user);
        return user;
    }

    public void saveAll(Collection<User> users) {
        for (User user : users) {
            storage.put(user.getId(), user);
        }
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<User> findAll() {
        return storage.values().stream()
                .toList();
    }
}
