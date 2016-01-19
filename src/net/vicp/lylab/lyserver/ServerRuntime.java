package net.vicp.lylab.lyserver;

import java.io.File;

import net.vicp.lylab.core.CoreDef;
import net.vicp.lylab.utils.Config;

public class ServerRuntime {
	public static void main(String[] arg) {
		CoreDef.config = new Config(CoreDef.rootPath + File.separator + "config" + File.separator + "config.txt");
	}
	
	public static void close() {
		CoreDef.config.deepClose();
	}
	
}
