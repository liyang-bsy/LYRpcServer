package net.vicp.lylab.lyserver.action;

import net.vicp.lylab.core.BaseAction;

public class DecAction extends BaseAction {

	@Override
	public void exec() {
		do {
			Integer i = (Integer) getRequest().getBody().get("int");
			if (i == null) {
				response.setCode(-2);
				response.setMessage("数字不存在");
				break;
			}
			response.getBody().put("int", i--);
		response.success(); } while (false);
	}

}
