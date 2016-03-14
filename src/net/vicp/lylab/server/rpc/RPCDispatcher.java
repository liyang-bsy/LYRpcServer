package net.vicp.lylab.server.rpc;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.server.dispatcher.SimpleKeyDispatcher;
import net.vicp.lylab.server.utils.Logger;

public class RPCDispatcher extends SimpleKeyDispatcher<RPCMessage> {
	@Override
	protected void logger(RPCMessage request, Message response) {
//		System.out.println("Access key:" + request.getKey() + "\tAccess rpcKey:" + ((RPCMessage) request).getRpcKey()
//				+ "\nBefore:" + request + "\nAfter:" + response);
		((Logger) CoreDef.config.getConfig("Singleton").getObject("Logger"))
				.appendLine("Access key:" + request.getKey() + "\tAccess rpcKey:" + ((RPCMessage) request).getRpcKey()
						+ "\nBefore:" + request + "\nAfter:" + response);
	}

	@Override
	protected BaseAction mapAction(RPCMessage request) {
		try {
			return (BaseAction) actions.getNewInstance(request.getRpcKey() + "Action");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}