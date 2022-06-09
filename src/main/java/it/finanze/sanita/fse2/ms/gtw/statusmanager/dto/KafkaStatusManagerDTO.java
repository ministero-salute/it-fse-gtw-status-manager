package  it.finanze.sanita.fse2.ms.gtw.statusmanager.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Content of a Kafka message
 */
@Getter
@Builder
public class KafkaStatusManagerDTO implements AbstractDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -2144344497297675698L;

	private String json;

}
