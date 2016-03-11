package net.vicp.lylab.server.action.manager;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.server.ServerRuntime;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.tq.Task;

public class StopAction extends BaseAction {
	protected int closeWait = 5;

	@SuppressWarnings("serial")
	@Override
	public void exec() {
		try {
			do {
				getResponse().getBody().put("result", "Stop procedure will start in " + closeWait + " seconds.");
				getResponse().success();
				new Task() {
					@Override
					public void exec() {
						System.out.println("Stop procedure will start in " + closeWait + " seconds.");
						await(closeWait * CoreDef.SECOND);
						ServerRuntime.close();
					}
				}.begin("ServerDestroyer");
			} while (false);
		} catch (Exception e) {
			log.error("Exception detected:" + Utils.getStringFromException(e));
		}
	}

	@Override
	public boolean foundBadParameter() {
		return false;
	}

}
