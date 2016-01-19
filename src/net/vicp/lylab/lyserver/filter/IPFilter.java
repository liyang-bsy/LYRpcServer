package net.vicp.lylab.lyserver.filter;

import java.net.Socket;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.server.filter.Filter;
import net.vicp.lylab.utils.Utils;

public class IPFilter extends NonCloneableBaseObject implements Filter {
	
	public Message doFilter(Socket socket, Message request) {
		String host = socket.getInetAddress().getHostAddress();
		
		if(request.getKey().equals("Stop") && "127.0.0.1".equals(host)) return null;
		if(Utils.inList(CoreDef.config.getString("ipWhiteList").split(","), host)) return null;
		throw new LYException("IP[" + host + "] is forbidden");
	}

	@Override
	public void initialize() {
	}

	@Override
	public void close() throws Exception {
	}

}
