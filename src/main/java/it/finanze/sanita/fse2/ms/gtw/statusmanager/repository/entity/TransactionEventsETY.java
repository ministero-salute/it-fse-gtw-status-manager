package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.TransactionEventDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ValidationResultEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model to save validation events.
 */
@Document(collection = "transaction_data")
@Data
@NoArgsConstructor
public class TransactionEventsETY {

    @Id
	private String id;
	
	@Field(name = "transactionID")
	private String transactionID;

    @Field(name = "last_update")
	private Date lastUpdate;  
	
	@Field(name = "last_event_type")
	private EventTypeEnum lastEventType;

	@Field(name = "last_validation_result")
	private ValidationResultEnum lastValidationResult;

	@Field(name = "last_publication_result")
	private PublicationResultEnum lastPublicationResult;

    @Field(name = "events")
	private List<TransactionEventDTO> events;
    
}
