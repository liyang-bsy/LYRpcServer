package net.vicp.lylab.server.action.manager;

import net.vicp.lylab.core.model.RPCBaseAction;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicBoolean;

public class SwithModificationAction extends RPCBaseAction {
	public static AtomicBoolean changeable = new AtomicBoolean(true);

	@Override
	public void exec() {
		try {
			do {
				getResponse().getBody().put("Current mode is ", changeable.get()?"changeable":"changeable");
				getResponse().success();
			} while (false);
		} catch (Exception e) {
			log.error("Exception detected:" + Utils.getStringFromException(e));
		}
	}

}
