/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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
		public static final String EVENT_ISSUER = "issuer";
		public static final String EVENT_SUBJECT = "subject";
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
