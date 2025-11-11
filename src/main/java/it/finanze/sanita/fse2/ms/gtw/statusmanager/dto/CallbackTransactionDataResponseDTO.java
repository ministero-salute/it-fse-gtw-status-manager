package it.finanze.sanita.fse2.ms.gtw.statusmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class CallbackTransactionDataResponseDTO {
    
    private Boolean success;
    
}
