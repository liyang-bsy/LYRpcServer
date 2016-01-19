package net.vicp.lylab.server.rpc.connector;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.model.CallContent;
import net.vicp.lylab.core.model.Pair;

/**
 * Please configure it into action list, so that RCPDispatcherAop may found this action.
 * 
 * @author Young
 *
 */
public class RPCProcedureConnector extends BaseAction {

	@Override
	public void exec() {
		CallContent cc = (CallContent) getRequest();
		Pair<String, String> serverProcedure = new Pair<>(cc.getServer(), cc.getKey());
		
	}

}
