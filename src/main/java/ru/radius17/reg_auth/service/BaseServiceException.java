package ru.radius17.reg_auth.service;

import lombok.Getter;
import org.springframework.dao.DataIntegrityViolationException;

public class BaseServiceException extends Exception{
    @Getter
    String constraintRejectedFieldName = "";
    @Getter
    String constraintRejectedFieldMessage = "";

    public BaseServiceException(DataIntegrityViolationException e) {
        org.hibernate.exception.ConstraintViolationException exDetail = (org.hibernate.exception.ConstraintViolationException) e.getCause();
        String constraintName = exDetail.getConstraintName();
        switch (constraintName) {
            case "t_user_username_key":
                this.constraintRejectedFieldName = "username";
                this.constraintRejectedFieldMessage = "NotUnique.user.username";
                break;
            case "t_user_email_key":
                this.constraintRejectedFieldName = "email";
                this.constraintRejectedFieldMessage = "NotUnique.user.email";
                break;
            case "t_user_nickname_key":
                this.constraintRejectedFieldName = "nickname";
                this.constraintRejectedFieldMessage = "NotUnique.user.nickname";
                break;
            case "t_user_phone_key":
                this.constraintRejectedFieldName = "phone";
                this.constraintRejectedFieldMessage = "NotUnique.user.phone";
                break;
            default:
                this.constraintRejectedFieldName = "";
                this.constraintRejectedFieldMessage = "";
                break;
        }
    }
}