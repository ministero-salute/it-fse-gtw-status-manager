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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.executors.base;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActionRes;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.actions.ActionBuilderEDS;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.actions.base.IActionStepEDS;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.actions.base.IExecutableEDS;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;

@Slf4j
public abstract class LExecutor implements IExecutableEDS {

    @Override
    public ActionRes execute() {
        return ActionBuilderEDS.execute(getSteps());
    }

    protected abstract ActionRes onReset();

    protected abstract LinkedHashMap<String, IActionStepEDS> getSteps();

}
