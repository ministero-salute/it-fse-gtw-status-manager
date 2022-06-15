package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionEventsETY;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class TransactionEventsRepo extends AbstractMongoRepository<TransactionEventsETY, String>
		implements ITransactionEventsRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -4017623557412046071L;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public void saveEvent(String transactionId, String json) {
		try {
			Document doc = Document.parse(json);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
			Date eventDate = sdf.parse(doc.getString("eventDate"));
			doc.put("eventDate", eventDate);
			doc.put("transactionId", transactionId);
			mongoTemplate.insert(doc, "transaction_data");
		} catch(Exception ex){
			log.error("Error while save event : " , ex);
			throw new BusinessException("Error while save event : " , ex);
		}
	}
	 
}
