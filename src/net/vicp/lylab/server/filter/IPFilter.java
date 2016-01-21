package net.vicp.lylab.server.filter;

import java.net.Socket;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.utils.Utils;

public class IPFilter extends NonCloneableBaseObject implements Filter<Message, Message> {
	
	public Message doFilter(Socket socket, Message request) {
		String host = socket.getInetAddress().getHostAddress();
		
		if (request.getKey().startsWith("Privilege")) {
			if ("127.0.0.1".equals(host))
				return null;
			else
				throw new LYException("Remote access is forbidden for privilege actions");
		}
		if (Utils.inList(CoreDef.config.getString("ipWhiteList").split(","), host))
			return null;
		else
			throw new LYException("IP[" + host + "] is forbidden");
	}

	@Override
	public void initialize() {
	}

	@Override
	public void close() {
	}

}
