package net.vicp.lylab.server.model;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.model.RPCMessage;

public abstract class RPCBaseAction extends BaseAction {

	@Override
	public RPCMessage getRequest() {
		return (RPCMessage) super.getRequest();
	}

}
