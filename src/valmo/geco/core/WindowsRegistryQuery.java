/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;


/**
 * See http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 * See http://www.codeproject.com/KB/system/listports.aspx
 * See http://www.rhinocerus.net/forum/lang-pascal-delphi-misc/438962-identifying-friendlyname-usb-com-port.html
 * 
 * @author Simon Denier, based on Oleg Ryaboy, Miguel Enriquez 
 * @since 12 d�c. 2010
 */
public class WindowsRegistryQuery {
	public static final String readRegistry(String location, String key) {
		try {
			Process process = Runtime.getRuntime().exec("reg query \"" + location + "\" /v " + key);
			StreamReader reader = new StreamReader(process.getInputStream());
			reader.start();
			process.waitFor();
			reader.join();

			String output = reader.getResult();
			// Output has the following format:
			// \n<Version information>\n\n<key>\t<registry type>\t<value>
			if (!output.contains("\t")) {
				return null;
			}
			// Parse out the value
			String[] parsed = output.split("\t");
			return parsed[parsed.length - 1];
		} catch (Exception e) {
			return null;
		}
	}

	public static final String listRegistryEntries(String location) {
		try {
			Process process = Runtime.getRuntime().exec("reg query \"" + location + "\" /s");
			StreamReader reader = new StreamReader(process.getInputStream());
			reader.start();
			process.waitFor();
			reader.join();
			
			return reader.getResult();
		} catch (Exception e) {
			return null;
		}
	}

	static class StreamReader extends Thread {
		private InputStream is;
		private StringWriter sw = new StringWriter();;
		public StreamReader(InputStream is) {
			this.is = is;
		}
		public void run() {
			try {
				int c;
				while ((c = is.read()) != -1)
					sw.write(c);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public String getResult() {
			return sw.toString();
		}
	}
	
	public static void main(String[] args) {
		String[] entries =
			WindowsRegistryQuery.listRegistryEntries("HKLM\\System\\CurrentControlSet\\Enum").split("\n");
		for (String string : entries) {
			if( string.contains("COM") && string.contains("FriendlyName") )
				System.out.println(string.trim());
		}
	}
}
