package ru.radius17.reg_auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.radius17.reg_auth.entity.Role;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);
}
