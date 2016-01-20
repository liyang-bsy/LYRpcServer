package net.vicp.lylab.server.rpc;

import java.net.Socket;

import org.apache.commons.lang3.StringUtils;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.Aop;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.server.aop.SimpleKeyDispatcherAop;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.server.utils.Logger;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.HeartBeat;

public class RPCDispatcherAop extends SimpleKeyDispatcherAop implements Aop {
	
	@Override
	public byte[] doAction(Socket client, byte[] requestByte, int offset) {
		RPCMessage request = null;

		String key = null;
		BaseAction action = null;
		Message response = new Message();
		try {
			do {
				try {
					Object obj = protocol.decode(requestByte, offset);
					if(obj instanceof HeartBeat)
						return protocol.encode(obj);
					request = (RPCMessage) obj;
				} catch (Exception e) {
					log.debug(Utils.getStringFromException(e));
				}
				if(request == null) {
					response.setCode(0x00000001);
					response.setMessage("RPCMessage not found");
					break;
				}
				// do start filter
				if (filterChain != null && filterChain.size() != 0)
					for (Filter filter : filterChain) {
						Message ret = null;
						if ((ret = filter.doFilter(client, request)) != null)
							return protocol.encode(ret);
					}
				response.copyBasicInfo(request);
				
				// gain key from rpcReq
				key = request.getRpcKey();
				if (StringUtils.isBlank(key)) {
					response.setCode(0x00000002);
					response.setMessage("Rpc key not found");
					break;
				}
				else if("RPC".equals(key)) {
					// check server from request
					if (StringUtils.isBlank(request.getServer())) {
						response.setCode(0x00000101);
						response.setMessage("Server is blank");
						break;
					}
					// check procedure from request
					if (StringUtils.isBlank(request.getKey())) {
						response.setCode(0x00000102);
						response.setMessage("Foreign key not found");
						break;
					}
				}
				// get action related to key
				try {
					action = (BaseAction) CoreDef.config.getConfig("Aop").getNewInstance(key + "Action");
				} catch (Exception e) { }
				if (action == null) {
					response.setCode(0x00000003);
					response.setMessage("Action not found");
					break;
				}
				// Initialize action
				action.setSocket(client);
				action.setRequest(request);
				action.setResponse(response);
				// execute action
				try {
					action.exec();
				} catch (Throwable t) {
					String reason = Utils.getStringFromThrowable(t);
					log.error(reason);
					response.setCode(0x00000004);
					response.setMessage("Action exec failed:" + reason);
					break;
				}
			} while (false);
		} catch (Exception e) {
			log.error(Utils.getStringFromException(e));
		}
		// to logger
		System.out.println("Access key:" + key  + "\nBefore:" + request + "\nAfter:" + response);
		((Logger) CoreDef.config.getConfig("Singleton").getObject("Logger")).appendLine(
				"Access key:" + key  + "\nBefore:" + request + "\nAfter:" + response);
		return protocol.encode(response);
	}

}