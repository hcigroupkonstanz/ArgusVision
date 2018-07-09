package control;

import processing.core.PApplet;
import processing.core.PFont;

import controlP5.*;

/**
 * This class hosts the Main Windows of the Client Application
 * 
 * @author Moritz Skowronski
 *
 */
public class Display extends PApplet {

	private Control control;

	/*-------------Various Colors-------------*/
	private int backgroundColor;
	private int controlColor;
	private int controlColor2;
	private int dockPanelColor;
	private int extraColor;

	// GUI
	private ControlP5 guiControl;
	private PFont font;
	private PFont textAreaFont;
	private PFont headerFont;

	@Override
	public void settings() {
		size(300, 400, P2D);
		smooth();
	}

	@Override
	public void setup() {
		surface.setTitle("Argus Vision Kinect");
		surface.setLocation(0, 0);
		// Setting up color
		colorMode(RGB);
		font = createFont("fontSmall.otf", 9);
		headerFont = createFont("fontBig.otf", 28);
		textAreaFont = createFont("fontBig.otf", 8);

		extraColor = color(207, 23, 93);
		controlColor = color(204, 82, 122);
		controlColor2 = color(232, 23, 93);
		dockPanelColor = color(54, 54, 54);
		backgroundColor = color(130, 130, 130);
		background(backgroundColor);

		/**
		 * Controllers
		 */

		control = new Control(this);
		guiControl = new ControlP5(this);
		guiControl.setColorBackground(controlColor);
		guiControl.setColorActive(controlColor2);
		guiControl.setColorForeground(extraColor);
		guiControl.setFont(font);
		guiControl.addTextfield("kinectname").setPosition(50, 120).setColorBackground(controlColor)
				.setFont(textAreaFont).setAutoClear(false).setCaptionLabel("Kinect Name");
		guiControl.addTextfield("broadcastport").setPosition(50, 170).setFont(textAreaFont).setAutoClear(false)
				.setCaptionLabel("Broadcast Port").setInputFilter(ControlP5.INTEGER);
		guiControl.addToggle("broadcast").setPosition(50, 270).setSize(200, 100).setCaptionLabel("Broadcast")
				.align(ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER);

		// try to load configuration on startup
		guiControl.loadProperties("./data/settings");
	}

	@Override
	public void draw() {
		// Look & Feel
		background(dockPanelColor);
		fill(255);
		textFont(headerFont);
		// Headline
		text("KINECT", 20, 60);
		// Run logic
		control.execute();
//		textFont(textAreaFont);
//		text(frameRate, 40, height - 20);
	}

	/**
	 * Event handler for click Broadcast event
	 * 
	 * @param value
	 */
	public void broadcast(boolean value) {
		if (value) {
			control.setBroadcast(guiControl.get(Textfield.class, "kinectname").getText(),
					Integer.parseInt(guiControl.get(Textfield.class, "broadcastport").getText()));
			guiControl.get(Textfield.class, "kinectname").setLock(true);
			guiControl.get(Textfield.class, "broadcastport").setLock(true);
			control.broadCast(true);
			// save Properties so they're still there on restart
			guiControl.saveProperties("./data/settings");
		} else {
			guiControl.get(Textfield.class, "kinectname").setLock(false);
			guiControl.get(Textfield.class, "broadcastport").setLock(false);
			control.broadCast(false);
		}
	}
}