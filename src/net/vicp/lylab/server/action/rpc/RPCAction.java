package net.vicp.lylab.server.action.rpc;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.core.model.RPCBaseAction;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.server.rpc.RpcConnector;
import net.vicp.lylab.utils.Utils;

/**
 * Please configure it into action list, so that RCPDispatcherAop may found this action.
 * 
 * @author Young
 *
 */
public class RPCAction extends RPCBaseAction {

	@Override
	public void exec() {
		do {
			RpcConnector connector = (RpcConnector) CoreDef.config.getConfig("Singleton").getObject("connector");

			RPCMessage req = getRequest();
			List<Pair<String, Integer>> addrList = null;
			try {
				if (req.isBroadcast()) {
					addrList = connector.getAllAddress(req.getServer(), req.getProcedure());
				} else {
					addrList = connector.getOneRandomAddress(req.getServer(), req.getProcedure());
				}
			} catch (Exception e) {
				getResponse().fail("Access rpc server list faild:" + Utils.getStringFromException(e));
			}
			List<Pair<String, Message>> result = new ArrayList<>();
			for (Pair<String, Integer> addr : addrList) {
				Message message = connector.request(addr.getLeft(), addr.getRight(), req);
				result.add(new Pair<>(addr.getLeft(), message));
			}
			getResponse().getBody().put("BraodCastResult", result);
			getResponse().success();
		} while (false);
	}

}
