package client;

import java.util.ArrayList;
import java.util.List;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.controller.TimeoutController;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.ClientLongSocket;
import net.vicp.lylab.utils.internet.impl.LYLabProtocol;
import net.vicp.lylab.utils.internet.protocol.ProtocolUtils;
import net.vicp.lylab.utils.operation.KeepAliveValidator;

public class Client {
	static {
		new TimeoutController().initialize();
	}
	
	public static void main(String[] args) throws InterruptedException {
		CoreDef.config.reload("C:/config.txt");
		
		Protocol p = new LYLabProtocol();
		ProtocolUtils.setRawProtocols(new LYLabProtocol());
		List<ClientLongSocket> tsl = new ArrayList<>();
		AutoCreator<ClientLongSocket> creator = new InstanceCreator<ClientLongSocket>(
				ClientLongSocket.class, "127.0.0.1", 1234, p,
				new SimpleHeartBeat());
		AutoGeneratePool<ClientLongSocket> pool = new AutoGeneratePool<ClientLongSocket>(creator, new KeepAliveValidator<ClientLongSocket>(),
				20000, Integer.MAX_VALUE);

		ClientLongSocket ts = pool.accessOne();

		RPCMessage message = new RPCMessage();
		message.setKey("Register");
		message.getBody().put("server", CoreDef.config.getString("server"));
		message.getBody().put("procedures", CoreDef.config.getConfig("Aop").keyList());
		
		byte[] req, res;
		req = p.encode(message);
		res = ts.request(req);
		Message m = (Message) p.decode(res);
		System.out.println(m);
		
//		for (int i = 0; i < 50; i++)
//			tsl.add(pool.accessOne());
//		for (int i = 0; i < 50; i++) 
//			pool.recycle(tsl.get(i));
//		
//		tsl.clear();
//		
//		for (int i = 0; i < 50; i++) {
//			ClientLongSocket ts = pool.accessOne();
//			tsl.add(ts);
//			try {
//				Thread.sleep(20000);
//				byte[] req, res;
//				req = p.encode(message);
//				res = tsl.get(i).request(req);
//				Message m = (Message) p.decode(res);
//				message.getBody().put("int", m.getBody().get("int"));
//				System.out.println(m);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		pool.close();
	}
	
}
