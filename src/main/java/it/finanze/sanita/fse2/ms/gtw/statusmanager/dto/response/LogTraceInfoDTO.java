package it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.AbstractDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *	Trace info.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LogTraceInfoDTO extends AbstractDTO {

	/**
	 * Span.
	 */
	private final String spanID;
	
	/**
	 * Trace.
	 */
	private final String traceID;

}
