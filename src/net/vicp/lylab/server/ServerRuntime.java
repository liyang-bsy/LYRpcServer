package net.vicp.lylab.server;

import java.io.File;

import net.vicp.lylab.core.CoreDef;

public class ServerRuntime {
	public static void main(String[] arg) {
		CoreDef.config.reload(CoreDef.rootPath + File.separator + "config" + File.separator + "config.txt");
	}
	
	public static void close() {
		CoreDef.config.deepClose();
	}
	
}
