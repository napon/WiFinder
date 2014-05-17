package controller;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import model.WiFiPoint;

/**
 * Establish a connection with the server and parses its response
 * @author napontaratan
 */
public class WiFinderServerConnection {

	private static WiFinderServerConnection instance = null;
	
	final List<WiFiPoint> points = new ArrayList<WiFiPoint>();
	
	// allow only one instance of WiFinderServerConnection
	private WiFinderServerConnection() { fetchLocations(); }

	public static WiFinderServerConnection getInstance() {
		if(instance == null)
			instance = new WiFinderServerConnection();
		return instance;
	}

	// retrieve locations from server
	public void fetchLocations(){

		//using sample data, since server is not setup yet
		// TODO: parse xml content into List of WiFiPoint

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				String name;
				double latitude;
				double longitude;
				int signalStrength;

				StringBuffer accumulator = new StringBuffer();

				public void endElement(String uri, String localName, String qName) throws SAXException {

					if(qName.equals("name")){
						name = accumulator.toString();
					} else if(qName.equals("latitude")) {
						latitude = Double.parseDouble(accumulator.toString());
					} else if(qName.equals("longitude")) {
						longitude = Double.parseDouble(accumulator.toString());
					} else if(qName.equals("signal_strength")) {
						signalStrength = Integer.parseInt(accumulator.toString());
					} else if(qName.equals("hotspot")) {
						WiFiPoint wf = new WiFiPoint(name, signalStrength, latitude, longitude);
						points.add(wf);
					}

					accumulator = new StringBuffer();

				}

				public void characters(char[] ch, int start, int length) throws SAXException {
					accumulator.append(new String(ch, start, length).trim());
				}

			};

			saxParser.parse(new InputSource(new StringReader(SampleData.SAMPLE_XML_RESPONSE)), handler);

		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	public List<WiFiPoint> getWiFiPoints(){
		return points;
	}
}