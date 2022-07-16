package ru.radius17.reg_auth.service;

import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.radius17.reg_auth.entity.Role;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.repository.RoleRepository;
import ru.radius17.reg_auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User absent");
        }
        return user;
    }
    public User getMySelf() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails)principal).getUsername();
            // User user = userService.findUserById(75L); // Test
            User user = userRepository.findByUsername(username);
            if (user == null) {
                // По всей видимости сразу выкидывает
                throw new UsernameNotFoundException("User not found by name");
            }
            if(user.getId() == null){
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
    public boolean addUser(User userForm) {
        //@TODO Обработка ошибок уникальности полей
        userForm.setPassword(bCryptPasswordEncoder.encode(userForm.getPassword()));
        try {
            userRepository.save(userForm);
        } catch (Exception e){
            return false;
        }
        return true;
    }
    public boolean saveUser(User userForm, User mySelf) {
        //@TODO Обработка ошибок уникальности полей
        if(userForm.getPassword().equals(userForm.getPasswordConfirm())){
            userForm.setPassword(bCryptPasswordEncoder.encode(userForm.getPassword()));
        }
        try {
            userRepository.save(userForm);
        } catch (Exception e){
            return false;
        }
        return true;
    }
    public Page<User> findPaginated(Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<User> list;
        List<User> items = userRepository.findAll();

        if (items.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, items.size());
            list = items.subList(startItem, toIndex);
        }

        Page<User> itemsPage = new PageImpl<>(list, PageRequest.of(currentPage, pageSize), items.size());

        return itemsPage;
    }
    public Iterable<User> getUsers(Integer pageNo, Integer pageSize, String sortBy){
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

        Page<User> pagedResult = userRepository.findAll(paging);

        if(pagedResult.hasContent()) {
            return pagedResult.getContent();
        } else {
            return new ArrayList<User>();
        }
    }
    public boolean deleteUser(Long userId) {
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public Role getDefaultRole() {
        return roleRepository.findById(1L).orElse(new Role());
    }
}