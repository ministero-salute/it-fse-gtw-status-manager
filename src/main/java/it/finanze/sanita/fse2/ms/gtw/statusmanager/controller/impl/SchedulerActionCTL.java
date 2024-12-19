 it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.ISchedulerActionCTL;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.TxScheduler;

@RestController
@ConditionalOnExpression("'${scheduler.tx-scheduler}'!='-'")
public class SchedulerActionCTL extends AbstractCTL implements ISchedulerActionCTL{

	
	@Autowired
	private TxScheduler txScheduler;
	
	@Override
	public void runSchedulerAction() {
		txScheduler.action();
	}	
}
