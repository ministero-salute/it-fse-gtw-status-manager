package it.finanze.sanita.fse2.ms.gtw.statusmanager.dto;

import java.util.Date;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
@ToString
public class CallbackTransactionDataRequestDTO {
    @NotNull
    private String workflowInstanceId;
    @NotNull
    private String type;
    @NotNull
	private Date insertionDate;
    @NotNull
    private String status;
    @NotNull
    private String message;
}
