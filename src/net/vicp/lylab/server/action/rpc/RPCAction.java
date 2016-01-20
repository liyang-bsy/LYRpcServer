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
 * Please configure it into action list, so that RCPDispatcherAop may found this
 * action.
 * 
 * @author Young
 *
 */
public class RPCAction extends RPCBaseAction {

	@Override
	public void exec() {
		RpcConnector connector = (RpcConnector) CoreDef.config.getConfig("Singleton").getObject("connector");

		RPCMessage req = getRequest();
		List<Pair<String, Integer>> addrList = new ArrayList<>();
		List<Pair<String, Message>> result = new ArrayList<>();
		retry: while (true) {
			try {
				if (req.isBroadcast()) {
					addrList = connector.getAllAddress(req.getServer());
				} else {
					addrList = connector.getOneRandomAddress(req.getServer());
				}
			} catch (Exception e) {
				getResponse().fail("Access rpc server list faild:" + Utils.getStringFromException(e));
				return;
			}
			for (Pair<String, Integer> addr : addrList) {
				Message message = new Message();
				try {
					message = connector.request(addr.getLeft(), addr.getRight(), req);
				} catch (Exception e) {
					if (req.isBroadcast())
						message.setMessage("Unreachable server:" + addr);
					else
						continue retry;
				}
				result.add(new Pair<>(addr.getLeft(), message));
			}
			break;
		}
		getResponse().getBody().put("BraodCastResult", result);
		getResponse().success();
	}

}
