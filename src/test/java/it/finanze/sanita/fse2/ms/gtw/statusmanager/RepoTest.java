 it.finanze.sanita.fse2.ms.gtw.statusmanager;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl.ConfigSRV;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.impl.TransactionEventsRepo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class RepoTest { 
	
	@MockBean
	private ConfigSRV config;

	@Test
	@DisplayName("transaction Events Repo test Ko ")
	void transactionEvTestKo() {
		TransactionEventsRepo transEvRep = new TransactionEventsRepo();
		
		given(config.getExpirationDate()).willReturn(0);
		assertThrows(BusinessException.class, () -> transEvRep.saveEvent(null, null));
	}
	
}
