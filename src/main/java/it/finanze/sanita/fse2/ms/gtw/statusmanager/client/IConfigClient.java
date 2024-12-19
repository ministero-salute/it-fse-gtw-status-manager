 it.finanze.sanita.fse2.ms.gtw.statusmanager.client;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ConfigItemTypeEnum;

public interface IConfigClient {
	ConfigItemDTO getConfigurationItems(ConfigItemTypeEnum type);
	String getProps(String props, String previous, ConfigItemTypeEnum ms);
}
