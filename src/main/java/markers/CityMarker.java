package markers;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;

public class CityMarker extends CommonMarker {
	
	public static int TRI_SIZE = 5;  // The size of the triangle marker
	
	public CityMarker(Location location) {
		super(location);
	}
	
	
	public CityMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
		// Cities have properties: "name" (city name), "country" (country name)
		// and "population" (population, in millions)
	}
	
	
	// pg is the graphics object on which you call the graphics
	// methods.  e.g. pg.fill(255, 0, 0) will set the color to red
	// x and y are the center of the object to draw. 
	// They will be used to calculate the coordinates to pass
	// into any shape drawing methods.  
	// e.g. pg.rect(x, y, 10, 10) will draw a 10x10 square
	// whose upper left corner is at position x, y
	/**
	 * Implementation of method to draw marker on the map.
	 */
	public void drawMarker(PGraphics pg, float x, float y) {
		//System.out.println("Drawing a city");
		// Save previous drawing style
		pg.pushStyle();
		
		// IMPLEMENT: drawing triangle for each city
		pg.fill(150, 30, 30);
		pg.triangle(x, y-TRI_SIZE, x-TRI_SIZE, y+TRI_SIZE, x+TRI_SIZE, y+TRI_SIZE);
		
		// Restore previous drawing style
		pg.popStyle();
	}
	
	/** Show the title of the city if this marker is selected */
//	public void showTitle(PGraphics pg, float x, float y)
//	{
//		String name = getCity() + " " + getCountry() + " ";
//		String pop = getPopulationInt();
//
//		float textHeight = 18;
//		float padding = 6;
//		float rectHeight = 2 * textHeight + padding;
//		float rectWidth = Math.max(pg.textWidth(name), pg.textWidth(pop)) + padding;
//
//		pg.pushStyle();
//
//		pg.fill(255, 255, 255);
//		pg.textSize(12);
//		pg.rectMode(PConstants.CORNER);
//		pg.rect(x, y - TRI_SIZE - rectHeight, rectWidth, rectHeight);
//
//		pg.fill(0, 0, 0);
//		pg.textAlign(PConstants.LEFT, PConstants.TOP);
//
//		float lineSpacing = textHeight * 0.9f;
//		pg.text(name, x + 3, y - TRI_SIZE - rectHeight + 3);
//		pg.text(pop, x + 3, y - TRI_SIZE - rectHeight + textHeight + 3);
//
//		pg.popStyle();
//	}

	public void showTitle(PGraphics pg, float x, float y) {
		String name = getCity() + " " + getCountry() + " ";
		String pop = getPopulationInt();

		float textHeight = 12;
		float padding = 6;
		float lineSpacing = textHeight * 1.2f;

		float rectHeight = 2 * lineSpacing + padding;
		float rectWidth = Math.max(pg.textWidth(name), pg.textWidth(pop)) + padding;

		pg.pushStyle();

		pg.fill(255, 255, 255);
		pg.textSize(textHeight);
		pg.rectMode(PConstants.CORNER);
		pg.rect(x, y - TRI_SIZE - rectHeight, rectWidth, rectHeight);

		pg.fill(0, 0, 0);
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		// Центрируем строки вертикально внутри прямоугольника
		pg.text(name, x + 3, y - TRI_SIZE - rectHeight + 3);
		pg.text(pop, x + 3, y - TRI_SIZE - rectHeight + lineSpacing + 3);

		pg.popStyle();
	}


	private String getCity()
	{
		return getStringProperty("name");
	}
	
	private String getCountry()
	{
		return getStringProperty("cou_name_en");
	}

	private String getPopulationInt() {
		Object popObj = getProperty("population");
		if (popObj == null) return "Pop: N/A";
		double population;

		if (popObj instanceof Number) {
			population = ((Number) popObj).doubleValue();
		} else {
			try {
				population = Double.parseDouble(popObj.toString());
			} catch (NumberFormatException e) {
				return "Pop: N/A";
			}
		}

		if (population >= 1_000_000) {
			return String.format("Pop: %.2f Million", population / 1_000_000.0);
		} else if (population >= 1_000) {
			return String.format("Pop: %.1f K", population / 1_000.0);
		} else {
			return "Pop: " + (int)population + " people";
		}
	}
}
