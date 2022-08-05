package ru.radius17.reg_auth.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.radius17.reg_auth.entity.Notify;

import java.util.UUID;

public interface NotifyRepository extends JpaRepository<Notify, UUID>, JpaSpecificationExecutor<Notify> {
    Page<Notify> findAll(Specification specification, Pageable pageable);
}
