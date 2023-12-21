package it.finanze.sanita.fse2.ms.gtw.statusmanager;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ConfigItemTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles(Constants.Profile.TEST)

@Slf4j
public class ConfigItemTypeEnumTest {

    @Test
    public void testPriority() {
        // Call the priority method
        List<ConfigItemTypeEnum> items = ConfigItemTypeEnum.priority();

        // Assert that the first item is GENERIC
        assertEquals(ConfigItemTypeEnum.GENERIC, items.get(0));

        // Assert that the last item is GARBAGE
        assertEquals(ConfigItemTypeEnum.STATUS_MANAGER, items.get(items.size() - 1));
    }
}