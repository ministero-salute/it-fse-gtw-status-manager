/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.impl.TransactionEventsRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class RepoTest { 

	@Test
	@DisplayName("transaction Events Repo test Ko ")
	void transactionEvTestKo() {
		TransactionEventsRepo transEvRep = new TransactionEventsRepo();
		assertThrows(BusinessException.class, () -> transEvRep.saveEvent(null, null));
	}
	
}
