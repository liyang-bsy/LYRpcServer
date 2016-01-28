package net.vicp.lylab.server.action.manager;

import net.vicp.lylab.server.model.RPCBaseAction;
import net.vicp.lylab.utils.Utils;

public class SwithModificationAction extends RPCBaseAction {
	protected static boolean changeable = true;

	@Override
	public void exec() {
		try {
			do {
				changeable = !changeable;
				getResponse().getBody().put("Current mode", changeable?"changeable":"unchangeable");
				getResponse().success();
			} while (false);
		} catch (Exception e) {
			log.error("Exception detected:" + Utils.getStringFromException(e));
		}
	}

	public static boolean isChangeable() {
		return changeable;
	}

}
