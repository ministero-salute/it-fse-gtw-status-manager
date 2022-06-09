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

	/**
	 * 
	 */
	private static final long serialVersionUID = -8284964507529194465L;
	
	@Autowired
	private transient Tracer tracer;

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
