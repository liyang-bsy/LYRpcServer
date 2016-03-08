package net.vicp.lylab.server.action.rpc;

import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.model.InetAddr;
import net.vicp.lylab.server.action.manager.SwithModificationAction;
import net.vicp.lylab.server.model.RPCBaseAction;
import net.vicp.lylab.server.rpc.RpcConnector;
import net.vicp.lylab.utils.Utils;

public class RemoveServerAction extends RPCBaseAction {

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

			List<InetAddr> addr = connector.getAllAddress(server);
			if (!addr.contains(InetAddr.fromInetAddr(ip, port))) {
				getResponse().fail("Background server can only remove itself");
				break;
			}
			connector.syncRemoveServer(server, ip, port);

			System.out.println(Utils.getPeer(clientSocket) + " requested RemoveServerAction from [" + server + "]");
			getResponse().success();
		} while (false);
	}

	@Override
	public boolean foundBadParameter() {
		return false;
	}

}
