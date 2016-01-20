package net.vicp.lylab.server.action.rpc;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.server.rpc.connector.ServerConnector;
import net.vicp.lylab.server.rpc.connector.ServerDispathcer;
import net.vicp.lylab.utils.Utils;

/**
 * Please configure it into action list, so that RCPDispatcherAop may found this action.
 * 
 * @author Young
 *
 */
public class RPCAction extends BaseAction {

	@Override
	public void exec() {
		// TODO
		ServerDispathcer serverDispathcer = new ServerDispathcer();
		ServerConnector serverConnector = new ServerConnector();
		try {
			RPCMessage req = (RPCMessage) getRequest();
			if (req.isBroadcast()) {
				List<Pair<String, Integer>> addrList = serverDispathcer.getAllAddress(req.getServer(), req.getProcedure());
				List<Pair<String, Message>> result = new ArrayList<>();
				for (Pair<String, Integer> addr : addrList) {
					Message message = serverConnector.request(addr.getLeft(), addr.getRight(), req);
					result.add(new Pair<>(addr.getLeft(), message));
				}
				getResponse().getBody().put("BraodCastResult", result);
			} else {
				Pair<String, Integer> addr = serverDispathcer.getRandomAddress(req.getServer(), req.getProcedure());
				Message message = serverConnector.request(addr.getLeft(), addr.getRight(), req);
				setResponse(message);
			}
		} catch (Throwable t) {
			log.fatal(Utils.getStringFromThrowable(t));
		}
	}

}
