package ru.radius17.reg_auth.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "t_user")
@Getter
@Setter
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 4, message = "Не меньше 4 знаков")
    private String username;

    private String password;

    @Transient
    private String passwordConfirm;

    @Size(min = 4, message = "Не меньше 4 знаков")
    private String nickname;

    // @NonNull
    @NotEmpty(message = "Не заполнен телефон")
    private String phone;

    @NotEmpty(message = "Не заполнен E-mail")
    private String email;

    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @Builder.Default
    private Boolean enabled = false;

    public User() {
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }
}