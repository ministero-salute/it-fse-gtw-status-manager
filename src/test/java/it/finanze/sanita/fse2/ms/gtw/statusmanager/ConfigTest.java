package it.finanze.sanita.fse2.ms.gtw.statusmanager;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base.ClientRoutes.Config.*;
import static it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants.Profile.TEST;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
class ConfigTest extends AbstractConfig {

    private static final List<Pair<String, String>> DEFAULT_PROPS = Arrays.asList(
        Pair.of(PROPS_NAME_EXP_DAYS, "5"),
        Pair.of(PROPS_NAME_SUBJECT, "false"),
        Pair.of(PROPS_NAME_ISSUER_CF, "false")
    );

    @Test
    void testCacheProps() {
        testCacheProps(DEFAULT_PROPS.get(0), () -> assertEquals(5, config.getExpirationDate()));
        testCacheProps(DEFAULT_PROPS.get(1), () -> assertFalse(config.isSubjectNotAllowed()));
        testCacheProps(DEFAULT_PROPS.get(2), () -> assertFalse(config.isCfOnIssuerNotAllowed()));
    }

    @Test
    void testRefreshProps() {
        testRefreshProps(DEFAULT_PROPS.get(0), "4", () -> assertEquals(4, config.getExpirationDate()));
        testRefreshProps(DEFAULT_PROPS.get(1), "true", () -> assertTrue(config.isSubjectNotAllowed()));
        testRefreshProps(DEFAULT_PROPS.get(2), "true", () -> assertTrue(config.isCfOnIssuerNotAllowed()));
    }

    @Test
    void testIntegrityProps() {
        testIntegrityCheck();
    }

    @Override
    public List<Pair<String, String>> defaults() {
        return DEFAULT_PROPS;
    }
}
