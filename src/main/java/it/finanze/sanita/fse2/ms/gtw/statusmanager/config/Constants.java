/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.config;

/**
 * Constants application.
 */
public final class Constants {

	
	public static final class Collections {

		public static final String TRANSACTION_DATA = "transaction_data";

		private Collections() {

		}
	}

	public static final class Fields {
		public static final String EVENT_STATUS = "eventStatus";
		public static final String TRACE_ID = "traceId";
		public static final String WORKFLOW_INSTANCE_ID = "workflow_instance_id";
		public static final String EVENT_DATE = "eventDate";
		public static final String EXPIRING_DATE = "expiring_date";

		private Fields() {}
		public static final String EVENT_TYPE = "eventType";
	}
 
	public static final class Profile {

		/**
		 * Test profile.
		 */
		public static final String TEST = "test";

		public static final String TEST_PREFIX = "test_";
		
		public static final String DEV = "dev";

		
		/** 
		 * Constructor.
		 */
		private Profile() {
			//This method is intentionally left blank.
		}

	}

	public static final class Logs {

		public static final String ERR_REP_FHIR_EVENTS = "Unable to insert FHIR events";
        public static final String EXECUTE_REQUEST = "{} - Executing request: {}";

        private Logs() {}
	}
  
	/**
	 *	Constants.
	 */
	private Constants() {

	}

}
