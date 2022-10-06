
package com.trapezium.chisel;

import java.io.*;
import java.awt.*;
import java.util.*;

public class ChiselProperties {

	private static Properties properties = null;
	private static String filename = null;

	static {
		properties = new Properties();
		setDefaults(properties);
		filename = "chisel.context";
	}

	public static void setDefaults(Properties properties) {
		properties.put("font.name", "Monospaced");
		properties.put("font.style", String.valueOf(Font.PLAIN));
		properties.put("font.size", "12");
		properties.put("tab.size", "4");
		properties.put("save.dir", ".");
	}

	public static Properties getProperties() {
		return properties;
	}

	public static void setProperties(String name) {
		try {
			FileInputStream in = new FileInputStream(name);
			properties.load(in);
			in.close();
			filename = name;

			System.out.println("Chisel settings loaded from " + name);

		} catch (Exception e) {
//			System.out.println("Unable to load Chisel settings from " + name);
		}
	}
	public static void saveProperties() {
		saveProperties(filename);
	}


	public static void saveProperties(String name) {
		try	{
			FileOutputStream out = new FileOutputStream(name);
			properties.save( out, "Chisel settings" );
			out.close();
			filename = name;

		} catch (Exception e) {
			System.out.println("Unable to save Chisel settings to " + name);
		}
	}
}
