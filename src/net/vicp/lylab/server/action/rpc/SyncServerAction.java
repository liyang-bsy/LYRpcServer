package net.vicp.lylab.server.action.rpc;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.server.model.RPCBaseAction;
import net.vicp.lylab.server.rpc.RpcConnector;
import net.vicp.lylab.utils.Utils;

public class SyncServerAction extends RPCBaseAction {

	@Override
	public boolean foundBadParameter() {
		return false;
	}

	@Override
	public void exec() {
		do {
			RpcConnector connector = (RpcConnector) CoreDef.config.getConfig("Singleton").getObject("connector");
			connector.sync();

			getResponse().success();
			
			System.out.println(Utils.getPeer(clientSocket) + " requested SyncServerAction");
		} while (false);
	}

}
