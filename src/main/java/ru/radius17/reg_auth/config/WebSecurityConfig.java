package ru.radius17.reg_auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable();
        //Доступ только для не зарегистрированных пользователей
        httpSecurity.authorizeRequests().antMatchers("/registration").not().fullyAuthenticated();
        //Доступ только для пользователей с ролью Администратор
        httpSecurity.authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN");
        //Доступ только для пользователей с ролью Пользователь
        httpSecurity.authorizeRequests().antMatchers("/profile").hasRole("USER");
        //Доступ разрешен всем подряд
        httpSecurity.authorizeRequests().antMatchers("/", "/css/**", "/js/**", "/images/**").permitAll();
        //Все остальные страницы требуют аутентификации
        httpSecurity.authorizeRequests().anyRequest().authenticated();
        //Настройка для входа в систему и перенарпавление на главную страницу после успешного входа
        httpSecurity.formLogin().loginPage("/login").defaultSuccessUrl("/").permitAll();
        httpSecurity.rememberMe().key("uniqueAndSecret").tokenValiditySeconds(86400);
        // Настройки для выхода из системы
        httpSecurity.logout().permitAll().logoutSuccessUrl("/").deleteCookies("JSESSIONID");

        return httpSecurity.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}