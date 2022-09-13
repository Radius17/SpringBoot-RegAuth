package ru.radius17.reg_auth.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "t_notify_log")
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(generator = "uuid4")
    @GenericGenerator(name = "uuid4", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    private Integer status;

    @Column(name = "notification_type")
    private String notificationType;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    private String subject;

    private String message;

    private String description;
}
