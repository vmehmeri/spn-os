package spn.os;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Console {
	private String prefix;
	
	protected Console () {
		
	}
	
	protected Console (Object o) {
		this.prefix = String.format("[%s] ",o.getClass().getSimpleName());
	}
	
	public static Console getConsole(Object o) {
		return new Console(o);
	}
	
	public void info(Object out) {
		if (out != null) {
			System.out.println(prefix + out.toString());
		}
	}
	
	public void debug(Object out) {
		if (out != null) {
			System.out.println("[DEBUG] " + prefix + out.toString());
		}
	}
	
	public void json(Object out) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		if (out != null) {
			try {
				JsonElement je = jp.parse(out.toString());
				String outStr = gson.toJson(je);
				System.out.println("[DEBUG] " + prefix + outStr);
			} catch (Exception e) {
				System.out.println("[DEBUG] " + prefix + out.toString());
			}
		}
	}
	
	public void jsonToFile(Object out, String destFilePath) throws Exception {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		if (out != null && destFilePath != null) {
			JsonElement je = jp.parse(out.toString());
			String outStr = gson.toJson(je);
			Util.writeToFile(outStr, destFilePath);
		}
	}
	
	public void jsonToFile(String prefix, Object out, String result, String destFilePath) throws Exception {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		if (out != null && destFilePath != null) {
			String outStr;
			if (result == null) result = "";
			try {
				JsonElement je = jp.parse(out.toString());
				outStr = prefix + "\n" + gson.toJson(je) + "\nResult:\n";
			} catch (Exception e) {
				outStr = prefix + "\n" + out.toString() + "\nResult:\n";
			}
			
			try {
				JsonElement je = jp.parse(result);
				outStr = outStr + gson.toJson(je) + "\n";
			} catch (Exception e) {
				outStr = outStr + result + "\n";
			}
			
			Util.appendToFile(outStr, destFilePath);
		}
	}		
	
	public void error(Object out) {
		if (out != null) {
			System.err.println("[ERROR] " + prefix + out.toString());
		}
	}
	
	public void info(String... args) {
		StringBuilder output = new StringBuilder();
		output.append(prefix);
	    for (String arg : args) {
	    	output.append(arg);
	        output.append(": ");
	    }
	    output.deleteCharAt(output.length()-1);
	    output.deleteCharAt(output.length()-1);
	    System.out.println(output);
	}
	
	public void debug(String... args) {
		StringBuilder output = new StringBuilder();
		output.append("[DEBUG] ");
		output.append(prefix);
	    for (String arg : args) {
	    	output.append(arg);
	        output.append(": ");
	    }
	    output.deleteCharAt(output.length()-1);
	    output.deleteCharAt(output.length()-1);
	    System.out.println(output);
	}
	
	public void error(String... args) {
		StringBuilder output = new StringBuilder();
		output.append("[ERROR] ");
		output.append(prefix);
	    for (String arg : args) {
	    	output.append(arg);
	        output.append(": ");
	    }
	    output.deleteCharAt(output.length()-1);
	    output.deleteCharAt(output.length()-1);
	    System.err.println(output);
	}
	
	public void menu(String... args) {
		StringBuilder output = new StringBuilder();
		output.append("\t");
		
		if (args != null && args.length > 0) {
			output.append(args[0]);
		}
		
		if (args.length > 1) {
			for (int indx = 1; indx < args.length; indx++) {
				output.append(" <" + args[indx] + ">");
			}
		}
		System.out.println(output);
	}
	
}
