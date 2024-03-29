package src.parsers;


import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PApplet;
import processing.data.XML;

public class ParseFeed {


	/*
	 * This method is to parse a GeoRSS feed corresponding to earthquakes around
	 * the globe.
	 * 
	 * @param p - PApplet being used
	 * @param fileName - file name or URL for data source
	 */
	public static List<PointFeature> parseEarthquake(PApplet p, String fileName) {
		List<PointFeature> features = new ArrayList<>();

		XML rss = p.loadXML(fileName);
		// Get all items
		XML[] itemXML = rss.getChildren("entry");
		PointFeature point;

        for (XML xml : itemXML) {

            // get location and create feature
            Location location = getLocationFromPoint(xml);

            // if successful create PointFeature and add to list
            if (location != null) {
                point = new PointFeature(location);
                features.add(point);
            } else {
                continue;
            }

            // Sets title if existing
            String titleStr = getStringVal(xml, "title");
            if (titleStr != null) {
                point.putProperty("title", titleStr);
                // get magnitude from title
                point.putProperty("magnitude", Float.parseFloat(titleStr.substring(2, 5)));
            }

            // Sets depth(elevation) if existing
            float depthVal = getFloatVal(xml);

            // NOT SURE ABOUT CHECKING ERR CONDITION BECAUSE 0 COULD BE VALID?
            // get one decimal place when converting to km
            int interVal = (int) (depthVal / 100);
            depthVal = (float) interVal / 10;
            point.putProperty("depth", Math.abs((depthVal)));


            // Sets age if existing
            XML[] catXML = xml.getChildren("category");
            for (XML value : catXML) {
                String label = value.getString("label");
                if ("Age".equals(label)) {
                    String ageStr = value.getString("term");
                    point.putProperty("age", ageStr);
                }
            }


        }
		
			return features;
		}

	
	/*
	 * Gets location from georss:point tag
	 * 
	 * @param XML Node which has point as child
	 * 
	 * @return Location object corresponding to point
	 */
	private static Location getLocationFromPoint(XML itemXML) {
		// set loc to null in case of failure
		Location loc = null;
		XML pointXML = itemXML.getChild("georss:point");
		
		// set location if existing
		if (pointXML != null && pointXML.getContent() != null) {
			String pointStr = pointXML.getContent();
			String[] latLon = pointStr.split(" ");
			float lat = Float.parseFloat(latLon[0]);
			float lon = Float.parseFloat(latLon[1]);

			loc = new Location(lat, lon);
		}
		
		return loc;
	}	
	
	/*
	 * Get String content from child node.
	 */
	private static String getStringVal(XML itemXML, String tagName) {
		// Sets title if existing
		String str = null;
		XML strXML = itemXML.getChild(tagName);
		
		// check if node exists and has content
		if (strXML != null && strXML.getContent() != null) {
			str = strXML.getContent();
		}
		
		return str;
	}
	
	/*
	 * Get float value from child node
	 */
	private static float getFloatVal(XML itemXML) {
		return Float.parseFloat(getStringVal(itemXML, "georss:elev"));
	}

}