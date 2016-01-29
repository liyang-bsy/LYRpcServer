package client;

import java.util.HashMap;

import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.internet.protocol.LYLabProtocol;

public class OneTimeCmdHub {


	public static void main(String[] args) throws InterruptedException {
//		CoreDef.config.reload("C:/config.txt");
		Protocol p = new LYLabProtocol();
		SyncSession session = new SyncSession("127.0.0.1", 2000, p, new SimpleHeartBeat());
		Message message = new Message();

		//--------------------------
//		message.setKey("Inc");
//		message.getBody().put("int", 254);
		
		HashMap<Object, Object> data = new HashMap<>();
		data.put("str", Utils.createUUID());
		data.put("int", 2);
		
		message.setKey("CheckRuntime");
		message.getBody().put("server", "Test");
		message.getBody().put("module", "rpc_test");
		message.getBody().put("key", "c96a5e29d8b746b5862db1999d3f000e");
		//----------------------------

		session.send(p.encode(message));
		Message m = (Message) p.decode(session.receive().getLeft());
		System.out.println(m);
		session.close();
	}

}
