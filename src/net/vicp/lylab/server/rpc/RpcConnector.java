package net.vicp.lylab.server.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.InetAddr;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.operation.KeepAliveValidator;

public class RpcConnector extends NonCloneableBaseObject {
	//					Server		Address
	protected final Map<String, List<InetAddr>> server2addr;
	//						ip				Socket Pool
	protected final Map<InetAddr, AutoGeneratePool<SyncSession>> addr2ConnectionPool;
	
	// for random access
	protected transient final Random random = new Random();

	public RpcConnector() {
		server2addr = new HashMap<>();
		addr2ConnectionPool = new HashMap<>();
	}
	
	public boolean sync() {
		// TODO
//		Map<String, List<InetAddr>> server2addr = null;
//		if(server2addr == null)
//			return false;
		return true;
	}

	private SyncSession getConnection(String ip, int port) {
		AutoGeneratePool<SyncSession> pool = addr2ConnectionPool.get(InetAddr.fromInetAddr(ip, port));
		if (pool == null)
			throw new LYException("Connection pool is null for ip:" + ip);
		return pool.accessOne();
	}

	private void returnConnection(SyncSession socket) {
		returnConnection(socket, false);
	}
	
	private void returnConnection(SyncSession socket, boolean isBad) {
		if (socket == null)
			throw new NullPointerException("Parameter socket is null");
		AutoGeneratePool<SyncSession> pool = addr2ConnectionPool.get(socket.getPeer());
		if (pool == null)
			throw new LYException("Connection pool is null for addr:" + socket.getPeer());
		pool.recycle(socket, isBad);
	}

	public Message request(String ip, int port, RPCMessage request) {
		if (request == null)
			throw new NullPointerException("Parameter request is null");

		Protocol p = (Protocol) CoreDef.config.getObject("protocol");
		byte[] nextReq = p.encode(request);
		int torelent = 5;
		do {
			SyncSession socket = getConnection(ip, port);
			byte[] response = null;
			try {
				socket.send(nextReq);
				response = socket.receive().getLeft();
			} catch (Exception e) {
				returnConnection(socket, true);
				continue;
			}
			returnConnection(socket);
			return (Message) p.decode(response);
		} while (torelent-- > 0);
//		removeServer(request.getServer(), ip);
		// TODO fatal report
		log.fatal("Maximun torelent is reached, socket request failed, server[" + request.getServer() + "] on " + ip + " is down.");
		throw new LYException("Maximun torelent is reached, socket request failed, server[" + request.getServer() + "] on " + ip + " is down.");
	}

	public List<InetAddr> getOneRandomAddress(String server) {//, String procedure) {
		List<InetAddr> retList = new ArrayList<>();
		List<InetAddr> addrList = server2addr.get(server);
		if (addrList == null)
			throw new LYException("No such server:" + server);
		int seq = random.nextInt(addrList.size());
		retList.add(addrList.get(seq));
		return retList;
	}

	public List<InetAddr> getAllAddress(String server) {//, String procedure) {
		List<InetAddr> addrList = server2addr.get(server);
		if (addrList == null)
			throw new LYException("No such server:" + server);
		return addrList;
	}

	public void addServer(String server, String ip, int port) {
		synchronized (lock) {
			InetAddr addr = InetAddr.fromInetAddr(ip, port);
			if (server2addr.get(server) == null)
				server2addr.put(server, new ArrayList<InetAddr>());
			List<InetAddr> list = server2addr.get(server);
			if (!list.contains(addr))
				server2addr.get(server).add(addr);

			AutoGeneratePool<SyncSession> pool = addr2ConnectionPool.get(addr);
			if (pool == null) {
				AutoCreator<SyncSession> creator = new InstanceCreator<SyncSession>(
						SyncSession.class, ip, port, CoreDef.config.getObject("protocol"),
						CoreDef.config.getObject("heartBeat"));
				pool = new AutoGeneratePool<SyncSession>(creator, new KeepAliveValidator<SyncSession>(),
						20000, Integer.MAX_VALUE);
				addr2ConnectionPool.put(addr, pool);
			}
		}
	}

	public void removeServer(String server, String ip) {
		synchronized (lock) {
			List<InetAddr> list = server2addr.remove(server);

			AutoGeneratePool<SyncSession> pool = null;
			for (InetAddr addr : list) {
				if (!addr.getIp().equals(ip)) {
					continue;
				}
				pool = addr2ConnectionPool.remove(ip);
				break;

			}
			if (pool != null)
				pool.close();
		}
	}

}
