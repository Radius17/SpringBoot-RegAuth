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
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @Size(min=4, message = "Не меньше 4 знаков")
    private String username;

    @Getter
    @Setter
    private String password;

    @Transient
    @Getter
    @Setter
    private String passwordConfirm;

    @Getter
    @Setter
    @Size(min=4, message = "Не меньше 4 знаков")
    private String nickname;

    @Getter
    @Setter
    // @NonNull
    @NotEmpty(message = "Не заполнен телефон")
    private String phone;

    @Getter
    @Setter
    @NotEmpty(message = "Не заполнен E-mail")
    private String email;

    @Getter
    @Setter
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @Getter
    @Setter
    private Set<Role> roles;

    @Getter
    @Setter
    @Builder.Default
    private Boolean enabled = false;

    public User() { }

    @Override
    public boolean isAccountNonExpired() { return this.enabled; }

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