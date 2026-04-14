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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "#{@transactionDataBean}")
@Data
@NoArgsConstructor
public class TransactionDataETY {

    private static final String FHIR_OUTCOME = "SUCCESS";
    private static final String FHIR_TYPE = "EDS_WORKFLOW";
    public static final String FHIR_TYPE_UAR = "UAR_FINAL_STATUS";
    public static final String PULL_STATUS_BLOCKED = "BLOCKED";

    public static final String FIELD_ID = "_id";
    public static final String FIELD_WIF = "workflow_instance_id";
    public static final String FIELD_EVENT_DATE = "eventDate";
    public static final String FIELD_EVENT_TYPE = "eventType";
    public static final String FIELD_EVENT_STATUS = "eventStatus";
    public static final String FIELD_EXPIRING_DATE = "expiring_date";
    public static final String FIELD_EXTRA = "extra";
    public static final String FIELD_PULL_STATUS_OUTCOME = "pullStatusOutcome";
    public static final String FIELD_TRACE_ID = "traceId";
    public static final String FIELD_ISSUER = "issuer";
    public static final String FIELD_SUBJECT = "subject";
    public static final String FIELD_IDENTIFICATIVO_DOCUMENTO = "identificativoDocumento";
    public static final String FIELD_TIPO_ATTIVITA = "tipoAttivita";
    public static final String FIELD_ORGANIZZAZIONE = "organizzazione";
    public static final String FIELD_MICROSERVICE_NAME = "microserviceName";
    public static final String FIELD_DETAIL = "detail";
    
    @Id
    private String id;
    @Field(name = FIELD_WIF)
    private String workflowInstanceId;
    @Field(name = FIELD_EVENT_DATE)
    private Date date;
    @Field(name = FIELD_EVENT_TYPE)
    private String type;
    @Field(name = FIELD_EVENT_STATUS)
    private String status;
    @Field(name = FIELD_EXPIRING_DATE)
    private Date expiringDate;
    @Field(name = FIELD_EXTRA)
    private String extra;
    @Field(name = FIELD_PULL_STATUS_OUTCOME)
    private String pullStatusOutcome;
    @Field(name = FIELD_TRACE_ID)
    private String traceId;
    @Field(name = FIELD_ISSUER)
    private String issuer;
    @Field(name = FIELD_SUBJECT)
    private String subject;
    @Field(name = FIELD_IDENTIFICATIVO_DOCUMENTO)
    private String identificativoDocumento;
    @Field(name = FIELD_TIPO_ATTIVITA)
    private String tipoAttivita;
    @Field(name = FIELD_ORGANIZZAZIONE)
    private String organizzazione;
    @Field(name = FIELD_MICROSERVICE_NAME)
    private String microserviceName;
    @Field(name = FIELD_DETAIL)
    private String detail;
    

    public static TransactionDataETY asSuccess(String wif, Date date, Date expiringDate) {
        // Create document
        TransactionDataETY event = new TransactionDataETY();
        // Update field
        event.setWorkflowInstanceId(wif);
        event.setDate(date);
        event.setType(FHIR_TYPE);
        event.setStatus(FHIR_OUTCOME);
        event.setExpiringDate(expiringDate);
        return event;
    }

}
