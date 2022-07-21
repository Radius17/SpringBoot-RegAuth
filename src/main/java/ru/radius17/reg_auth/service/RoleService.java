package ru.radius17.reg_auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.radius17.reg_auth.entity.Role;
import ru.radius17.reg_auth.repository.RoleRepository;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    RoleRepository roleRepository;

    public Role getDefaultRole() {
        return roleRepository.findById(1L).orElse(new Role());
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}