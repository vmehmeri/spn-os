package spn.os;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import spn.exception.SfcException;


public class RemoteToscaParser  extends Thread {
	private static final String basePath = "http://192.168.137.110:5000/tosca-parser/api/";
	private static final String operationsPrefix = "operations/";
	private static final String resultsPrefix = "results/";
	private static final String resultXmlFileName = "service_config.xml";
	
	static enum Type {
		NSD, VNFD
	}

	private InputStream is;
	
	public RemoteToscaParser (InputStream is) {
		this.is = is;
	}
	
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ( (line = br.readLine()) != null)
				System.out.println("[TOSCA-PARSER]" + line);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static String postServiceTemplate(Type type, File file) throws Exception {
		if (type.equals(Type.NSD)) {
			return post(basePath + "nsd",file);
		} else if (type.equals(Type.VNFD)) {
			return post(basePath + "vnfd", file);
		} else {
			return null;
		}
	}
	
	public static String parseTemplates() throws Exception {
		post(basePath+operationsPrefix+"parse", "{'dummy':'value'}");
		String resultXmlStr = get(basePath+resultsPrefix+resultXmlFileName);
		
		return resultXmlStr;
	}
	
	private static String get(String path) throws Exception {
		StringBuilder result = new StringBuilder();
		try {
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet getRequest = new HttpGet(path);
			//getRequest.addHeader("Content-Type", "application/yang.data+json");
			//getRequest.addHeader("Accept", "application/yang.data+json");
			HttpResponse response = httpClient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 204
					&& response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode() + "\n"
						+ response.toString());
			}

			if (response.getEntity() == null)
				return "";

			else {

				BufferedReader br = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String output;

				while ((output = br.readLine()) != null) {
					result.append(output);
				}

				return result.toString();
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error: " + e.getMessage());
		}
	}
	
	
	private static String post(String path, File file) throws Exception {
		StringBuilder result = new StringBuilder();
		try {
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost postRequest = new HttpPost(path);
			//postRequest.addHeader("Content-Type", "application/yang.data+json");
			//postRequest.addHeader("Accept", "application/yang.data+json");
			HttpEntity httpEntity = MultipartEntityBuilder.create().addBinaryBody("file",file,
					ContentType.APPLICATION_OCTET_STREAM, file.getName())
					.build();
			postRequest.setEntity(httpEntity);
			HttpResponse response = httpClient.execute(postRequest);

			if (response.getStatusLine().getStatusCode() != 204
					&& response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode() + "\n"
						+ response.toString());
			}

			if (response.getEntity() == null)
				return "";

			else {

				BufferedReader br = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String output;

				while ((output = br.readLine()) != null) {
					result.append(output);
				}

				return result.toString();
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error: " + e.getMessage());
		}
	}
	
	private static String post(String path, String jsonStr) throws SfcException {
    	StringBuilder result = new StringBuilder();
        System.out.println("POST:");
        System.out.println(jsonStr);
    	try {
        	CloseableHttpClient httpClient = 
        		    HttpClientBuilder.create().build();
        		HttpPost postRequest = new HttpPost(path);
        		postRequest.addHeader("Content-Type", "application/json");
        		//postRequest.addHeader("Accept", "application/json");
        		postRequest.setEntity(new StringEntity(jsonStr, ContentType.APPLICATION_JSON));
        		HttpResponse response = httpClient.execute(postRequest);
        		
        		
        		if (response.getStatusLine().getStatusCode() != 201 && response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + response.getStatusLine().getStatusCode() + "\n" + response.toString());
                }
        		
        		if (response.getEntity() == null)
        			return "";
        		
        		else {
        		
	                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	                String output;
	                
	                while ((output = br.readLine()) != null) {
	                    result.append(output);
	                }
	        		
	        		return result.toString() ;
        		}
        		
        } catch (Exception e) {
        	e.printStackTrace();
            throw new SfcException("Error communicating with SFC manager: " + e.toString());
        }
    }
	
	

}
