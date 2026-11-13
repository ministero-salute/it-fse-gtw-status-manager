package it.finanze.sanita.fse2.ms.gtw.statusmanager.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.SubjectOrganizationEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.CollectionNaming;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.*;

@Tag("ISOLATION")
class UtilitiesIsolationTest {
    @Test
    void organizationLookupSupportsEveryCodeAndCaseInsensitiveDisplay() {
        for (var organization : SubjectOrganizationEnum.values()) {
            assertSame(organization, SubjectOrganizationEnum.getCode(organization.getCode()));
            assertSame(organization, SubjectOrganizationEnum.getDisplay(organization.getDisplay().toUpperCase(Locale.ROOT)));
        }
        assertNull(SubjectOrganizationEnum.getCode("unknown")); assertNull(SubjectOrganizationEnum.getCode(null));
        assertNull(SubjectOrganizationEnum.getDisplay("unknown")); assertNull(SubjectOrganizationEnum.getDisplay(null));
    }

    @Test
    void profileDetectionControlsTestCollectionAndTopicPrefixes() {
        var profiles = new ProfileUtility();
        assertFalse(profiles.isTestProfile());
        Environment environment = mock(Environment.class);
        ReflectionTestUtils.setField(profiles, "environment", environment);
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        assertFalse(profiles.isTestProfile());
        var naming = new CollectionNaming(); ReflectionTestUtils.setField(naming, "profileUtility", profiles);
        var topics = new KafkaTopicCFG(); topics.setProfileUtility(profiles);
        topics.setStatusManagerTopic("events"); topics.setStatusManagerTopicDlt("dead"); topics.setFinalStateTopic("final");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"ISOLATION"});
        assertEquals("transaction_data", naming.getTransactionDataCollection());
        topics.afterInit(); assertEquals("events", topics.getStatusManagerTopic());
        when(environment.getActiveProfiles()).thenReturn(new String[]{"TEST"});
        assertTrue(profiles.isTestProfile());
        assertEquals("test_transaction_data", naming.getTransactionDataCollection());
        topics.afterInit();
        assertEquals("test_events", topics.getStatusManagerTopic());
        assertEquals("test_dead", topics.getStatusManagerTopicDlt()); assertEquals("test_final", topics.getFinalStateTopic());
    }

    @Test
    void dateArithmeticAndCurrentTimeUseExpectedPrecisionAndRejectNull() {
        var now = DateUtility.getCurrentTime();
        assertEquals(ZoneOffset.UTC, now.getOffset()); assertEquals(0, now.getNano() % 1000000);
        Date date = new Date(1700000000000L);
        assertEquals(date, DateUtility.addDay(DateUtility.addDay(date, 3), -3));
        assertThrows(BusinessException.class, () -> DateUtility.addDay(null, 1));
    }

    @Test
    void stringsDistinguishEmptyFromWhitespaceAndResourcesReturnBytesOrNull() {
        assertTrue(StringUtility.isNullOrEmpty(null)); assertTrue(StringUtility.isNullOrEmpty(""));
        assertFalse(StringUtility.isNullOrEmpty(" ")); assertFalse(StringUtility.isNullOrEmpty("value"));
        var bytes = FileUtility.getFileFromInternalResources("application-test.properties");
        assertNotNull(bytes); assertFalse(new String(bytes, StandardCharsets.UTF_8).isBlank());
        assertNull(FileUtility.getFileFromInternalResources("isolation-resource-does-not-exist"));
    }
}
