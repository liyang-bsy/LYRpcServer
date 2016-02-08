package client;

import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.client.RPCClient;
import net.vicp.lylab.utils.internet.protocol.LYLabProtocol;

public class RPCHub {


	public static void main(String[] args) throws InterruptedException {
		
		RPCClient caller = new RPCClient();
		caller.setProtocol(new LYLabProtocol());
		caller.setRpcHost("127.0.0.1");
		caller.setRpcPort(2002);
		caller.setHeartBeat(new SimpleHeartBeat());
		caller.setBackgroundServer(false);
		caller.initialize();
		

		RPCMessage rpcMessage = new RPCMessage();

//		rpcMessage = new RPCMessage();
//		rpcMessage.setRpcKey("SyncServer");
//		System.out.println(caller.callRpcServer(rpcMessage));
		

		rpcMessage = new RPCMessage();
		rpcMessage.setKey("SetCache");
		rpcMessage.setServer("公共缓存服务");
		rpcMessage.getBody().put("int", 254);
		System.out.println(caller.call(rpcMessage,true));
		
		caller.close();
	}

}
