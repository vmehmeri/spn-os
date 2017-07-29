package spn.os;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spn.exception.InvalidConfigurationException;
import spn.exception.InvalidMappingException;
import spn.os.element.PhysicalNetwork;
import spn.os.element.VirtualNetwork;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Util {
	public static PhysicalNetwork readPhysical(String filename) {
        PhysicalNetwork physical;
//        System.out.println("Reading physical topology from " + filename);
        try {
            physical = PhysicalNetwork.fromJson(Util.jsonFromFilename(filename));
//            System.out.println("Read physical topology from " + filename);
            return physical;
        } catch (InvalidConfigurationException e) {
            System.err.println("Could not read physical topology file." + e.toString());
            return null;
        }
    }
    
    public static PhysicalNetwork buildPhysical(String jsonStringPhysicalNetwork) {
    	PhysicalNetwork physical;
    	try {
    		JsonParser parser = new JsonParser();
    		JsonObject jsonObj = (JsonObject) parser.parse(jsonStringPhysicalNetwork);
            physical = PhysicalNetwork.fromJson(jsonObj);
            return physical;
        } catch (InvalidConfigurationException e) {
            System.err.println("Could not build physical network object" + e.toString());
            return null;
        }
    }

    public static VirtualNetwork readVirtual(String filename) {
        VirtualNetwork virtual;
//        System.out.println("Reading virtual topology from " + filename);
        try {
            virtual = VirtualNetwork.fromJson(Util.jsonFromFilename(filename));
//            System.out.println("Read virtual topology from " + filename);
            return virtual;
        } catch (InvalidConfigurationException e) {
            System.err.println("Could not read virtual topology file." + e.toString());
            return null;
        }
    }

    public static Mapping readMapping(String filename, VirtualNetwork virtual,
                                      PhysicalNetwork physical) {
        Mapping mapping;
//        System.out.println("Reading mapping from " + filename);
        try {
            mapping = Mapping.fromJson(Util.jsonElementFromFilename(filename),
                                       virtual, physical);
//            System.out.println("Read mapping from " + filename);
            return mapping;
        } catch (InvalidConfigurationException e) {
            System.err.println("Could not read virtual topology file." + e.toString());
            return null;
        }
    }
    
    public static Mapping buildMapping(String jsonStringMapping, VirtualNetwork virtual, PhysicalNetwork physical) {
    	Mapping mapping;
    	try {
    		
    		mapping = Mapping.fromJson(jsonElementFromString(jsonStringMapping), virtual, physical) ;
            
            return mapping;
        } catch (InvalidConfigurationException e) {
            System.err.println("Could not build network mapping object" + e.toString());
            return null;
        }
    }

    public static OpticalMapping readOpticalMapping(String filename,
                                                PhysicalNetwork network) {
        try {
            JsonArray json = jsonElementFromFilename(filename).getAsJsonArray();
            OpticalMapping mapping = OpticalMapping.fromJson(json, network);
//            System.out.println("Read optical mapping from " + filename);
            return mapping;
        } catch (InvalidConfigurationException e) {
            System.err.println("Could not read optical mapping file. " + e.toString());
            return null;
        } catch (IllegalStateException e) {
            System.err.println("Optical mapping must be a JSON Array. " + e.toString());
            return null;
        } catch (InvalidMappingException e) {
            // InvalidMappingException already contains a descriptive error message
            System.err.println(e.toString());
        }
        return null;
    }

    public static OpticalMapping buildOpticalMapping(String jsonStr,
                                                PhysicalNetwork network) {
        try {
            JsonArray json = jsonElementFromString(jsonStr).getAsJsonArray();
            OpticalMapping mapping = OpticalMapping.fromJson(json, network);
            return mapping;
        } catch (InvalidConfigurationException e) {
            System.err.println("Could not build optical mapping object " + e.toString());
            return null;
        } catch (IllegalStateException e) {
            System.err.println("Optical mapping must be a JSON Array. " + e.toString());
            return null;
        } catch (InvalidMappingException e) {
            // InvalidMappingException already contains a descriptive error message
            System.err.println(e.toString());
        }
        return null;
    }

    public static JsonObject jsonFromFilename (String filename)
        throws InvalidConfigurationException {
        try
        {
            String config = new String(Files.readAllBytes(Paths.get(filename)));
            JsonObject json = (JsonObject) new JsonParser().parse(config);
            return json;
        } catch (IOException e) {
            throw new InvalidConfigurationException("Could not read file: " + filename);
        }
    }
    
    public static JsonObject jsonFromString (String jsonString) {
            JsonObject json = (JsonObject) new JsonParser().parse(jsonString);
            return json;
        }
    
    public static String jsonStringFromFilename (String filename) 
    	throws InvalidConfigurationException {
            try
            {
                return new String(Files.readAllBytes(Paths.get(filename)));
                
            } catch (IOException e) {
                throw new InvalidConfigurationException("Could not read file: " + filename);
            }
    }

    public static JsonElement jsonElementFromFilename (String filename)
        throws InvalidConfigurationException {
        try
        {
            String config = new String(Files.readAllBytes(Paths.get(filename)));
            JsonElement json = new JsonParser().parse(config);
            return json;
        } catch (IOException e) {
            throw new InvalidConfigurationException("Could not use configuration file: " + filename);
        }
    }

    public static JsonElement jsonElementFromString (String jsonStr)
        throws InvalidConfigurationException {
        
        JsonElement json = new JsonParser().parse(jsonStr);
        return json;
    }
    
    /**
     * Transforms a String set into a single String with each
     * element of the set separated by the white space character
     * @param set : Set of Strings
     * @return
     */
    public static String fromStringSetToString(Set<String> set) {
    	StringBuilder sb = new StringBuilder();
    	for (String s : set)
    	{
    	    sb.append(s);
    	    sb.append(" ");
    	}

    	return sb.toString();
    }
    
    public static Set<String> fromStringToStringSet(String str) {
    	Set<String> hashSet = new HashSet<String>(Arrays.asList(str
                .split(" ")));
        
    	return hashSet;
    }
    
    public static void wait(int seconds) {
    	if (seconds < 1) return;
    	try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

	public static List<String> readTemplates(List<String> serviceFilesLoc) {
		List<String> templates = new ArrayList<String>();
		
		for (String serviceFile : serviceFilesLoc) {
			System.out.println("Loading template: " + serviceFile);
			try(BufferedReader br = new BufferedReader(new FileReader(serviceFile))) {
			    StringBuilder sb = new StringBuilder();
			    String line = br.readLine();
	
			    while (line != null) {
			        sb.append(line);
			        sb.append(System.lineSeparator());
			        line = br.readLine();
			    }
			    String allLines = sb.toString();
			    
			    if (allLines != null)
			    	templates.add(allLines);
			    
			} catch (IOException e) {
				System.err.println("Cloud not load service template file: " + e.getMessage());
				return null;
			}
		}
		return templates;
	}
	
	public static void writeToFile(String content, String destFilePath) throws Exception  {
		File file = new File( destFilePath );
		file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(content);
		bw.close();

//		System.out.println("Wrote service config to file " + destFilePath);
	}
	
	public static void appendToFile(String content, String destFilePath) throws Exception  {
		File file = new File( destFilePath );
		if (!file.exists())
			file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.newLine();
		bw.write(content);
		bw.close();

//		System.out.println("Wrote service config to file " + destFilePath);
	}
	
	public static String writeNsdTemplateToFile(String template, String destDir, String fileNamePrefix) throws Exception {
		String destFilePath = destDir + fileNamePrefix + "_nsd.yaml";
		File file = new File( destFilePath );
		file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(template);
		bw.close();

		System.out.println("[DEBUG] Wrote service template to file " + destFilePath);
		return destFilePath;
	}
	
	public static List<String> writeVnfdTemplatesToFiles(List<String> templates, String destDir, String fileNamePrefix) throws Exception {
		int fileIndex = 1;
		List<String> destFiles = new ArrayList<String>();
		for (String content : templates) {
			
				if (content == null) continue;
				
				String destFilePath = destDir + fileNamePrefix + "_vnfd_" + Integer.toString(fileIndex++) + ".yaml";
				File file = new File( destFilePath );

				/*while (file.exists()) {
					
				}*/
				
				file.createNewFile();
				

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				
				bw.write(content);
				bw.close();

//				System.out.println("Wrote service template to file " + destFile);
				destFiles.add( destFilePath );
				System.out.println("[DEBUG] Wrote VNF template to file " + destFilePath);
		}
			return destFiles;
	}
	
	public static String readJsonTemplate(String jsonFileLocation) {
		
	
//		System.out.println("Loading JSON template: " + jsonFileLocation);
		try(BufferedReader br = new BufferedReader(new FileReader(jsonFileLocation))) {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    String allLines = sb.toString();
		    
		    return allLines;
		    
		} catch (IOException e) {
			System.err.println("Cloud not load service template file: " + e.getMessage());
			return null;
		}
	
		
	}

			

}
