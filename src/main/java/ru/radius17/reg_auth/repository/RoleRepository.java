package ru.radius17.reg_auth.repository;

import ru.radius17.reg_auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
