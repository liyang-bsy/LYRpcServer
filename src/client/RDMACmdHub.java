package client;

import java.util.Arrays;

import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.CacheMessage;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.internet.protocol.CacheMessageProtocol;

public class RDMACmdHub {


	public static void main(String[] args) throws InterruptedException {
//		CoreDef.config.reload("C:/config.txt");
		Protocol p = new CacheMessageProtocol();
		SyncSession session = new SyncSession("127.0.0.1", 2000, p, new SimpleHeartBeat());
		CacheMessage message = new CacheMessage();

		//--------------------------
		Pair<String, byte[]> pair = new Pair<>("RPCBackgroundServerMap_server2addr",new byte[0]);
		
		message.setAction("Get");
		message.setPair(pair);
		//----------------------------

		session.send(p.encode(message));
		CacheMessage m = (CacheMessage) p.decode(session.receive().getLeft());
		System.out.println(m);
		System.out.println(Arrays.toString(m.getPair().getRight()));
		System.out.println(new String(m.getPair().getRight()));
		session.close();
	}

}
