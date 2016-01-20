package net.vicp.lylab.server.action;

import net.vicp.lylab.core.model.RPCBaseAction;

public class DecAction extends RPCBaseAction {

	@Override
	public void exec() {
		do {
			Integer i = (Integer) getRequest().getBody().get("int");
			if (i == null) {
				getResponse().setCode(-2);
				getResponse().setMessage("数字不存在");
				break;
			}
			getResponse().getBody().put("int", i--);
		getResponse().success(); } while (false);
	}

}
