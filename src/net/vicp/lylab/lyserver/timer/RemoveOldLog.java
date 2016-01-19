package net.vicp.lylab.lyserver.timer;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.lyserver.utils.Logger;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.timer.TimerJob;

public class RemoveOldLog extends TimerJob {
	Logger logger;
	
	@Override
	public Date getStartTime() {
		Calendar cl = Calendar.getInstance();
		cl.set(Calendar.HOUR, 0);
		cl.set(Calendar.AM_PM, Calendar.AM);
		cl.set(Calendar.MINUTE, 30);
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
		for (String fn : Utils.getFileList(logger.getFilePath(), logger.getFileSuffix())) {
			File f = new File(fn);
			long time = f.lastModified();
			if (time < System.currentTimeMillis() - CoreDef.WEEK)
				f.delete();
		}
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

}
