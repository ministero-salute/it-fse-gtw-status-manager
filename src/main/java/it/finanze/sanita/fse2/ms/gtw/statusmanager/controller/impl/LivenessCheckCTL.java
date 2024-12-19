 it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.impl;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Class LivenessCheckController.
 */
@RestController
public class LivenessCheckCTL implements HealthIndicator {

	/**
	 * Return system state.
	 * 
	 * @return system state
	 */
	@Override
	@GetMapping("/status")
	public Health health() {
		return Health.up().build();
	}

}
