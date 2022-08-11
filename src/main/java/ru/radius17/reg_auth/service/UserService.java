package ru.radius17.reg_auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.radius17.reg_auth.entity.Role;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.repository.UserRepository;

import java.util.*;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    ReloadableResourceBundleMessageSource ms;
    @Autowired
    UserRepository mainRepository;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = mainRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User absent");
        }
        return user;
    }

    public User getById(UUID id) {
        Optional<User> object = mainRepository.findById(id);
        return object.orElse(new User());
    }

    public User getEmptyObject() {
        return new User();
    }

    public User getMySelf() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            Optional<User> userFromDb = mainRepository.findById(((User) principal).getId());
            if(userFromDb.isEmpty()) throw new UsernameNotFoundException("Myself not found by id...");
            return userFromDb.orElse(new User());
        } else {
            throw new UsernameNotFoundException("User absent...");
        }
    }

    public HashSet<UUID> getSelectedRoles(List<Role> listRoles, User user){
        HashSet<UUID> selectedRoles = new HashSet<>();
        if(user!=null && user.getRoles() != null ) {
            for (Role userRole : user.getRoles()) {
                for (Role existingRole : listRoles) {
                    if (existingRole.getId().equals(userRole.getId())) {
                        selectedRoles.add(userRole.getId());
                        // System.out.println(existingRole.getId());
                    }
                }
            }
        }
        return selectedRoles;
    }

    public void saveObject(User object) throws BaseServiceException {
        if (object.getPassword().equals(object.getPasswordConfirm())) {
            object.setPassword(bCryptPasswordEncoder.encode(object.getPassword()));
        }

        try {
            this.transactionalSave(object);
        } catch (DataIntegrityViolationException e) {
            throw new BaseServiceException(e);
//        } catch (Exception e) {
//            System.out.print(e);
        }
    }

    @Transactional
    public void transactionalSave(User object) {
        mainRepository.save(object);
    }

    @Transactional
    public void deleteObject(UUID objId) {
        mainRepository.deleteById(objId);
    }

    public List<User> getAll() {
        return mainRepository.findAll(Sort.by("username"));
    }

    public Page<User> getAllFilteredAndPaginated(Specification specification, Pageable pageable) {
        return mainRepository.findAll(specification, pageable);
    }
}