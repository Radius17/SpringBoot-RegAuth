package ru.radius17.reg_auth.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "t_notify_log")
@Getter
@Setter
public class Notify {
    @Id
    @GeneratedValue(generator = "uuid4")
    @GenericGenerator(name = "uuid4", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private Integer status;

    @Column(name = "local_date_time", columnDefinition = "TIMESTAMP")
    private ZonedDateTime localDateTime;

    private String subject;

    private String message;

    private String description;
}
