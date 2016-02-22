package net.vicp.lylab.server.action.manager;

import java.util.Date;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.server.model.RPCBaseAction;
import net.vicp.lylab.server.rpc.RpcConnector;
import net.vicp.lylab.utils.Utils;

public class CheckRuntimeAction extends RPCBaseAction {

	@Override
	public void exec() {
		try {
			do {

				getResponse().getBody().put("Current time", Utils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
				RpcConnector connector = (RpcConnector) CoreDef.config.getConfig("Singleton").getObject("connector");
				getResponse().getBody().put("Current server map", connector.getServerAddrMap());
				getResponse().success();
			} while (false);
		} catch (Exception e) {
			log.error("Exception detected:" + Utils.getStringFromException(e));
		}
	}

	@Override
	public boolean foundBadParameter() {
		return false;
	}

}
