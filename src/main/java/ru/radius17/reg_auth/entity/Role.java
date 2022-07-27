package ru.radius17.reg_auth.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "t_role")
@Getter
@Setter
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(generator = "uuid4")
    @GenericGenerator(name = "uuid4", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    private String name;

    private String label;

    @Transient
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    public Role() {
    }

    @Override
    public String getAuthority() {
        return getName();
    }
    /*
    @Override
    public boolean equals(Object object) {
        // if (this == object) return true;

        if (object == null || object.getClass() != getClass()) {
            return false;
        } else {
            Role role = (Role) object;
            if (this.id.equals(role.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }
    */
}
