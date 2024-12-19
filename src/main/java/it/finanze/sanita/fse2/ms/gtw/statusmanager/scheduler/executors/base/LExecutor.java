 it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.executors.base;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActionRes;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.actions.ActionBuilderEDS;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.actions.base.IActionStepEDS;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.actions.base.IExecutableEDS;

import java.util.LinkedHashMap;

public abstract class LExecutor implements IExecutableEDS {

    @Override
    public ActionRes execute() {
        return ActionBuilderEDS.execute(getSteps());
    }

    protected abstract ActionRes onReset();

    protected abstract LinkedHashMap<String, IActionStepEDS> getSteps();

}
