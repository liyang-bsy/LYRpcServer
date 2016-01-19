package net.vicp.lylab.lyserver.timer;

import java.util.Calendar;
import java.util.Date;

import net.vicp.lylab.utils.timer.TimerJob;

public class HeartBeat extends TimerJob {
	
	private String serverName;
	
	@Override
	public Date getStartTime() {
		Calendar cl = Calendar.getInstance();
		cl.add(Calendar.SECOND, 30);
		return cl.getTime();
	}

	@Override
	public Integer getInterval() {
		return 1*MINUTE;
	}

	@Override
	public void exec() {
		// Do some thing to say "I'm alive!"
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

}
