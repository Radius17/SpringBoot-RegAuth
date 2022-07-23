package ru.radius17.reg_auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.validation.BindingResult;
import ru.radius17.reg_auth.entity.User;
import ru.radius17.reg_auth.repository.UserRepository;

import javax.transaction.Transactional;
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

    private void checkConstraintViolation(DataIntegrityViolationException e, BindingResult bindingResult){
        org.hibernate.exception.ConstraintViolationException exDetail = (org.hibernate.exception.ConstraintViolationException) e.getCause();
        String constraintName = exDetail.getConstraintName();
        String constraintRejectedFieldName = "";
        String constraintRejectedFieldMessage = "";
        switch (constraintName) {
            case "t_user_username_key":
                constraintRejectedFieldName = "username";
                constraintRejectedFieldMessage = ms.getMessage("NotUnique.user.username", null, LocaleContextHolder.getLocale());
                break;
            case "t_user_email_key":
                constraintRejectedFieldName = "email";
                constraintRejectedFieldMessage = ms.getMessage("NotUnique.user.email", null, LocaleContextHolder.getLocale());
                break;
            case "t_user_nickname_key":
                constraintRejectedFieldName = "nickname";
                constraintRejectedFieldMessage = ms.getMessage("NotUnique.user.nickname", null, LocaleContextHolder.getLocale());
                break;
            case "t_user_phone_key":
                constraintRejectedFieldName = "phone";
                constraintRejectedFieldMessage = ms.getMessage("NotUnique.user.phone", null, LocaleContextHolder.getLocale());
                break;
        }
        if(!constraintRejectedFieldName.isEmpty())
            bindingResult.rejectValue(constraintRejectedFieldName, null, constraintRejectedFieldMessage);
    }

    public boolean saveUser(User userForm, BindingResult bindingResult, Boolean isNewUser) {
        if(isNewUser){
            userForm.setPassword(bCryptPasswordEncoder.encode(userForm.getPassword()));
        } else {
            if (userForm.getPassword().equals(userForm.getPasswordConfirm())) {
                userForm.setPassword(bCryptPasswordEncoder.encode(userForm.getPassword()));
            }
        }

        try {
            this.save(userForm);
        } catch (DataIntegrityViolationException e) {
            this.checkConstraintViolation(e, bindingResult);
            return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    @Transactional
    public void save(User user){
        userRepository.save(user);
    }
    public Page<User> getUsersPaginated(Pageable pageable) {
        Page<User> pagedResult = userRepository.findAll(pageable);
        return pagedResult;
    }

    public boolean deleteUser(UUID userId) {
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }
}