 it.finanze.sanita.fse2.ms.gtw.statusmanager.client.impl;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.ConfigClientRoutes;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ConfigItemTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ConfigItemTypeEnum.GENERIC;

@Slf4j
@Component
public class ConfigClient implements IConfigClient {

    @Autowired
    private RestTemplate client;

    @Autowired
    private ConfigClientRoutes routes;

    private boolean isReachable() {
        boolean out;
        try {
            client.getForEntity(routes.status(), String.class);
            out = true;
        } catch (ResourceAccessException clientException) {
            out = false;
        }
        return out;
    }

    @Override
    public ConfigItemDTO getConfigurationItems(ConfigItemTypeEnum type) {
        return client.getForObject(routes.getConfigItems(type), ConfigItemDTO.class);
    }

    @Override
    public String getProps(String props, String previous, ConfigItemTypeEnum ms) {
        String out = previous;
        ConfigItemTypeEnum src = ms;
        // Check if gtw-config is available and get props
        if (isReachable()) {
            // Try to get the specific one
            out = client.getForObject(routes.getConfigItem(ms, props), String.class);
            // If the props don't exist
            if (out == null) {
                // Retrieve the generic one
                out = client.getForObject(routes.getConfigItem(GENERIC, props), String.class);
                // Set where has been retrieved from
                src = GENERIC;
            }
        }
        if(out == null || !out.equals(previous)) {
            log.info("[GTW-CFG] {} set as {} (previously: {}) from {}", props, out, previous, src);
        }
        return out;
    }
}
