package ru.radius17.reg_auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.radius17.reg_auth.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
