package net.vicp.lylab.lyserver.action.manager;

import java.util.Date;

import net.vicp.lylab.core.BaseAction;
import net.vicp.lylab.utils.Utils;

public class CheckRuntimeAction extends BaseAction {

	@Override
	public void exec() {
		try {
			do {
				response.getBody().put("当前时间", Utils.format(new Date(), "yyyy-MM-dd HH-mm:ss"));
				response.success();
			} while (false);
		} catch (Exception e) {
			log.error("Exception detected:" + Utils.getStringFromException(e));
		}
	}

}
