package ru.radius17.reg_auth.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "t_role")
public class Role implements GrantedAuthority {
    @Id
    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private String name;
    @Transient
    @ManyToMany(mappedBy = "roles")
    @Getter
    @Setter
    private Set<User> users;

    public Role() { }

    @Override
    public String getAuthority() {
        return getName();
    }
}
