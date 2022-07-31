package ru.radius17.reg_auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.radius17.reg_auth.BaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserController userController;

    @Test
    public void contextLoads() throws Exception {
        assertThat(userController).isNotNull();
    }

    @Test
    void loginForm() {
    }

    @Test
    void registrationForm() {
    }

    @Test
    void addProfile() {
    }

    @Test
    void modifyProfile() {
    }

    @Test
    void saveProfile() {
    }
}