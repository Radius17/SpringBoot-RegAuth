package ru.radius17.reg_auth;

import lombok.Getter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("dev")
@Getter
public abstract class BaseIntegrationTest {

    private final String requestScheme="https";
    private final String requestDomain="localhost";

    @LocalServerPort
    @Getter
    private int rdmServerPort;

    public String getRequestURL(){
        return this.getRequestScheme() +"://"+this.getRequestDomain()+":" + this.getRdmServerPort();
    }

}