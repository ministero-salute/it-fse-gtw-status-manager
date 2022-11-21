package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "#{@transactionDataBean}")
@Data
@NoArgsConstructor
public class FhirEvent {

    private static final String FHIR_OUTCOME = "SUCCESS";
    private static final String FHIR_TYPE = "FHIR_PROCESSING";

    public static final String FIELD_ID = "_id";
    public static final String FIELD_WIF = "workflow_instance_id";
    public static final String FIELD_EVENT_DATE = "eventDate";
    public static final String FIELD_EVENT_TYPE = "eventType";
    public static final String FIELD_EVENT_STATUS = "eventStatus";

    @Id
    private String id;
    @Field(name = FIELD_WIF)
    private String workflowInstanceId;
    @Field(name = FIELD_EVENT_DATE)
    private String date;
    @Field(name = FIELD_EVENT_TYPE)
    private String type;
    @Field(name = FIELD_EVENT_STATUS)
    private String status;

    public static FhirEvent asSuccess(String wif, String date) {
        // Create document
        FhirEvent event = new FhirEvent();
        // Update field
        event.setWorkflowInstanceId(wif);
        event.setDate(date);
        event.setType(FHIR_TYPE);
        event.setStatus(FHIR_OUTCOME);
        return event;
    }

}
