package module6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	

	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;
	private List<EarthquakeMarker> eqMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	private ArrayList<Button> buttons;
	private float startX = 45;
	private float startY = 580;
	private float side = 20;
	private float gap = 30;
	
	private int count = 0;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(1935, 1100, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 385, 50, 1500, 1000, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			AbstractMapProvider provider = new Microsoft.RoadProvider();
			//AbstractMapProvider provider = new Google.GoogleMapProvider();
			//map = new UnfoldingMap(this, 385, 50, 1500, 1000, provider);
			map = new UnfoldingMap(this, 385, 50, 1500, 1000, provider);
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
		//earthquakesURL = "test1.atom";
		//earthquakesURL = "test2.atom";
		
		// Uncomment this line to take the quiz
		//earthquakesURL = "quiz2.atom";
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    eqMarkers = new ArrayList<EarthquakeMarker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }
	    
	    for(Marker quakeMarker : quakeMarkers) {
	    	eqMarkers.add((EarthquakeMarker) quakeMarker);
	    }

	    // could be used for debugging
	    //printQuakes();
	    sortAndPrint(30);
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    buttons = new ArrayList<Button>();
	    buttons.add(new Button(startX, startY, side, side, "Magnitude", 0, 3));
	    buttons.add(new Button(startX, startY + gap, side, side, "Magnitude", 3, 7));
	    buttons.add(new Button(startX, startY + 2 * gap, side, side, "Magnitude", 7, 10));
	    buttons.add(new Button(startX, startY + 5 * gap, side, side, "Depth", 0, 70));
	    buttons.add(new Button(startX, startY + 6 * gap, side, side, "Depth", 70, 300));
	    buttons.add(new Button(startX, startY + 7 * gap, side, side, "Depth", 300, 701));
	    buttons.add(new Button(startX, startY + 9 * gap, side, side, "City"));
	    buttons.add(new Button(startX, (float) (startY + 10.5 * gap), (float) 7 * side, (float) 1.5 * side, "Reset"));
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
		this.drawButtons();
		addFilterKey();
		this.checkCriteriaAndUpdate();
		this.checkCityCriteriaAndUpdate();
		
	}
	
	private void drawButtons() {
		for( Button button : buttons) {
			button.drawButton(this, 255);
			if (button.getPressed() && !button.getTitle().equals("Reset")) {
				button.drawTick(this);
			}
		}
	}
	
	private void checkCriteriaAndUpdate () {
		if (allQuakeButtonsNotPressed()) {
			showAllQuakes();
			return;
		}
		hideAllQuakes();
		
		
		for (Marker marker : quakeMarkers) {
			this.checkMagnitudeCriteriaAndUpdate(marker);
			this.checkDepthCriteriaAndUpdate(marker);
		}

	}
	
	private void checkMagnitudeCriteriaAndUpdate(Marker marker) {
		float magnitude = ( (EarthquakeMarker) marker ).getMagnitude();
		if (this.magnitudeButtonsNotPressed()) {
			( (EarthquakeMarker) marker ).setMagnitudeHidden(false);
		}
		else {
			for (int i = 0; i < 3; i++) {
				Button button = buttons.get(i);
				if (button.getPressed() && checkLimits(magnitude, button)) {
					( (EarthquakeMarker) marker ).setMagnitudeHidden(false);
				}
			}
		}
	}
	
	private void checkDepthCriteriaAndUpdate(Marker marker) {
		float depth = ( (EarthquakeMarker) marker ).getDepth();
		if (this.depthButtonsNotPressed()) {
			( (EarthquakeMarker) marker ).setDepthHidden(false);
		}
		else {
			for (int i = 3; i < 6; i++) {
				Button button = buttons.get(i);
				if (button.getPressed() && checkLimits(depth, button)) {
					( (EarthquakeMarker) marker ).setDepthHidden(false);
				}
			}
		}
	}
	
	private void printQuakesHidden() {
		int hello = 1;
		for (Marker marker : quakeMarkers) {
			System.out.print(hello + ". ");
			System.out.print(marker);
			System.out.print("\thidden : " + ( (EarthquakeMarker) marker ).getFilterHidden());
			System.out.print("\tdepth hidden : " + ( (EarthquakeMarker) marker ).getDepthHidden());
			System.out.println("\tmagnitude hidden : " + ( (EarthquakeMarker) marker ).getMagnitudeHidden());
			hello += 1;
		}
	}
	
	private boolean checkLimits(float value, Button button) {
		return (value >= button.getLowerLimit() && value < button.getUpperLimit());
	}
	
	private void hideAllQuakes() {
		for (Marker m : quakeMarkers) {
			((EarthquakeMarker)m).setDepthHidden(true);
			((EarthquakeMarker)m).setMagnitudeHidden(true);
		}
	}
	
	private void showAllQuakes() {
		for (Marker m : quakeMarkers) {
			((EarthquakeMarker)m).setDepthHidden(false);
			((EarthquakeMarker)m).setMagnitudeHidden(false);
		}
	}
	
	private void checkCityCriteriaAndUpdate() {
		Button button = buttons.get(6);
		if (button.getPressed()) {
			hideAllCities();
		}
		else {
			showAllCities();
		}
	}
	
	
	
	private void hideAllCities() {
		for (Marker m : cityMarkers) {
			((CityMarker)m).setFilterHidden(true);
		}
	}
	
	private void showAllCities() {
		for (Marker m : cityMarkers) {
			((CityMarker)m).setFilterHidden(false);
		}
	}
	
	private boolean allQuakeButtonsNotPressed() {
		boolean out = false;
		for (Button button : buttons) {
			if (button.getTitle().equals("City") || button.getTitle().equals("Reset") ) continue;
			out = out || button.getPressed();
		}
		return !out;
	}
	
	private boolean magnitudeButtonsNotPressed() {
		boolean out = false;
		for (int i = 0; i < 3; i++) {
			Button button = buttons.get(i);
			out = out || button.getPressed();
		}
		return !out;
	}
	private boolean depthButtonsNotPressed() {
		boolean out = false;
		for (int i = 3; i < 6; i++) {
			Button button = buttons.get(i);
			out = out || button.getPressed();
		}
		return !out;
	}
	
	private void addFilterKey() {
		
		
		fill(0);
		text("Magnitude Filter", startX, startY - gap);
		this.addText(startX, startY, "Small (< 3.0)");
		this.addText(startX, startY + gap, "Medium (3.0 - 7.0)");
		this.addText(startX, startY + 2 * gap, "Large (> 7.0)");
		text("Depth Filter", startX, startY + 4 * gap);
		this.addText(startX, startY + 5 * gap, "Shallow (< 70km)");
		this.addText(startX, startY + 6 * gap, "Intermediate (300 - 700km)");
		this.addText(startX, startY + 7 * gap, "Deep (> 700km)");
		this.addText(startX, startY + 9 * gap, "Hide Cities");
		this.addText(startX - 15, (float)(startY + 10.5 * gap), "Reset Filters");
		textSize(23);
		text("FILTERS", startX + 30, (float) (startY - 2.2 * gap));
	}
	
	private void addText(float x, float y, String text) {
		//fill(255);
		//rect(x, y, side, side);
		fill(0);
		text(text, x + 30, y + side/ 2);
		
	}
	
	// hello
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		rect(25, 50, 335, 400);
		
		fill(255, 230, 200);
		rect(25, 475, 335, 470);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(20);
		text("Earthquake Key", 50, 75);
		
		fill(0, 0, 0);
		text("City Marker", 75, 120);
		text("Land Quake", 75, 155);
		text("Ocean Quake", 75, 190);
		text("Size - Magnitude", 40, 225);
		text("Shallow", 75, 280);
		text("Intermediate", 75, 315);
		text("Deep", 75, 350);
		
		fill(155, 31, 31);
		int height = 18;
		int base = 20;
		int x = 50;
		int y = 123;
		triangle(x, y - 2 * height/3, x - base/2, y + height/3, x + base/2, y + height/3);
		fill(255, 255, 255);
		ellipse(50, 158, 20, 20);
		rect(40, 183, 19, 19);
		fill(255, 255, 0);
		ellipse(50, 283, 20, 20);
		fill(0, 0, 255);
		ellipse(50, 318, 20, 20);
		fill(255, 0, 0);
		ellipse(50, 353, 20, 20);
	}
	
	// TODO: Add the method:
	private void sortAndPrint(int numToPrint) {
		Collections.sort(eqMarkers);
		for ( int i = 0; i < numToPrint; i++ ) {
			System.out.println(eqMarkers.get(i));
		}
	}
	// and then call that method from setUp
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
		//loop();
	}
	
	// If there is a marker selected 
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}
		
		for (Marker m : markers) 
		{
			CommonMarker marker = (CommonMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}
	
	/** The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes 
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked()

	{
		if (lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
		}
		else if (lastClicked == null) 
		{
			checkEarthquakesForClick();
			if (lastClicked == null) {
				checkCitiesForClick();
			}
		}
	}
	
	public boolean isInside(float x, float y, Button button) {
		if (x > button.getx1() && x < button.getx1() + button.getWidth() && y > button.gety1() && y < button.gety1() + button.getHeight() ) {
			return true;
		}
		return false;
	}

	public void mousePressed() {
		for (Button button : buttons) {
			if (button.getTitle().equals("Reset")) continue;
			if (isInside(mouseX, mouseY, button)) {
				button.drawButton(this, 150);
				button.toggle();
			}
		}
		if (isInside(mouseX, mouseY, buttons.get(7))) {
			for (Button button : buttons) {
				button.setPressed(false);
			}
		}
	}
	// Helper method that will check if a city marker was clicked on
	// and respond appropriately
	private void checkCitiesForClick()
	{
		if (lastClicked != null) return;
		// Loop over the earthquake markers to see if one of them is selected
		for (Marker marker : cityMarkers) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker)marker;
				// Hide all the other earthquakes and hide
				for (Marker mhide : cityMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : quakeMarkers) {
					EarthquakeMarker quakeMarker = (EarthquakeMarker)mhide;
					if (quakeMarker.getDistanceTo(marker.getLocation()) 
							> quakeMarker.threatCircle()) {
						quakeMarker.setHidden(true);
					}
				}
				return;
			}
		}		
	}
	
	// Helper method that will check if an earthquake marker was clicked on
	// and respond appropriately
	private void checkEarthquakesForClick()
	{
		if (lastClicked != null) return;
		// Loop over the earthquake markers to see if one of them is selected
		for (Marker m : quakeMarkers) {
			EarthquakeMarker marker = (EarthquakeMarker)m;
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = marker;
				// Hide all the other earthquakes and hide
				for (Marker mhide : quakeMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : cityMarkers) {
					if (mhide.getDistanceTo(marker.getLocation()) 
							> marker.threatCircle()) {
						mhide.setHidden(true);
					}
				}
				return;
			}
		}
	}
	
	// loop over and unhide all markers
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
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
	
	// prints countries with number of earthquakes
	// You will want to loop through the country markers or country features
	// (either will work) and then for each country, loop through
	// the quakes to count how many occurred in that country.
	// Recall that the country markers have a "name" property, 
	// And LandQuakeMarkers have a "country" property set.
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake feature if 
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}
	
}
