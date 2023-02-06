package ru.radius17.reg_auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.radius17.reg_auth.entity.Role;
import ru.radius17.reg_auth.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    RoleRepository mainRepository;

    public Role getDefaultObject() {
        return mainRepository.findByName("ROLE_USER").orElse(new Role());
    }

    public List<Role> getAll() {
        return mainRepository.findAll(Sort.by("label"));
    }
    public Role getByName(String name) {
        Optional<Role> object = mainRepository.findByName(name);
        return object.orElse(new Role());
    }
}