package net.vicp.lylab.server.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.exceptions.LYException;
import net.vicp.lylab.core.interfaces.Initializable;
import net.vicp.lylab.core.interfaces.Protocol;
import net.vicp.lylab.core.model.InetAddr;
import net.vicp.lylab.core.model.Message;
import net.vicp.lylab.core.model.RPCMessage;
import net.vicp.lylab.core.pool.AutoGeneratePool;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.client.RDMAClient;
import net.vicp.lylab.utils.creator.AutoCreator;
import net.vicp.lylab.utils.creator.InstanceCreator;
import net.vicp.lylab.utils.internet.SyncSession;
import net.vicp.lylab.utils.operation.KeepAliveValidator;

import org.apache.commons.lang3.StringUtils;

public class RpcConnector extends NonCloneableBaseObject implements Initializable {
	//					Server		Address
	protected Map<String, List<InetAddr>> server2addr;
	//						ip				Socket Pool
	protected Map<InetAddr, AutoGeneratePool<SyncSession>> addr2ConnectionPool;
	
	private RDMAClient rdmaClient;
	
	// for random access
	protected transient final Random random = new Random();

	public RpcConnector() {
		server2addr = new ConcurrentHashMap<>();
		addr2ConnectionPool = new ConcurrentHashMap<>();
	}
	
	// Sync when initialize
	@Override
	public void initialize() {
		sync();
	}

	// Optimistic Locking
	public void syncAddServer(String server, String ip, int port) {
		synchronized (lock) {
			while (true) {
				Map<String, List<InetAddr>> server2addr = syncGet();
				addServer(server, ip, port);
				if (!syncSet(server2addr))
					continue;
				break;
			}
		}
	}

	// Optimistic Locking
	public void syncRemoveServer(String server, String ip, int port) {
		synchronized (lock) {
			while (true) {
				Map<String, List<InetAddr>> server2addr = syncGet();
				removeServer(server, ip, port);
				if (!syncSet(server2addr))
					continue;
				break;
			}
		}
	}

	public void sync() {
		syncGet();
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, List<InetAddr>> syncGet() {
		synchronized (lock) {
			try {
				byte[] data = rdmaClient.get("RPCBackgroundServerMap_" + "server2addr");
				RpcConnector tempConnector = new RpcConnector();
				String json = new String(data, CoreDef.CHARSET());
				if (StringUtils.isBlank(json))
					return null;
				Map<String, Object> map = Utils.deserialize(Map.class, json);

				for (String server : map.keySet()) {
					List<Map<String, Object>> items = (List<Map<String, Object>>) map.get(server);
					for (Map<String, Object> item : items)
						tempConnector.addServer(server, (String) item.get("ip"), (int) item.get("port"));
				}
				Map<String, List<InetAddr>> temp = this.server2addr;
				Map<InetAddr, AutoGeneratePool<SyncSession>> tempPool = this.addr2ConnectionPool;
				this.server2addr = tempConnector.server2addr;
				this.addr2ConnectionPool = tempConnector.addr2ConnectionPool;

				Set<Entry<InetAddr, AutoGeneratePool<SyncSession>>> entries = tempPool.entrySet();
				for (Entry<InetAddr, AutoGeneratePool<SyncSession>> entry : entries)
					Utils.tryClose(entry.getValue());
				tempPool.clear();

				return temp;
			} catch (Exception e) {
				throw new LYException("Data in cache is corrupted, unable to fetch last data!", e);
			}
		}
	}

	private boolean syncSet(Map<String, List<InetAddr>> server2addr_cmp) {
		synchronized (lock) {
			try {
				byte[] cmpData = server2addr_cmp == null ? new byte[0]
						: Utils.serialize(server2addr_cmp).getBytes(CoreDef.CHARSET());
				byte[] data = Utils.serialize(server2addr).getBytes(CoreDef.CHARSET());
				if (rdmaClient.compareAndSet("RPCBackgroundServerMap_" + "server2addr", data, cmpData) != 0)
					return false;
				return true;
			} catch (Exception e) {
				throw new LYException("Compare and set newest server to addr map failed with exception", e);
			}
		}
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
			Message msg = (Message) p.decode(response);
			returnConnection(socket);
			return msg;
		} while (torelent-- > 0);
//		removeServer(request.getServer(), ip);
		// TODO fatal report
		log.error("Maximun torelent is reached, socket request failed, server[" + request.getServer() + "] on " + ip + " is down.");
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

	private void addServer(String server, String ip, int port) {
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

	private void removeServer(String server, String ip, int port) {
		synchronized (lock) {
			List<InetAddr> list = server2addr.remove(server);

			AutoGeneratePool<SyncSession> pool = null;
			for (InetAddr addr : list) {
				if (!addr.equals(InetAddr.fromInetAddr(ip, port)))
					continue;
				pool = addr2ConnectionPool.remove(addr);
				break;

			}
			if (pool != null)
				pool.close();
		}
	}

	public void setRdmaClient(RDMAClient rdmaClient) {
		this.rdmaClient = rdmaClient;
	}

	public Map<String, List<InetAddr>> getServerAddrMap() {
		return server2addr;
	}

}
