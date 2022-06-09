package it.finanze.sanita.fse2.ms.gtw.statusmanager.enums;

import lombok.Getter;

public enum EventTypeEnum {

    VALIDATION("V"), 
	PUBLICATION("P"),
    HISTORICAL_DOC_VALIDATION("HV"),
    HISTORICAL_DOC_PUBLICATION("HP"),
	TS_DOC_VALIDATION("TSV"),
	TS_DOC_PUBLICATION("TSP");

	@Getter
	private String code;

	private EventTypeEnum(String inCode) {
		code = inCode;
	}


    
}
