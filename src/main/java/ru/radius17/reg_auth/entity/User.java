package ru.radius17.reg_auth.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "t_user")
@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(generator = "uuid4")
    @GenericGenerator(name = "uuid4", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Size(min = 4, message = "{user.atLeastXCharacters}")
    private String username;

    private String password;

    @Transient
    private String passwordConfirm;

    @Size(min = 4, message = "{user.atLeastXCharacters}")
    private String nickname;

    @NotEmpty(message = "{user.emptyPhone}")
    private String phone;

    @Email
    @NotEmpty(message = "{user.emptyEmail}")
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