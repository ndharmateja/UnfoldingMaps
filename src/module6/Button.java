package module6;

import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PApplet;
import processing.core.PGraphics;

public class Button{
	private float x1;
	private float y1;
	private float width;
	private float height;
	private boolean pressed;
	private String title;
	private float lowerLimit;
	private float upperLimit;
	public Button(float x1, float y1, float width, float height, String title, float lowerLimit, float upperLimit) {
		this.x1 = x1;
		this.y1 = y1;
		this.width = width;
		this.height = height;
		this.pressed = false;
		this.title = title;
		this.lowerLimit = lowerLimit;		
		this.upperLimit = upperLimit;
	}
	public Button(float x1, float y1, float width, float height, String title) {
		this.x1 = x1;
		this.y1 = y1;
		this.width = width;
		this.height = height;
		this.pressed = false;
		this.title = title;
	}
	public float getx1() {
		return x1;
	}
	public float gety1() {
		return y1;
	}
	public float getWidth() {
		return width;
	}
	public float getHeight() {
		return height;
	}
	public String getTitle() {
		return title;
	}
	public float getLowerLimit() {
		return lowerLimit;
	}
	public float getUpperLimit() {
		return upperLimit;
	}
	public void drawButton(PApplet app, int grayColor) {
		app.fill(grayColor);
		app.rect(x1, y1, width, height);
	}
	public void drawTick(PApplet app) {
		app.fill(0);
		app.line(x1, y1, x1 + width, y1 + height);
		app.line(x1 + width, y1, x1, y1 + height);
	}
	public void toggle() {
		this.pressed = !this.pressed;
	}
	public boolean getPressed() {
		return pressed;
	}
	public void setPressed(boolean state) {
		this.pressed = state;
	}
	public String toString() {
		return title + "\t" + lowerLimit + "\t" + upperLimit;
	}
}
