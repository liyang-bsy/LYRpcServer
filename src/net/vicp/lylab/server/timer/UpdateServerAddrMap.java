package net.vicp.lylab.server.timer;

import java.util.Calendar;
import java.util.Date;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.server.rpc.RpcConnector;
import net.vicp.lylab.utils.timer.TimerJob;

public class UpdateServerAddrMap extends TimerJob {
	
	private String serverName;
	
	@Override
	public Date getStartTime() {
		Calendar cl = Calendar.getInstance();
		cl.set(Calendar.HOUR, 0);
		cl.set(Calendar.AM_PM, Calendar.AM);
		cl.set(Calendar.MINUTE, 45);
		cl.set(Calendar.SECOND, 0);
		cl.set(Calendar.MILLISECOND, 0);
		while(cl.getTime().before(new Date()))
			cl.add(Calendar.DAY_OF_YEAR, 1);
		return cl.getTime();
	}

	@Override
	public Integer getInterval() {
		return DAY;
	}

	@Override
	public void exec() {
		RpcConnector connector = (RpcConnector) CoreDef.config.getConfig("Singleton").getObject("connector");
		connector.sync();
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

}
