package net.vicp.lylab.lyserver.utils;

import net.vicp.lylab.core.NonCloneableBaseObject;
import net.vicp.lylab.core.interfaces.LifeCycle;
import net.vicp.lylab.core.model.CacheValue;
import net.vicp.lylab.lyserver.model.CacheValueEntry;
import net.vicp.lylab.utils.Utils;
import net.vicp.lylab.utils.cache.CacheContainer;
import net.vicp.lylab.utils.cache.LYCache;
import net.vicp.lylab.utils.permanent.DiskStorage;

/**
 * [!]警告：必须在LYCache初始化后才能使用
 * 
 * @author Young
 *
 */
public class CacheLifeCycle extends NonCloneableBaseObject implements LifeCycle {

	String savePath = null;
	LYCache lyCache = null;
	
	@Override
	public void initialize() {
		Object[] list = Utils.readJsonObjectFromFile(CacheValueEntry.class, savePath, "cve");
		for(Object obj:list)
			lyCache.setCacheValue(((CacheValueEntry) obj).getKey(), ((CacheValueEntry) obj).getCv());
		lyCache.flush();
	}

	@Override
	public void close() throws Exception {
		DiskStorage dp = new DiskStorage(savePath, "cve");
		for (CacheContainer cc : lyCache.getBundles()) {
			for (String key : cc.keySet()) {
				CacheValue cv = cc.getCacheValue(key);
				dp.appendLine(Utils.serialize(new CacheValueEntry(key, cv)));
			}
		}
		try {
			dp.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public LYCache getLyCache() {
		return lyCache;
	}

	public void setLyCache(LYCache lyCache) {
		this.lyCache = lyCache;
	}
	
}
