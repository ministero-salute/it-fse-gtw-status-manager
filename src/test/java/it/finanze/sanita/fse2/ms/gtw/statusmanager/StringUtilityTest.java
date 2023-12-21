package it.finanze.sanita.fse2.ms.gtw.statusmanager;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles(Constants.Profile.TEST)
@Slf4j
@ExtendWith(SpringExtension.class)
public class StringUtilityTest {
    @Test
    void testIsNullOrEmpty(){
        assertTrue(StringUtility.isNullOrEmpty(null));
        assertTrue(StringUtility.isNullOrEmpty(""));
        assertFalse(StringUtility.isNullOrEmpty("Test"));
    }
}
