package net.vicp.lylab.server.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.Pair;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.ClientLongSocket;
import net.vicp.lylab.utils.operation.KeepAliveValidator;

public class RpcConnector extends NonCloneableBaseObject {
	//					Server	Procedure
	protected final Map<String, Set<String>> server2procedure;
	//					Server		Address:ip	port
	protected final Map<String, List<Pair<String, Integer>>> server2addr;
	//				ip				Socket
	protected final Map<String, AutoGeneratePool<ClientLongSocket>> ip2ConnectionPool;
	//				ip				creators
//	protected Map<String, AutoCreator<ClientLongSocket>> ip2Creator;
	
	// random access
	protected transient final Random random = new Random();
	
	// mode
	protected boolean restrict = true;

	public RpcConnector() {
		server2procedure = new HashMap<>();
		server2addr = new HashMap<>();
		ip2ConnectionPool = new HashMap<>();
//		ip2Creator = new HashMap<>();
	}

	private ClientLongSocket getConnection(String ip, int port) {
		AutoGeneratePool<ClientLongSocket> pool = ip2ConnectionPool.get(ip);
		if (pool == null)
			throw new LYException("Connection pool is null for ip:" + ip);
		return pool.accessOne();
	}

	private void returnConnection(ClientLongSocket socket) {
		if (socket == null)
			throw new NullPointerException("Parameter socket is null");
		AutoGeneratePool<ClientLongSocket> pool = ip2ConnectionPool.get(socket.getHost());
		if (pool == null)
			throw new LYException("Connection pool is null for ip:" + socket.getHost());
			pool.recycle(socket);
	}

	public Message request(String ip, int port, RPCMessage request) {
		if (request == null)
			throw new NullPointerException("Parameter request is null");

		ClientLongSocket socket = getConnection(ip, port);
		Protocol p = socket.getProtocol();

		byte[] nextReq = p.encode(request.getMessage());
		int torelent = 10;
		do {
			byte[] response = null;
			try {
				response = socket.request(nextReq);
			} catch (Exception e) {
				continue;
			}
			returnConnection(socket);
			return (Message) p.decode(response);
		} while (torelent-- > 0);
		throw new LYException("Maximun torelent is reached, socket request failed.");
	}

	public List<Pair<String, Integer>> getOneRandomAddress(String server, String procedure) {
		List<Pair<String, Integer>> retList = new ArrayList<>();
		List<Pair<String, Integer>> addrList = server2addr.get(server);
		if (addrList == null)
			throw new LYException("No such server:" + server);
		if (restrict) {
			Set<String> procedures = server2procedure.get(server);
			if (procedures == null)
				throw new LYException("No such server:" + server);
			if (!procedures.contains(procedure))
				throw new LYException("No such procedure:" + procedure);
		}
		int seq = random.nextInt(addrList.size());
		retList.add(addrList.get(seq));
		return retList;
	}

	public List<Pair<String, Integer>> getAllAddress(String server, String procedure) {
		List<Pair<String, Integer>> addrList = server2addr.get(server);
		if (addrList == null)
			throw new LYException("No such server:" + server);
		if (restrict) {
			Set<String> procedures = server2procedure.get(server);
			if (procedures == null)
				throw new LYException("No such server:" + server);
			if (!procedures.contains(procedure))
				throw new LYException("No such procedure:" + procedure);
		}
		return addrList;
	}

	public void addServer(String server, String ip, int port) {
		synchronized (lock) {
			addServer(server);
			server2addr.get(server).add(new Pair<>(ip, port));

			AutoGeneratePool<ClientLongSocket> pool = ip2ConnectionPool.get(ip);
			if (pool == null) {
				AutoCreator<ClientLongSocket> creator = new InstanceCreator<ClientLongSocket>(
						ClientLongSocket.class, ip, port, CoreDef.config.getObject("protocol"),
						CoreDef.config.getObject("heartBeat"));
				pool = new AutoGeneratePool<ClientLongSocket>(creator, new KeepAliveValidator<ClientLongSocket>(),
						20000, Integer.MAX_VALUE);
//				ip2Creator.put(ip, creator);
				ip2ConnectionPool.put(ip, pool);
			}
		}
	}

	private void addServer(String server) {
		synchronized (lock) {
			if (server2addr.get(server) == null)
				server2addr.put(server, new ArrayList<Pair<String, Integer>>());
			if (server2procedure.get(server) == null)
				server2procedure.put(server, new HashSet<String>());
		}
	}

	public void addProcedures(String server, Collection<String> procedures) {
		synchronized (lock) {
			if (server2addr.containsKey(server)) {
				Iterator<String> iterator = procedures.iterator();
				while (iterator.hasNext())
					server2procedure.get(server).add(iterator.next());
			}
		}
	}

	public void removeServer(String server, String ip) {
		synchronized (lock) {
			server2addr.remove(server);
			server2procedure.remove(server);
		}
	}

	public void removeProcedure(String server, String procedure) {
		synchronized (lock) {
			if (server2addr.containsKey(server)) {
				server2procedure.containsValue(server);
				Set<String> procedures = server2procedure.get(server);
				procedures.remove(procedure);
			}
		}
	}
	
}
