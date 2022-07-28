package ru.radius17.reg_auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    UserRepository userRepository;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User absent");
        }
        return user;
    }

    public User getUserById(UUID id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(new User());
    }

    public User getEmptyUser() {
        return new User();
    }

    public User getMySelf() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            Optional<User> userFromDb = userRepository.findById(((User) principal).getId());
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
                        System.out.println(existingRole.getId());
                    }
                }
            }
        }
        return selectedRoles;
    }

    public void saveUser(User user) throws UserServiceException {

        if (user.getId() == null) {
            // It is new user
            user.setRoles(Collections.singleton(roleService.getDefaultRole()));
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        } else {
            User mySelf = this.getMySelf();
            if (mySelf.getId().equals(user.getId())) {
                // It is myself
                // Restrict to change roles
                user.setRoles(mySelf.getRoles());
            }
            if (user.getPassword().equals(user.getPasswordConfirm())) {
                user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            }
        }

        try {
            this.transactionalSave(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserServiceException(e);
        } catch (Exception e) {
            System.out.print(e);
        }
    }

    @Transactional
    public void transactionalSave(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    public Page<User> getUsersPaginated(Pageable pageable) {
        Page<User> pagedResult = userRepository.findAll(pageable);
        return pagedResult;
    }
    public Page<User> getUsersFilteredAndPaginated(Specification specification, Pageable pageable) {
        Page<User> pagedResult = userRepository.findAll(specification, pageable);
        return pagedResult;
    }
}