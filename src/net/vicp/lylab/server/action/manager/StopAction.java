package net.vicp.lylab.server.action.manager;

import net.vicp.lylab.server.ServerRuntime;
import net.vicp.lylab.server.model.RPCBaseAction;
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
				getResponse().getBody().put("result", "Stop procedure will start in 5 seconds.");
				getResponse().success();
				new Task() {
					@Override
					public void exec() {
						try {
							System.out.println("Stop procedure will start in 5 seconds.");
							Thread.sleep(5000L);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						ServerRuntime.close();
					}
				}.begin("ServerDestroyer");
			} while (false);
		} catch (Exception e) {
			log.error("Exception detected:" + Utils.getStringFromException(e));
		}
	}

}
