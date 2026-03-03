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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.client;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.eds.GetIngestionStatusResDTO;

/**
 * Interface for EDS Client operations.
 */
public interface IEdsClient {

    /**
     * Retrieves the ingestion status for a given workflow instance ID from EDS broker.
     *
     * @param workflowInstanceId The workflow instance identifier
     * @return GetIngestionStatusResDTO containing the status information
     */
    GetIngestionStatusResDTO getIngestionStatus(String workflowInstanceId);
}
