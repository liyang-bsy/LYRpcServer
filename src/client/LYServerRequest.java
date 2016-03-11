package client;

import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.internet.protocol.LYLabProtocol;

public class LYServerRequest {

	public static void main(String[] args) {
		if (args.length <= 1) {
			System.out.println("Addresses and commands?");
			return;
		}

		String[] hosts = args[0].split("\\,");
		String cmd = args[1];

		for (String host : hosts) {
			SyncSession session = new SyncSession(host, 2011, new LYLabProtocol(), new SimpleHeartBeat());
			session.initialize();

			Message message = new Message();
			message.setKey(cmd);

			session.send(message);
			Message retm = (Message) new LYLabProtocol().decode(session.receive().getLeft());
			
			System.out.println(Utils.serialize(retm));
			session.close();
		}
	}

}
