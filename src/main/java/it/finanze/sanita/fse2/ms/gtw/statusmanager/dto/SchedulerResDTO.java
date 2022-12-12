/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SchedulerResDTO extends ResponseDTO{
	
	Map<String,Integer> output;
	
	public SchedulerResDTO() {
		super();
	}

	public SchedulerResDTO(final LogTraceInfoDTO traceInfo, final Map<String,Integer> inOutput) {
		super(traceInfo);
		output = inOutput;
	}

}
