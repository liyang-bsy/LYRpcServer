package client;

import net.vicp.lylab.core.model.SimpleHeartBeat;
import net.vicp.lylab.utils.client.RDMAClient;

/**
 * Close server only
 * 
 * @author Young
 *
 */
public class RDMARequest {

	public static void main(String[] args) {
		if (args.length <= 1) {
			System.out.println("Addresses?");
			return;
		}

		String[] hosts = args[0].split("\\,");

		for (String host : hosts) {
			RDMAClient caller = new RDMAClient();
			caller.setRdmaHost(host);
			caller.setRdmaPort(2000);
			caller.setHeartBeat(new SimpleHeartBeat());
			caller.initialize();

			caller.stop();
			caller.close();
		}
	}

}
