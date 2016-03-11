package client;

import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.client.RPCClient;
import net.vicp.lylab.utils.internet.protocol.LYLabProtocol;

public class RPCRequest {

	public static void main(String[] args) {
		if (args.length <= 1) {
			System.out.println("Addresses and commands?");
			return;
		}

		String[] rpcHosts = args[0].split("\\,");
		String rpcCmd = args[1];

		for (String rpcHost : rpcHosts) {
			RPCClient caller = new RPCClient();
			caller.setProtocol(new LYLabProtocol());
			caller.setRpcHost(rpcHost);
			caller.setRpcPort(2001);
			caller.setHeartBeat(new SimpleHeartBeat());
			caller.setBackgroundServer(false);
			caller.initialize();

			RPCMessage message = new RPCMessage();
			message.setRpcKey(rpcCmd);

			System.out.println(Utils.serialize(caller.callRpcServer(message)));
			caller.close();
		}
	}

}
