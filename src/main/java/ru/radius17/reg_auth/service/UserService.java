package ru.radius17.reg_auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
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
            String username = ((UserDetails) principal).getUsername();
            // User user = userService.findUserById(75L); // Test
            User user = userRepository.findByUsername(username);
            if (user == null) {
                // По всей видимости сразу выкидывает
                throw new UsernameNotFoundException("User not found by name");
            }
            if (user.getId() == null) {
                // По всей видимости сразу выкидывает
                throw new UsernameNotFoundException("User not found");
            }
            // @TODO Надо оптимизировать
            Optional<User> userFromDb = userRepository.findById(user.getId());
            return userFromDb.orElse(new User());
        } else {
            return new User();
        }
    }

    public void saveUser(User user) {
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

        userRepository.save(user);
    }

    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    public Page<User> getUsersPaginated(Pageable pageable) {
        Page<User> pagedResult = userRepository.findAll(pageable);
        return pagedResult;
    }
}