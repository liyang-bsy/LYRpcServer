package net.vicp.lylab.server.action.rpc;

import java.util.List;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.server.action.manager.SwithModificationAction;
import net.vicp.lylab.server.rpc.RpcConnector;

public class ModifyProcedureAction extends BaseAction {

	@Override
	public void exec() {
		do {
			if (SwithModificationAction.isChangeable()) {
				getResponse().fail("Current mode is unchangeable");
				break;
			}
			String ip = clientSocket.getInetAddress().getHostAddress();
			int port = clientSocket.getLocalPort();
			@SuppressWarnings("unchecked")
			List<String> procedures = (List<String>) getRequest().getBody().get("procedures");
			String server = (String) getRequest().getBody().get("server");

			RpcConnector connector = (RpcConnector) CoreDef.config.getConfig("Singleton").getObject("connector");

			List<Pair<String, Integer>> addr = connector.getAllAddress(server);
			if (!addr.contains(new Pair<>(ip, port))) {
				getResponse().fail("Background server can only modify itself");
				break;
			}
			connector.addProcedures(server, procedures);

			getResponse().success();
		} while (false);
	}

}
