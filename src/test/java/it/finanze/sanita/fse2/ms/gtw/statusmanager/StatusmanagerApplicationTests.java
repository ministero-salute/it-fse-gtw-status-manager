package it.finanze.sanita.fse2.ms.gtw.statusmanager;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.OpenApiCFG;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class StatusmanagerApplicationTests {

	@Autowired
	OpenApiCFG openApiCFG;
	
	@Test
	void contextLoads() {
		
		assertNotNull(openApiCFG);
	}

}
