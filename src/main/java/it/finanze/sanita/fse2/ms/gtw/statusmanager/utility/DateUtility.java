package it.finanze.sanita.fse2.ms.gtw.statusmanager.utility;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class DateUtility {
    public static OffsetDateTime getCurrentTime() {
        return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS);
    }
}
