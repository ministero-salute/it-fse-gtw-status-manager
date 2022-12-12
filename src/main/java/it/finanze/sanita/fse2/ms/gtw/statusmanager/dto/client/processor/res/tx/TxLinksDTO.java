/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TxLinksDTO {
	
    private String prev;
    
    private String next;
}
