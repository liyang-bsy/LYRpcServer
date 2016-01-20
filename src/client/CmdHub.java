package client;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.utils.rpc.client.RPCaller;

public class CmdHub {
	public static void main(String[] args) {
		CoreDef.config.reload("C:/config.txt");
		RPCaller caller = new RPCaller();
		caller.setBackgroundServer(false);
		caller.initialize();

		RPCMessage rpcMessage = new RPCMessage();
//		rpcMessage.setRpcKey("PrivilegeSwithModification");
//		rpcMessage.setKey("Inc");
//		rpcMessage.setServer("LYServer");
//		rpcMessage.getBody().put("int", 254);
//		System.out.println(caller.call(rpcMessage));
		
		rpcMessage = new RPCMessage();
		rpcMessage.setKey("Inc");
		rpcMessage.setServer("LYServer");
		rpcMessage.getBody().put("int", 254);
		System.out.println(caller.call(rpcMessage));
		
		caller.close();
	}
	
}
