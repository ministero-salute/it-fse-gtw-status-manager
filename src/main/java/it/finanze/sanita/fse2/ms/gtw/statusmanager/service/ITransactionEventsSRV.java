package it.finanze.sanita.fse2.ms.gtw.statusmanager.service;

import java.io.Serializable;
import java.util.List;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.PublicationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.ValidationCDAInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionEventsETY;

public interface ITransactionEventsSRV extends Serializable {

  /**
   * Save on database the validation event received
   * 
   * @param validationInfo
   * @param validationResult
   * @param isHistoricalDoc
   * @param isTSFeeding
   */
  public void saveValidationEvent(ValidationCDAInfoDTO validationInfo, ValidationResultEnum validationResult, boolean isHistoricalDoc, boolean isTSFeeding);


  /**
   * Save on database the publication event received
   * 
   * @param publicationInfo
   * @param publicationResult
   * @param isHistoricalDoc
   * @param isTSFeeding
   */
  public void savePublicationEvent(PublicationInfoDTO publicationInfo, PublicationResultEnum publicationResult, boolean isHistoricalDoc, boolean isTSFeeding);


  /**
   * Retrieve from database the validation event transaction using the transaction
   * id
   * 
   * @param txID      Transaction ID
   * @param errorOnly
   */
  public TransactionEventsETY getTransactionEventByTxID(String transactionID, Boolean errorOnly);

  /**
   * Find the validation evntes transaction that matches the fields provided
   * 
   * @param lastValidationResult
   * @param lastEventType
   * @param eventType
   * @param eventValidationResult
   * @param startDate
   * @param endDate
   * @param errorOnly
   * @return
   */
  public List<TransactionEventsETY> findTransactions(ValidationResultEnum lastValidationResult,
  PublicationResultEnum lastPublicationResult, EventTypeEnum lastEventType, EventTypeEnum eventType,
  ValidationResultEnum eventValidationResult, PublicationResultEnum eventPublicationResult,
  String identificativoDoc, String identificativoPaziente, String identificativoSottomissione,
  Boolean forcePublish, String startDate, String endDate, Boolean errorOnly);

}
