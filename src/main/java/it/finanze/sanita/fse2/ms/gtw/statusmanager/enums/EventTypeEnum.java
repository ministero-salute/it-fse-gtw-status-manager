package it.finanze.sanita.fse2.ms.gtw.statusmanager.enums;

public enum EventTypeEnum {

    VALIDATION("V"), 
	PUBLICATION("P"),
    HISTORICAL_DOC_VALIDATION("HV"),
    HISTORICAL_DOC_PUBLICATION("HP"),
	TS_DOC_VALIDATION("TSV"),
	TS_DOC_PUBLICATION("TSP");

	private String code;

	private EventTypeEnum(String inCode) {
		code = inCode;
	}

	public String getCode() {
		return code;
	}
    
}
