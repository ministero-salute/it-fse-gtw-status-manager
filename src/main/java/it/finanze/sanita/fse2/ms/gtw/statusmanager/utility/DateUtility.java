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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.utility;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateUtility {

	private DateUtility() {}
	
    public static OffsetDateTime getCurrentTime() {
        return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS);
    }
    
    public static Date addDay(final Date date, final Integer nDays) {
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(date);
			c.add(Calendar.DATE, nDays);
		} catch(Exception ex) {
			log.error("Error while perform addDay : " , ex);
			throw new BusinessException("Error while perform addDay : " , ex);
		}
		return c.getTime();
		
	}
}
