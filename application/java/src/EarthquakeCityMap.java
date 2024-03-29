package src;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import src.markers.*;
import src.parsers.ParseFeed;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

public class EarthquakeCityMap extends PApplet {
    private final String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
    private final String cityFile = "city-data.json";
    private final String countryFile = "countries.geo.json";
    private UnfoldingMap map;
    private List<Marker> cityMarkers;
    private List<Marker> quakeMarkers;
    private List<Marker> countryMarkers;
    private CommonMarker lastSelected;
    private CommonMarker lastClicked;

    private static final long serialVersionUID = 1L;
    // IF YOU ARE WORKING OFFILINE, change the value of this variable to true
    private static final boolean offline = false;
    /**
     * This is where to find the local tiles, for working without an Internet connection
     */
    public static String mbTilesString = "blankLight-1-3.mbtiles";
    //feed with magnitude 2.5+ Earthquakes

    static public void main(String[] args) {
        PApplet.main(new String[]{"src.EarthquakeCityMap"});
    }

    public void setup() {
        // (1) Initializing canvas and map tiles
        size(900, 700, OPENGL);
        if (offline) {
            System.exit(0);
        } else {
            map = new UnfoldingMap(this, 200, 50, 650, 600, new Microsoft.RoadProvider());
            // IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
            //earthquakesURL = "2.5_week.atom";
        }
        MapUtils.createDefaultEventDispatcher(this, map);

        //     STEP 1: load country features and src.markers
        List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
        countryMarkers = MapUtils.createSimpleMarkers(countries);

        //     STEP 2: read in city data
        List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
        cityMarkers = new ArrayList<>();
        for (Feature city : cities) {
            cityMarkers.add(new CityMarker(city));
        }

        //     STEP 3: read in earthquake RSS feed
        List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
        quakeMarkers = new ArrayList<>();

        for (PointFeature feature : earthquakes) {
            //check if LandQuake
            if (isLand(feature)) {
                quakeMarkers.add(new LandQuakeMarker(feature));
            }
            // OceanQuakes
            else {
                quakeMarkers.add(new OceanQuakeMarker(feature));
            }
        }
        map.addMarkers(quakeMarkers);
        map.addMarkers(cityMarkers);

    }

    public void draw() {
        background(0);
        map.draw();
        addKey();
    }

    @Override
    public void mouseMoved() {
        // clear the last selection
        if (lastSelected != null) {
            lastSelected.setSelected(false);
            lastSelected = null;
        }
        selectMarkerIfHover(quakeMarkers);
        selectMarkerIfHover(cityMarkers);
    }

    private void selectMarkerIfHover(List<Marker> markers) {
        // Abort if there's already a marker selected
        if (lastSelected != null) {
            return;
        }

        for (Marker m : markers) {
            CommonMarker marker = (CommonMarker) m;
            if (marker.isInside(map, mouseX, mouseY)) {
                lastSelected = marker;
                marker.setSelected(true);
                return;
            }
        }
    }

    @Override
    public void mouseClicked() {
        if (lastClicked != null) {
            unhideMarkers();
            lastClicked = null;
        } else {
            checkEarthquakesForClick();
            if (lastClicked == null) {
                checkCitiesForClick();
            }
        }
    }


    private void checkCitiesForClick() {
        if (lastClicked != null) return;
        // Loop over the earthquake src.markers to see if one of them is selected
        for (Marker marker : cityMarkers) {
            if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
                lastClicked = (CommonMarker) marker;
                // Hide all the other earthquakes and hide
                for (Marker mhide : cityMarkers) {
                    if (mhide != lastClicked) {
                        mhide.setHidden(true);
                    }
                }
                for (Marker mhide : quakeMarkers) {
                    EarthquakeMarker quakeMarker = (EarthquakeMarker) mhide;
                    if (quakeMarker.getDistanceTo(marker.getLocation()) > quakeMarker.threatCircle()) {
                        quakeMarker.setHidden(true);
                    }
                }
                return;
            }
        }
    }

    private void checkEarthquakesForClick() {
        if (lastClicked != null) return;
        // Loop over the earthquake src.markers to see if one of them is selected
        for (Marker m : quakeMarkers) {
            EarthquakeMarker marker = (EarthquakeMarker) m;
            if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
                lastClicked = marker;
                // Hide all the other earthquakes and hide
                for (Marker mhide : quakeMarkers) {
                    if (mhide != lastClicked) {
                        mhide.setHidden(true);
                    }
                }
                for (Marker mhide : cityMarkers) {
                    if (mhide.getDistanceTo(marker.getLocation()) > marker.threatCircle()) {
                        mhide.setHidden(true);
                    }
                }
                return;
            }
        }
    }

    private void unhideMarkers() {
        for (Marker marker : quakeMarkers) {
            marker.setHidden(false);
        }

        for (Marker marker : cityMarkers) {
            marker.setHidden(false);
        }
    }


    private void addKey() {
        // Remember you can use Processing's graphics methods here
        fill(255, 250, 240);

        int xbase = 25;
        int ybase = 50;

        rect(xbase, ybase, 150, 250);

        fill(0);
        textAlign(LEFT, CENTER);
        textSize(12);
        text("Earthquake Key", xbase + 25, ybase + 25);

        fill(150, 30, 30);
        int tri_xbase = xbase + 35;
        int tri_ybase = ybase + 50;
        triangle(tri_xbase, tri_ybase - CityMarker.TRI_SIZE, tri_xbase - CityMarker.TRI_SIZE, tri_ybase + CityMarker.TRI_SIZE, tri_xbase + CityMarker.TRI_SIZE, tri_ybase + CityMarker.TRI_SIZE);

        fill(0, 0, 0);
        textAlign(LEFT, CENTER);
        text("City Marker", tri_xbase + 15, tri_ybase);

        text("Land Quake", xbase + 50, ybase + 70);
        text("Ocean Quake", xbase + 50, ybase + 90);
        text("Size ~ Magnitude", xbase + 25, ybase + 110);

        fill(255, 255, 255);
        ellipse(xbase + 35, ybase + 70, 10, 10);
        rect(xbase + 35 - 5, ybase + 90 - 5, 10, 10);

        fill(color(255, 255, 0));
        ellipse(xbase + 35, ybase + 140, 12, 12);
        fill(color(0, 0, 255));
        ellipse(xbase + 35, ybase + 160, 12, 12);
        fill(color(255, 0, 0));
        ellipse(xbase + 35, ybase + 180, 12, 12);

        textAlign(LEFT, CENTER);
        fill(0, 0, 0);
        text("Shallow", xbase + 50, ybase + 140);
        text("Intermediate", xbase + 50, ybase + 160);
        text("Deep", xbase + 50, ybase + 180);

        text("Past hour", xbase + 50, ybase + 200);

        fill(255, 255, 255);
        int centerx = xbase + 35;
        int centery = ybase + 200;
        ellipse(centerx, centery, 12, 12);

        strokeWeight(2);
        line(centerx - 8, centery - 8, centerx + 8, centery + 8);
        line(centerx - 8, centery + 8, centerx + 8, centery - 8);


    }


    private boolean isLand(PointFeature earthquake) {

        // IMPLEMENT THIS: loop over all countries to check if location is in any of them
        // If it is, add 1 to the entry in countryQuakes corresponding to this country.
        for (Marker country : countryMarkers) {
            if (isInCountry(earthquake, country)) {
                return true;
            }
        }

        // not inside any country
        return false;
    }

    private boolean isInCountry(PointFeature earthquake, Marker country) {
        // getting location of feature
        Location checkLoc = earthquake.getLocation();

        // some countries represented it as MultiMarker
        // looping over SimplePolygonMarkers which make them up to use isInsideByLoc
        if (country.getClass() == MultiMarker.class) {
            // looping over src.markers making up MultiMarker
            for (Marker marker : ((MultiMarker) country).getMarkers()) {
                // checking if inside
                if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc)) {
                    earthquake.addProperty("country", country.getProperty("name"));

                    // return if is inside one
                    return true;
                }
            }
        }
        // check if inside country represented by SimplePolygonMarker
        else if (((AbstractShapeMarker) country).isInsideByLocation(checkLoc)) {
            earthquake.addProperty("country", country.getProperty("name"));

            return true;
        }
        return false;
    }
}
