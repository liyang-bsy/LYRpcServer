package client;

import java.text.DecimalFormat;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.atomic.AtomicInteger;
import net.vicp.lylab.utils.client.RPCClient;
import net.vicp.lylab.utils.internet.protocol.LYLabProtocol;
import net.vicp.lylab.utils.tq.Task;

public class RPCCmdHub extends Task {
	private static final long serialVersionUID = -1319408007756814179L;

	public static AtomicInteger access = new AtomicInteger(0);
	public static AtomicInteger accessF = new AtomicInteger(0);
	public static AtomicInteger accessE = new AtomicInteger(0);
	public static AtomicInteger total = new AtomicInteger(0);

	public boolean action() {
		RPCMessage rpcMessage = new RPCMessage();

		rpcMessage = new RPCMessage();
		rpcMessage.setKey("Inc");
		rpcMessage.setServer("LYServer");
		rpcMessage.getBody().put("int", 254);
		Message m = caller.call(rpcMessage);
		if((Integer)m.getBody().get("int") != 255)
		{
			System.out.println(m);
			return false;
		}
		return true;
	}

	static RPCClient caller;

	public static void main(String[] args) throws InterruptedException {
		CoreDef.config.reload("C:/config.txt");
		
		caller = new RPCClient();
		caller.setProtocol(new LYLabProtocol());
//		caller.setRpcHost("127.0.0.1");
		caller.setRpcHost(CoreDef.config.getString("rpcHost"));
		caller.setRpcPort(2001);
		caller.setHeartBeat(new SimpleHeartBeat());
		caller.setBackgroundServer(false);
		caller.initialize();
//		CoreDef.config.getInteger("thread")
		for (int i = 0; i < CoreDef.config.getInteger("thread"); i++)
			new RPCCmdHub().begin();
		// 稳定以后才开始进行计算
		Integer recalcTimeInteger = 0;
		boolean recalc = true;

		for (int j = 0; j < Integer.MAX_VALUE; j += 1) {
			access.set(0);
			accessF.set(0);
			accessE.set(0);
			Thread.sleep(1000);

			if (recalc && j > 8) {
				recalcTimeInteger = j;
				recalc = false;
				total.set(0);
				System.out.println("recalc");
			}
			System.out.println("second:" + j + "\t\ttotal:" + total.get() + "\t\taverage:"
					+ new DecimalFormat("0.00").format(1.0 * total.get() / (j - recalcTimeInteger)));
			System.out.println("access:" + access.get() + "\taccessF:" + accessF.get() + "\taccessE:" + accessE.get());
		}
	}

	@Override
	public void exec() {
		while (!isStopped()) {
			try {
				if (action())
					access.incrementAndGet();
				else
					accessF.incrementAndGet();
			} catch (Throwable e) {
				accessE.incrementAndGet();
			}
			total.incrementAndGet();
		}
	}

}
