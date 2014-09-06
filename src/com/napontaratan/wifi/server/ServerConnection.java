package com.napontaratan.wifi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.util.Log;

/**
 * Establish connection with the remote server for
 * - fetch wifi points
 * - push new wifi points
 *  
 * @author Napon Taratan
 */
public class ServerConnection {
	private static final String TAG = 
			"com.napontaratan.wifi.server.ServerConnection";
	
	/**
	 * Disables instantiation.
	 * 
	 * @author Kurt Ahn
	 */
	protected ServerConnection() {}
	
	/**
	 * Sends an HTTP GET request.
	 * 
	 * @param script Server-side script to run.
	 * @param arguments Array of parameter-argument pairs.
	 * @return HTTP response.
	 * @throws IOException Thrown when the request fails.
	 * @see #sendGetRequest(String, Object[][])
	 * @see #formatArgumentList(Object[][])
	 * @author Kurt Ahn
	 */
	public static String sendGetRequest(
			String script, Object[][] arguments) 
			throws IOException {
		URLConnection connection = new URL(script + "?" + 
			formatArgumentList(arguments)).openConnection();
		connection.connect();
		
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(connection.getInputStream()));
		String response = "";
		String line;
		while ((line = reader.readLine()) != null)
			response += line;
		reader.close();
		
		Log.d(TAG, "GET response: " + response);
		
		return response;
	}
	
	/**
	 * Sends an HTTP POST request.
	 * 
	 * @param script Server-side script to run.
	 * @param arguments Array of parameter-argument pairs.
	 * @return HTTP response.
	 * @throws IOException Thrown when the request fails.
	 * @see #sendGetRequest(String, Object[][])
	 * @see #formatArgumentList(Object[][])
	 * @author Kurt Ahn
	 */
	public static String sendPostRequest(
			String script, Object[][] arguments) 
			throws IOException {
		URLConnection connection = new URL(script).openConnection();
		connection.setDoOutput(true);
		connection.connect();
		
		OutputStreamWriter writer = new OutputStreamWriter(
				connection.getOutputStream());
		writer.write(formatArgumentList(arguments));
		writer.close();
		
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(connection.getInputStream()));
		String response = "";
		String line;
		while ((line = reader.readLine()) != null)
			response += line;
		reader.close();
		
		
		Log.d(TAG, response);
		
		return response;
	}
	
	/**
	 * Takes in a list of parameter-argument pairs and converts
	 * it into a valid HTTP argument list string.
	 * 
	 * <p/>Example:
	 * <pre>
	 * <code>
	 * formatArgumentList(new Object[][] {
	 * 	{"Double", 2.2},
	 * 	{"Douglas Adams", 42},
	 * 	{"cash", "$28.00"}
	 * }); // returns: Double=2.2&Douglas+Adams=42&cash=%2428.00
	 * </code>
	 * </pre>
	 * 
	 * @param arguments List of parameter-argument pairs to format.
	 * Must be of n x 2 dimensions where n is any positive integer.
	 * @return URL-encoded string of the list of parameter-argument
	 * pairs.
	 * @throws IllegalArgumentException Thrown if <code>arguments</code>
	 * doesn't have exactly 2 columns.
	 * @author Kurt Ahn
	 */
	protected static String formatArgumentList(
			Object[][] arguments) {
		if (arguments.length == 0)
			throw new IllegalArgumentException(
					"'arguments' must have at least one row.");
		if (arguments[0].length != 2)
			throw new IllegalArgumentException(
					"'arguments' must have exactly two columns.");
		
		String list = "";
		try {
			for (int i = 0; i < arguments.length; ++i) {
				list += URLEncoder.encode(arguments[i][0].toString(), 
								"UTF-8") + "=" + 
						URLEncoder.encode(arguments[i][1].toString(),
								"UTF-8") + "&";
			}
		} catch (UnsupportedEncodingException e) {
			Log.wtf(TAG, "Encoding scheme is not valid.");
		}
		
		return list.substring(0, list.length() - 1);
	}
	
	/*
	 * Don't need the following.
	 */
//	/**
//	 * Creates an HTTP request to the server and returns the server's response
//	 * 
//	 * @param request HTTP request without the server address.
//	 * @author Napon Taratan
//	 * @throws ServerConnectionFailureException 
//	 */
//	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
//	public String makeJSONQuery(String request) 
//			throws ServerConnectionFailureException {
////		String responseString = null;
//		HttpURLConnection urlConnection = null;
//		StringBuilder response = new StringBuilder();
//
//		try {
//			URL url = new URL(request);
//			urlConnection =  (HttpURLConnection) url.openConnection();
//			urlConnection.setUseCaches(true);
//			int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
//			urlConnection.addRequestProperty("Cache-Control", "max-stale=" + maxStale);
//			System.out.println("response message: " + urlConnection.getResponseMessage());
//			System.out.println("header field: " + urlConnection.getHeaderFields());
//			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//				HttpResponseCache cache = HttpResponseCache.getInstalled();
//				System.out.println("cache request count: " + cache.getRequestCount());
//				System.out.println("cache hit count: " + cache.getHitCount() );
//				System.out.println("cache network count: "  + cache.getNetworkCount());
//			}			
//			BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//			String line;
//			while ((line = r.readLine()) != null) {
//				response.append(line);
//			}
//			r.close();
//			
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			urlConnection.disconnect();
//		}
//		return response.toString();
//        
////        try {
////            System.out.println("make JSON query to server \n " + server);
////            HttpClient httpClient = new DefaultHttpClient();
////            HttpGet httpGet = new HttpGet(server);
////            HttpResponse response = httpClient.execute(new HttpGet(server));
////            responseString = new BasicResponseHandler().handleResponse(response);
////            System.out.println(responseString);
////        } catch (Exception e) {
////            e.printStackTrace();
////            throw new ServerConnectionFailureException();
////        }
//
////        return responseString;
//	}
}
