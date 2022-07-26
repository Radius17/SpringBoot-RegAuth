package ru.radius17.reg_auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.repository.UserRepository;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

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

    public void saveUser(User user) throws UserServiceException {
        User mySelf = this.getMySelf();

        if (user.getId() == null) {
            // It is new user
            user.setRoles(Collections.singleton(roleService.getDefaultRole()));
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        } else {
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
}