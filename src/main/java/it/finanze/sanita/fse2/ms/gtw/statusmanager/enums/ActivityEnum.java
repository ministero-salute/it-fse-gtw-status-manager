package it.finanze.sanita.fse2.ms.gtw.statusmanager.enums;

public enum ActivityEnum {

	VALIDATION("V"), 
	PRE_PUBLISHING("P");

	private String code;

	private ActivityEnum(String inCode) {
		code = inCode;
	}

	public String getCode() {
		return code;
	}

}