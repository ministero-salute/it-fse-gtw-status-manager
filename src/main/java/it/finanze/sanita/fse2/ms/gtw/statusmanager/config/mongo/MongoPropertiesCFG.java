 it.finanze.sanita.fse2.ms.gtw.statusmanager.config.mongo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 *	Mongo properties configuration.
 */
@Data
@Component
@EqualsAndHashCode(callSuper = false)  
public class MongoPropertiesCFG {
  
	@Value("${data.mongodb.uri}")
	private String uri;

	@Value("${data.mongodb.schema-name}")
	private String schemaName;
}
