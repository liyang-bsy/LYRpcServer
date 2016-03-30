package net.vicp.lylab.server.action.rpc;

import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.model.InetAddr;
import net.vicp.lylab.server.action.manager.LockAction;
import net.vicp.lylab.server.core.RPCBaseAction;
import net.vicp.lylab.server.rpc.RpcConnector;
import net.vicp.lylab.utils.Utils;

public class RemoveServerAction extends RPCBaseAction {

	@Override
	public void exec() {
		do {
			if (!LockAction.isChangeable()) {
				getResponse().fail(0x00010002, "Current mode is unchangeable");
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

			System.out.println(Utils.getPeer(clientSocket) + " requested RemoveServerAction from [" + server + "," + ip + ":" + port + "]");
			getResponse().success();
		} while (false);
	}

	@Override
	public boolean foundBadParameter() {
		return false;
	}

}
