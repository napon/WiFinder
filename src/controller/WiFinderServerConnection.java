package controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import model.WiFiPoint;

/**
 * Establish a connection with the server and parses its response
 * @author napontaratan
 */
public class WiFinderServerConnection {

	private static WiFinderServerConnection instance = null;

	// allow only one instance of WiFinderServerConnection
	private WiFinderServerConnection() {}

	public static WiFinderServerConnection getInstance() {
		if(instance == null)
			instance = new WiFinderServerConnection();
		return instance;
	}

	// retrieve locations from server and return a list of WiFiPoint
	public static List<WiFiPoint> fetchLocations(){
		//using sample data, since server is not setup yet
		// TODO: parse xml content into List of WiFiPoint

		File sampleData = new File("sample_response.xml");
		final List<WiFiPoint> points = new ArrayList<WiFiPoint>();

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				String name;
				double latitude;
				double longitude;
				int signalStrength;

				public void endElement(String uri, String localName, String qName) throws SAXException {

					if(qName.equals("name")){
						name = qName;
					} else if(qName.equals("latitude")) {
						latitude = Double.parseDouble(qName);
					} else if(qName.equals("longitude")) {
						longitude = Double.parseDouble(qName);
					} else if(qName.equals("signal_strength")) {
						signalStrength = Integer.parseInt(qName);
					} else if(qName.equals("hotspot")) {
						WiFiPoint wf = new WiFiPoint(name, signalStrength, latitude, longitude);
						points.add(wf);
					}

					System.out.println("Element: " + qName);
				}

			};

			saxParser.parse(sampleData, handler);

		} catch(Exception e) {
			e.printStackTrace();
		}

		return points;
	}

}