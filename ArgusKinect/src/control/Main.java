package control;


import processing.core.PApplet;

/**
 * Starts the Application
 * @author Moritz Skowronski
 *
 */
public class Main {

	
	public static void main(String[] args) {
		
		String[] startString = { "Start" };
		Display display = new Display();
		PApplet.runSketch(startString, display);
	}
}
