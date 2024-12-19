 it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.ProfileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

import javax.annotation.PostConstruct;

/**
 *	Kafka topic configuration.
 */
@Data
@Component
public class KafkaTopicCFG {

	@Autowired
	private ProfileUtility profileUtility;

	/**
	 * Topic.
	 */
	@Value("${kafka.statusmanager.topic}")
	private String statusManagerTopic;
	
	/**
	 * Topic.
	 */
	@Value("${kafka.statusmanager.eds.topic}")
	private String statusManagerEdsTopic;
	
	/**
	 * Topic.
	 */
	@Value("${kafka.statusmanager.deadletter.topic}")
	private String statusManagerTopicDlt;
	
	/**
	 * Topic.
	 */
	@Value("${kafka.statusmanager.eds.deadletter.topic}")
	private String statusManagerEdsTopicDlt;

	@PostConstruct
	public void afterInit() {
		if (profileUtility.isTestProfile()) {
			statusManagerTopic = Constants.Profile.TEST_PREFIX + statusManagerTopic;
			statusManagerEdsTopic = Constants.Profile.TEST_PREFIX + statusManagerEdsTopic;
			statusManagerTopicDlt = Constants.Profile.TEST_PREFIX + statusManagerTopicDlt;
			statusManagerEdsTopicDlt = Constants.Profile.TEST_PREFIX + statusManagerEdsTopicDlt;
			
		}
	}
}
