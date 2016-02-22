package net.vicp.lylab.server;

import java.io.File;

import net.vicp.lylab.core.CoreDef;

public class ServerRuntime {
	public static void main(String[] arg) {
		try {
			CoreDef.config.reload(CoreDef.rootPath + File.separator + "config" + File.separator + "config.txt");
			System.out.println("Server started");
		} catch (Exception e) {
			e.printStackTrace();
			close();
			System.out.println("Server failed");
		}
	}
	
	public static void close() {
		CoreDef.config.deepClose();
		System.out.println("Server stopped");
	}
	
}
