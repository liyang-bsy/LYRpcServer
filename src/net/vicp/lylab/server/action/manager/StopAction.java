package net.vicp.lylab.server.action.manager;

import net.vicp.lylab.core.model.RPCBaseAction;
import net.vicp.lylab.server.ServerRuntime;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;
import net.vicp.lylab.utils.tq.Task;

public class StopAction extends RPCBaseAction {
	public static AtomicBoolean changeable = new AtomicBoolean(true);

	@SuppressWarnings("serial")
	@Override
	public void exec() {
		try {
			do {
				getResponse().success();
				new Task() {
					@Override
					public void exec() {
						ServerRuntime.close();
					}
				}.begin("ServerDestroyer");
			} while (false);
		} catch (Exception e) {
			log.error("Exception detected:" + Utils.getStringFromException(e));
		}
	}

}
