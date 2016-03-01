package client;

import java.text.DecimalFormat;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicLong;
import net.vicp.lylab.utils.client.RPCClient;
import net.vicp.lylab.utils.internet.protocol.LYLabProtocol;
import net.vicp.lylab.utils.tq.Task;

public class RPCCmdHub extends Task {
	private static final long serialVersionUID = -1319408007756814179L;

	public static AtomicLong access = new AtomicLong(0);
	public static AtomicLong accessF = new AtomicLong(0);
	public static AtomicLong accessE = new AtomicLong(0);
	public static AtomicLong total = new AtomicLong(0);
	public static AtomicLong totalF = new AtomicLong(0);
	public static AtomicLong totalE = new AtomicLong(0);

	public Message action(RPCMessage rpcMessage) {
		rpcMessage.setKey("Inc");
		rpcMessage.setServer("LYServer");
		rpcMessage.getBody().put("int", 254);
		Message m = caller.call(rpcMessage);
		if ((Integer) m.getBody().get("int") != 255) {
			return null;
		}
		return m;
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
		for (int i = 0; i < CoreDef.config.getInteger("thread"); i++) {
			Thread.sleep(CoreDef.config.getInteger("sleep"));
			new RPCCmdHub().begin();
		}
		// 稳定以后才开始进行计算
		Long recalcTimeInteger = 0l;
		boolean recalc = true;

		for (long j = 0; j < Long.MAX_VALUE; j += 1) {
			access.set(0l);
			accessF.set(0l);
			accessE.set(0l);
			Thread.sleep(1000);

			if (recalc && j > 8) {
				recalcTimeInteger = j;
				recalc = false;
				total.set(0l);
				totalF.set(0l);
				totalE.set(0l);
				System.out.println("recalc");
			}
			System.out.println("second:" + j + "\ttotal:" + total.get() + "\t\taverage:"
					+ new DecimalFormat("0.00").format(1.0 * total.get() / (j - recalcTimeInteger)));
			System.out.println("\t\ttotalF:" + totalF.get() + "\t\ttotalE:" + totalE.get());
			System.out.println("access:" + access.get() + "\taccessF:" + accessF.get() + "\taccessE:" + accessE.get());
		}
	}

	@Override
	public void exec() {
		Message m = null;
		while (!isStopped()) {
			try {
				Thread.sleep(CoreDef.config.getInteger("sleep"));
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			try {
				m = action(new RPCMessage());
				if (m != null)
				{
					access.incrementAndGet();
					total.incrementAndGet();
				}
				else
				{
					accessF.incrementAndGet();
					totalF.incrementAndGet();
				}
			} catch (Throwable e) {
				log.error(Utils.getStringFromThrowable(e));
				accessE.incrementAndGet();
				totalE.incrementAndGet();
			}
		}
	}

}
