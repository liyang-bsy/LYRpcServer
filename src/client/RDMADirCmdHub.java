package client;

import java.text.DecimalFormat;

import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.CacheMessage;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicInteger;
import net.vicp.lylab.utils.atomic.AtomicLong;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.internet.protocol.CacheMessageProtocol;
import net.vicp.lylab.utils.tq.Task;

public class RDMADirCmdHub extends Task {
	private static final long serialVersionUID = -1319408007756814179L;

	public static AtomicInteger access = new AtomicInteger(0);
	public static AtomicLong total = new AtomicLong(0);
	protected static final Protocol p = new CacheMessageProtocol();

	String lastL, lastR;
	public void action() {
		SyncSession session = pool.accessOne();

		CacheMessage message = new CacheMessage();

		//--------------------------

		if(total.get() < 700000)
		{
		Pair<String, byte[]> pair = new Pair<>(lastL = Utils.createUUID(), (lastR = Utils.createUUID()).getBytes());
		
		message.setAction("Set");
		message.setPair(pair);
		}
		else
		{
			Pair<String, byte[]> pair = new Pair<>(lastL, "".getBytes());
			message.setAction("Get");
			message.setPair(pair);
		}
		//----------------------------
		session.send(p.encode(message));
		CacheMessage m = (CacheMessage) 
				p.decode(session.receive().getLeft());
//		System.out.println(m);
		if(message.getAction().equals("Get") && !new String(m.getPair().getRight()).equals(lastR))
			System.out.println("error:\n" + lastR + "\n" + new String(m.getPair().getRight()));
//		System.out.println(Arrays.toString(m.getPair().getRight()));
		pool.recycle(session);
	}

	static AutoGeneratePool<SyncSession> pool;

	public static void main(String[] args) throws InterruptedException {
//		CoreDef.config.reload("C:/config.txt");
		AutoCreator<SyncSession> creator = new InstanceCreator<>(SyncSession.class
				, "127.0.0.1", 2000, p, new SimpleHeartBeat());
		pool = new AutoGeneratePool<>(creator);
//		CoreDef.config.getInteger("thread")
		for (int i = 0; i < 4; i++)
			new RDMADirCmdHub().begin();
		// 稳定以后才开始进行计算
		Integer recalcTimeInteger = 0;
		boolean recalc = true;

		for (int j = 0; j < Integer.MAX_VALUE; j += 1) {
			access.set(0);
			Thread.sleep(1000);

			if (recalc && j > 8) {
				recalcTimeInteger = j;
				recalc = false;
				total.set(0L);
				System.out.println("recalc");
			}
			System.out.println("second:" + j + "\t\ttotal:" + total.get() + "\t\taverage:"
					+ new DecimalFormat("0.00").format(1.0 * total.get() / (j - recalcTimeInteger)));
			System.out.println("access:" + access.get());
		}
	}

	@Override
	public void exec() {
		while (!isStopped()) {
			try {
				action();
				access.incrementAndGet();
				total.incrementAndGet();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
