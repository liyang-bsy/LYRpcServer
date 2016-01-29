package client;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.atomic.AtomicInteger;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.internet.protocol.LYLabProtocol;
import net.vicp.lylab.utils.tq.Task;

public class DirCmdHub extends Task {
	private static final long serialVersionUID = -1319408007756814179L;

	public static AtomicInteger access = new AtomicInteger(0);
	public static AtomicInteger total = new AtomicInteger(0);
	protected Protocol p = new LYLabProtocol();

	public void action() {
		SyncSession session = pool.accessOne();
		Message message = new Message();

		//--------------------------
//		message.setKey("Inc");
//		message.getBody().put("int", 254);
		String dataStr = Utils.createUUID();
		String keyStr = Utils.createUUID();
		
		HashMap<Object, Object> data = new HashMap<>();
		data.put("str", dataStr);
		data.put("int", 2);
		
		message.setKey("SetCache");
		message.getBody().put("server", "Test");
		message.getBody().put("module", "rpc_test");
		message.getBody().put("key", keyStr);
		message.getBody().put("data", data);
		//----------------------------

		session.send(p.encode(message));
		Message m = (Message) p.decode(session.receive().getLeft());
		

		
		
		message.setKey("GetCache");
		message.getBody().put("server", "Test");
		message.getBody().put("module", "rpc_test");
		message.getBody().put("key", keyStr);
		
		
		

		session.send(p.encode(message));
		m = (Message) p.decode(session.receive().getLeft());
		String value = (String) ((Map) m.getBody().get("data")).get("str");
		if(!value.equals(dataStr))
			System.out.println("bad exists");
//		System.out.println(uuid + "\n" + m);
		
		pool.recycle(session);
	}

	static AutoGeneratePool<SyncSession> pool;

	public static void main(String[] args) throws InterruptedException {
		CoreDef.config.reload("C:/config.txt");
		AutoCreator<SyncSession> creator = new InstanceCreator<>(SyncSession.class
				, "127.0.0.1", 2000, new LYLabProtocol(), new SimpleHeartBeat());
		pool = new AutoGeneratePool<>(creator);
//		CoreDef.config.getInteger("thread")
		for (int i = 0; i < 8; i++)
			new DirCmdHub().begin();
		// 稳定以后才开始进行计算
		Integer recalcTimeInteger = 0;
		boolean recalc = true;

		for (int j = 0; j < Integer.MAX_VALUE; j += 1) {
			access.set(0);
			Thread.sleep(1000);

			if (recalc && j > 8) {
				recalcTimeInteger = j;
				recalc = false;
				total.set(0);
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
			} catch (Throwable e) {
			}
			access.incrementAndGet();
			total.incrementAndGet();
		}
	}

}
