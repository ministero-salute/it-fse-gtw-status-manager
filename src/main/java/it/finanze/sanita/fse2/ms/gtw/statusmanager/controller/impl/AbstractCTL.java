package it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.impl;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.LogTraceInfoDTO;

/**
 * 
 * @author CPIERASC
 *
 *	Abstract controller.
 */
public abstract class AbstractCTL implements Serializable {

	@Autowired
	private Tracer tracer;

	protected LogTraceInfoDTO getLogTraceInfo() {
		LogTraceInfoDTO out = new LogTraceInfoDTO(null, null);
		if (tracer.currentSpan() != null) {
			out = new LogTraceInfoDTO(
					tracer.currentSpan().context().spanIdString(), 
					tracer.currentSpan().context().traceIdString());
		}
		return out;
	}

}
