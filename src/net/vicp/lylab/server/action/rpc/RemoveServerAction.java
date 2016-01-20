package net.vicp.lylab.server.action.rpc;

import java.util.List;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.server.rpc.RpcConnector;

public class RemoveServerAction extends BaseAction {

	@Override
	public void exec() {
		String ip = clientSocket.getInetAddress().getHostAddress();
		int port = clientSocket.getLocalPort();
		@SuppressWarnings("unchecked")
		List<String> procedures = (List<String>) getRequest().getBody().get("procedures");
		String server = (String) getRequest().getBody().get("server");

		RpcConnector connector = (RpcConnector) CoreDef.config.getConfig("Singleton").getObject("connector");

		connector.addServer(server, ip, port);
		connector.addProcedures(server, procedures);
		
	}

}
