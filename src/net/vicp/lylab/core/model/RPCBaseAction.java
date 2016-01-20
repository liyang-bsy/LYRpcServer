package net.vicp.lylab.core.model;

import net.vicp.lylab.core.BaseAction;

public abstract class RPCBaseAction extends BaseAction {

	@Override
	public RPCMessage getRequest() {
		return (RPCMessage) super.getRequest();
	}

}
