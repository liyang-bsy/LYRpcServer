package net.vicp.lylab.server.action.rpc;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.server.action.manager.SwithModificationAction;
import net.vicp.lylab.server.model.RPCBaseAction;
import net.vicp.lylab.server.rpc.RpcConnector;

public class RegisterServerAction extends RPCBaseAction {

	@Override
	public void exec() {
		do {
			if (!SwithModificationAction.isChangeable()) {
				getResponse().fail("Current mode is unchangeable");
				break;
			}
			String ip = clientSocket.getInetAddress().getHostAddress();
			Integer port = (Integer) getRequest().getBody().get("port");
			String server = (String) getRequest().getBody().get("server");

			RpcConnector connector = (RpcConnector) CoreDef.config.getConfig("Singleton").getObject("connector");

			connector.addServer(server, ip, port);

			// No sync, stand alone mode
			// TODO comunication and sync with other RPC server
			getResponse().success();
		} while (false);
	}

	@Override
	public boolean foundBadParameter() {
		return false;
	}

}
