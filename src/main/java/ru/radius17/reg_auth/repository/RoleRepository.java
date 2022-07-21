package ru.radius17.reg_auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.radius17.reg_auth.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
