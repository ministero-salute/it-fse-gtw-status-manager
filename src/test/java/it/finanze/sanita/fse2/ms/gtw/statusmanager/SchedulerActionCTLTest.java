package it.finanze.sanita.fse2.ms.gtw.statusmanager;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(Constants.Profile.TEST)
@AutoConfigureMockMvc
public class SchedulerActionCTLTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void testRunSchedulerAction404() throws Exception {
        mockMvc.perform(post("/v1/runStatusScheduler")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }
}
