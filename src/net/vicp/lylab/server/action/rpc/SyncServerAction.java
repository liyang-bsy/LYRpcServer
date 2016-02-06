package net.vicp.lylab.server.action.rpc;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.server.model.RPCBaseAction;
import net.vicp.lylab.server.rpc.RpcConnector;

public class SyncServerAction extends RPCBaseAction {

	@Override
	public void exec() {
		do {
			RpcConnector connector = (RpcConnector) CoreDef.config.getConfig("Singleton").getObject("connector");
			if (!connector.sync()) {
				getResponse().fail("Sync failed, check log for detail.");
				break;
			}
			getResponse().success();
		} while (false);
	}

	@Override
	public boolean foundBadParameter() {
		return false;
	}

}
