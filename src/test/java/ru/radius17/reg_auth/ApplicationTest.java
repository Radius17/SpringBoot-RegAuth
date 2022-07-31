package ru.radius17.reg_auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ApplicationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void main() throws Exception {
        // Should return default page with message
        String testString = "Танцуют все !";

        this.mockMvc.perform(get( this.getRequestURL()+ "/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(testString)));
    }

}