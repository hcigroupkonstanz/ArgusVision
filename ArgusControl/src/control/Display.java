package control;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import remixlab.bias.core.Grabber;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.Scene;

import visualization.KinectImage;
import visualization.InteractiveTriggerbox;

import scene.Person;
import scene.TriggerBox;
import scene.AbstractTriggerZone;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import communication.KinectConnectListener;
import communication.OSCSender;

import controlP5.*;

import helpers.Enum.DisplayMode;
import helpers.Enum.Streams;
import helpers.KinectTransformations;

/**
 * Display Class. Provides UI and a lot of Processing related functionality This
 * class is very bloated, due to the UI functionality
 * 
 * @author Moritz Skowronski
 *
 */
public class Display extends PApplet implements KinectConnectListener {

	/*-------------Various Colors-------------*/
	private int backgroundColor;
	private int controlColor2;
	private int dockPanelColor;
	private int extraColor;

	/*------------Controls--------------*/

	private Toggle startDepthCalibration;
	private Toggle multiCameraCalibration;
	private Slider threshold;
	private Slider contoursize;
	private Toggle startTracking;

	private Textfield receivePort;
	private Textfield sendPort;
	private Textfield sendIP;
	private Toggle startServer;
	private Toggle startSending;

	private ButtonBar kinects;

	private Toggle activeImage;
	private Toggle depthImage;
	private Toggle infraredImage;
	private Toggle maskImage;
	private Toggle pointCloud;

	private Toggle serverSettings;
	private Toggle triggerzoneSettings;

	private ScrollableList triggerzoneList;
	private Bang addTriggerzone;
	private Bang deleteTriggerzone;
	private Numberbox triggerzoneX;
	private Numberbox triggerzoneY;
	private Numberbox triggerzoneZ;
	private Numberbox triggerzoneDepth;
	private Numberbox triggerzoneWidth;
	private Numberbox triggerzoneHeight;

	private Numberbox triggerzoneRotateX;
	private Numberbox triggerzoneRotateY;
	private Numberbox triggerzoneRotateZ;

	private ControlP5 guiControl;
	private PFont font;
	private PFont textAreaFont;
	private PFont headerFont;

	/*--------------------Model--------------------*/

	private Control controller;

	/*------Used for switching between States------------*/

	private DisplayMode displaySwitch;

	private String activeTriggerzone;

	private String activeKinect;

	/*-----------------------Images----------------*/

	private KinectImage imageFromKinect;
	private PImage streamingImage;

	/*----Used to Display Point Cloud & Triggerzones and make them interactive----*/
	private PGraphics pointCloud3D;
	private Scene pointCloudScene;

	private int[] depthData;

	public Display(Control controller) {

		this.controller = controller;
		activeKinect = null;
		displaySwitch = DisplayMode.Server;
		streamingImage = createImage(682, 565, ARGB);
	}

	@Override
	public void settings() {

		size(1112, 845, P3D);
		smooth();
	}

	@Override
	public void setup() {

		surface.setTitle("Argus Vision Control");
		// Setting up color
		colorMode(RGB);
		font = createFont("fontSmall.otf", 9);
		headerFont = createFont("fontBig.otf", 24);
		textAreaFont = createFont("fontBig.otf", 9);

		extraColor = color(207, 23, 93);
		controlColor2 = color(232, 23, 93);
		dockPanelColor = color(54, 54, 54);
		backgroundColor = color(130, 130, 130);
		background(backgroundColor);

		guiControl = new ControlP5(this);

		guiControl.setColorBackground(backgroundColor);
		guiControl.setColorActive(extraColor);
		guiControl.setColorForeground(controlColor2);
		guiControl.setFont(font);

		// This is used to save the server and OSC sending settings, so they are
		// automatically loaded on startup
		guiControl.getProperties().addSet("communicationSettings");

		/*--------------------Local Kinect Settings------------------*/

		startDepthCalibration = new Toggle(guiControl, "startDepthCalibration");
		startDepthCalibration.setPosition(50, 720).setSize(200, 100).setCaptionLabel("Depth Calibration")
				.align(ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER);

		multiCameraCalibration = new Toggle(guiControl, "multiCameraCalibration");
		multiCameraCalibration.setPosition(321, 720).setSize(200, 100).setCaptionLabel("Camera Calibration")
				.align(ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER);

		threshold = new Slider(guiControl, "threshold");
		threshold.setPosition(592, 720).setRange(5, 1000).setSize(200, 20).setCaptionLabel("Threshold")
				.setDecimalPrecision(1).setColorActive(controlColor2).setColorForeground(extraColor)
				.align(ControlP5.BOTTOM_OUTSIDE, ControlP5.BOTTOM_OUTSIDE, ControlP5.BOTTOM_OUTSIDE,
						ControlP5.BOTTOM_OUTSIDE);

		contoursize = new Slider(guiControl, "contoursize");
		contoursize.setPosition(592, 780).setRange(1000, 100000).setSize(200, 20).setCaptionLabel("Minimum Contoursize")
				.setColorActive(controlColor2).setColorForeground(extraColor).align(ControlP5.BOTTOM_OUTSIDE,
						ControlP5.BOTTOM_OUTSIDE, ControlP5.BOTTOM_OUTSIDE, ControlP5.BOTTOM_OUTSIDE);

		startTracking = new Toggle(guiControl, "startTracking");
		startTracking.setPosition(863, 720).setSize(200, 100).setCaptionLabel("Start Tracking").align(ControlP5.CENTER,
				ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER);

		/*-------------------Global Kinect Settings-----------------*/

		kinects = new ButtonBar(guiControl, "kinects");

		kinects.setColorBackground(dockPanelColor).setPosition(0, 645).setSize(width, 50);

		/*--------------------Receive Port Settings-----------------*/

		receivePort = guiControl.addTextfield("receivePort");
		receivePort.setPosition(50, 173).setFont(textAreaFont).setAutoClear(false).setCaptionLabel("Port")
				.setColorForeground(0).setInputFilter(ControlP5.INTEGER);

		startServer = guiControl.addToggle("startServer");
		startServer.setPosition(50, 223).setSize(200, 100).setCaptionLabel("Start Argus").align(ControlP5.CENTER,
				ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER);

		guiControl.getProperties().copy(receivePort, "default", "communicationSettings");

		/*----------------------Send Port Settings------------------*/

		sendIP = guiControl.addTextfield("sendIP");
		sendIP.setPosition(50, 403).setFont(textAreaFont).setAutoClear(false).setCaptionLabel("IP")
				.setColorForeground(0);

		sendPort = guiControl.addTextfield("sendPort");
		sendPort.setPosition(50, 453).setFont(textAreaFont).setAutoClear(false).setCaptionLabel("Port")
				.setColorActive(controlColor2).setColorForeground(0);

		startSending = guiControl.addToggle("startSending");
		startSending.setPosition(50, 503).setSize(200, 100).setCaptionLabel("Send Data").align(ControlP5.CENTER,
				ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER);

		guiControl.getProperties().copy(sendIP, "default", "communicationSettings");
		guiControl.getProperties().copy(sendPort, "default", "communicationSettings");

		/*----------------------Image Streaming Settings------------------*/

		depthImage = new Toggle(guiControl, "depthImage");
		depthImage.setPosition(418, 10).setSize(155, 40).setCaptionLabel("Depth")
				.align(ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER)
				.setColorBackground(dockPanelColor);

		infraredImage = new Toggle(guiControl, "infraredImage");
		infraredImage.setPosition(588, 10).setSize(155, 40).setCaptionLabel("Infrared")
				.align(ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER)
				.setColorBackground(dockPanelColor);

		pointCloud = new Toggle(guiControl, "pointCloud");
		pointCloud.setPosition(758, 10).setSize(155, 40).setCaptionLabel("Point Cloud")
				.align(ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER)
				.setColorBackground(dockPanelColor);

		maskImage = new Toggle(guiControl, "maskImage");
		maskImage.setPosition(928, 10).setSize(155, 40).setCaptionLabel("Tracking")
				.align(ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER)
				.setColorBackground(dockPanelColor);

		/*-------------------Triggerzones-------------*/

		triggerzoneList = new ScrollableList(guiControl, "triggerzoneList");
		triggerzoneList.setPosition(0, 120).setSize(350, 240).setBarVisible(false).setType(ScrollableList.LIST)
				.setItemHeight(30).hide();

		addTriggerzone = new Bang(guiControl, "addTriggerzone");
		addTriggerzone.setPosition(10, 410).setSize(155, 30).setCaptionLabel("Add")
				.align(ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER)
				.setColorForeground(backgroundColor).hide();

		deleteTriggerzone = new Bang(guiControl, "deleteTriggerzone");
		deleteTriggerzone.setPosition(185, 410).setSize(155, 30).setCaptionLabel("Delete")
				.align(ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER)
				.setColorForeground(backgroundColor).hide();

		triggerzoneX = new Numberbox(guiControl, "triggerzoneX");
		triggerzoneX.setPosition(10, 460).setSize(100, 26).setCaptionLabel("Pos. X").setMin(-4500).setMax(4500).hide()
				.setValue(0f);

		triggerzoneY = new Numberbox(guiControl, "triggerzoneY");
		triggerzoneY.setPosition(126, 460).setSize(100, 26).setCaptionLabel("Pos. Y").setMin(-4500).setMax(4500).hide()
				.setValue(0f);

		triggerzoneZ = new Numberbox(guiControl, "triggerzoneZ");
		triggerzoneZ.setPosition(242, 460).setSize(100, 26).setCaptionLabel("Pos. Z").setMin(-4500).setMax(4500).hide()
				.setValue(0f);

		triggerzoneWidth = new Numberbox(guiControl, "triggerzoneWidth");
		triggerzoneWidth.setPosition(10, 520).setSize(100, 26).setCaptionLabel("Width").setMin(0).hide().setValue(0f);

		triggerzoneHeight = new Numberbox(guiControl, "triggerzoneHeight");
		triggerzoneHeight.setPosition(126, 520).setSize(100, 26).setCaptionLabel("Height").setMin(0).hide()
				.setValue(0f);

		triggerzoneDepth = new Numberbox(guiControl, "triggerzoneDepth");
		triggerzoneDepth.setPosition(242, 520).setSize(100, 26).setCaptionLabel("Depth").setMin(0).hide().setValue(0f);

		triggerzoneRotateX = new Numberbox(guiControl, "triggerzoneRotateX");
		triggerzoneRotateX.setPosition(10, 580).setSize(100, 26).setCaptionLabel("Rotate X").hide()
				.setDecimalPrecision(3).setRange(-10, 10).setMultiplier(0.001f).setValue(0f);

		triggerzoneRotateY = new Numberbox(guiControl, "triggerzoneRotateY");
		triggerzoneRotateY.setPosition(126, 580).setSize(100, 26).setCaptionLabel("Rotate Y").hide()
				.setDecimalPrecision(3).setRange(-10, 10).setMultiplier(0.001f).setValue(0f);

		triggerzoneRotateZ = new Numberbox(guiControl, "triggerzoneRotateZ");
		triggerzoneRotateZ.setPosition(242, 580).setSize(100, 26).setCaptionLabel("Rotate Z").hide()
				.setDecimalPrecision(3).setRange(-10, 10).setMultiplier(0.001f).setValue(0f);

		/*-------------------Settings-----------------*/

		serverSettings = new Toggle(guiControl, "serverSettings");
		serverSettings.setPosition(0, 85).setSize(175, 25).setCaptionLabel("Settings").align(ControlP5.CENTER,
				ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER);

		triggerzoneSettings = new Toggle(guiControl, "triggerzoneSettings");
		triggerzoneSettings.setPosition(175, 85).setSize(175, 25).setCaptionLabel("Triggerzones")
				.align(ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER, ControlP5.CENTER);

		serverSettings.setValue(true);

		guiControl.setAutoDraw(false);

		pointCloud3D = createGraphics(682, 565, P3D);

		pointCloudScene = new Scene(this, pointCloud3D, 410, 60);
		// Kinect Radius is 8000, so Camera Radius should be 8000 as well
		pointCloudScene.setRadius(8000);
		// Set Position to Kinect Position
		pointCloudScene.camera().setPosition(0, 0, 0);
		// Because of the Kinect point 3D transformation we have to mirror the
		// image along the x axis
		pointCloudScene.camera().setOrientation(radians(180), 0);
		// Switches the camera from a sort-of movie mode to a standard CAD
		// camera, as used in
		// Applications like Cinema4D&3D Studio Max
		pointCloudScene.eyeFrame().setMotionBinding(LEFT, "rotateCAD");
		pointCloudScene.setPickingVisualHint(true);
		pointCloudScene.setGridVisualHint(false);
		pointCloudScene.setAxesVisualHint(false);

		controller.setScene(pointCloudScene);

		guiControl.loadProperties("./data/communicationSettings");

		// Try to start up Server
		startServer.setValue(true);
		startSending.setValue(true);
	}

	@Override
	public void draw() {

		background(backgroundColor);
		drawGUI();
		testForNewBackgroundZones();

		if (activeKinect != null) {
			if (imageFromKinect != null && imageFromKinect.hasNew()) {
				if (activeImage != null && activeImage != pointCloud)
					streamingImage = imageFromKinect.getImage();
			}
			if (activeImage != null) {
				if (activeImage != pointCloud) {

					drawImage();
				} else {

					drawPointCloud(true);
				}
			}
		}
		stroke(0);
		fill(255);
	}

	/**
	 * Test if there were any new Triggerzones loaded in the background. If so,
	 * add them
	 */
	private void testForNewBackgroundZones() {

		if (controller.triggerzonesChangedBang()) {
			Iterator<Entry<String, AbstractTriggerZone[]>> iterator = controller.getTemporaryZones().entrySet()
					.iterator();
			while (iterator.hasNext()) {

				Entry<String, AbstractTriggerZone[]> entry = iterator.next();
				AbstractTriggerZone[] zones = entry.getValue();

				int parsedInt = 0;
				for (int i = 0; i < zones.length; i++) {
					controller.addTriggerZone(zones[i], entry.getKey());
					parsedInt = Integer.parseInt(zones[i].getID().substring(11));
				}
				iterator.remove();
				TriggerBox.setStartCounter(++parsedInt);
			}
		}
	}

	/**
	 * Draws 2D Images. Either standard 2D stream or visualization of the
	 * tracking
	 */
	private void drawImage() {

		// Draw Image on Screen
		image(streamingImage, 410, 60, 682, 565);

		if (startTracking.getState()) {

			// Make a black rectangle
			fill(0);
			rect(410, 60, 682, 565);

			// Draw Contours & Centroid on Top
			for (Person person : controller.getPersonsForScene(activeKinect)) {

				if (person == null)
					continue;

				fill(255);
				noStroke();

				// Draw Centroid
				ellipse(person.getCentroid().x * 685 + 410, person.getCentroid().y * 565 + 60, 5, 5);

				// Draw Contours
				strokeWeight(2);
				stroke(color(person.getColor()));
				noFill();
				beginShape();
				for (PVector contourPoint : person.getContour()) {
					vertex(contourPoint.x * 685 + 410, contourPoint.y * 565 + 60);
				}
				endShape(PApplet.CLOSE);
			}
		}
		stroke(0);

		// Draw Triggerzones from the Point Cloud on top of the 2D image
		drawPointCloud(false);
	}

	/**
	 * Draws point cloud & 3D Triggerzones onto the screen. If true draws
	 * pointcloud & triggerzones with hard background, if false draws only
	 * triggerzones on a transparent background to overlay them on top of 2d
	 * image
	 * 
	 * @param drawPointCloud
	 */
	private void drawPointCloud(boolean drawPointCloud) {

		// Construct Point Cloud in Background Buffer
		// This is done so transformations have no effect on the
		// GUI
		depthData = imageFromKinect.returnDepthData();

		// Only allow mouse control in Image Screen
		handleMouse();

		pointCloud3D.beginDraw();
		if (drawPointCloud)
			pointCloud3D.background(0);
		else
			pointCloud3D.background(0, 0);
		pointCloud3D.stroke(255);
		pointCloudScene.beginDraw();

		switchOnHover();

		// Draw all Triggerzones first
		for (InteractiveTriggerbox interactiveTriggerbox : controller.getDisplayableBoxForKinect(activeKinect)) {
			if (interactiveTriggerbox.getID() == activeTriggerzone)
				interactiveTriggerbox.draw(true, extraColor);
			else
				interactiveTriggerbox.draw(false, 0);
		}

		if (drawPointCloud) {

			for (int x = 0; x < 512; x += 4) {
				for (int y = 0; y < 424; y += 4) {
					// calculate the x, y, z camera position based on
					// the
					// depth
					// information

					PVector point = KinectTransformations.depthToPointCloudPos(x, y, depthData[y / 4 * 256/2 + x / 4]);

					// Draw a point (using line instead because it is
					// faster)
					pointCloud3D.line(-point.x, point.y, point.z, -(point.x + 1), point.y + 1, point.z + 1);
				}
			}
		}

		pointCloudScene.endDraw();
		pointCloud3D.endDraw();
		image(pointCloud3D, 410, 60);
	}

	/**
	 * Checks the picking of the point Cloud scene. If the grabber is not null,
	 * meaning we are hovering above a Triggerzone, select it in the scrollabe
	 * list
	 */
	private void switchOnHover() {

		Grabber grabber = pointCloudScene.motionAgent().inputGrabber();

		if (grabber != null) {

			if (grabber.getClass() == InteractiveFrame.class) {

				// if the grabbed thing is an interactive frame, search which
				// triggerbox it belongs to
				InteractiveFrame grabFrame = (InteractiveFrame) grabber;
				for (InteractiveTriggerbox interactiveTriggerbox : controller
						.getDisplayableBoxForKinect(activeKinect)) {

					if (grabFrame.equals(interactiveTriggerbox.getInteractiveFrame())) {

						activeTriggerzone = interactiveTriggerbox.getID();
						break;
					}
				}

				List<Map<String, Object>> items = triggerzoneList.getItems();
				int index = 0;
				// This again is an ugly workaround for a missing index access
				// functionality of controlp5
				for (Map<String, Object> o : items) {
					if (activeTriggerzone.equals(o.get("name"))) {
						triggerzoneList.setValue(index);
					}
					index++;
				}
			}
		}
	}

	/**
	 * Disables Mousehandling of 3D Pointcloud when mouse isn't hovering over
	 * the image
	 */
	private void handleMouse() {

		if (mouseX > 410 && mouseX < 1095 && mouseY > 60 && mouseY < 625 && activeImage == pointCloud) {
			pointCloudScene.setPickingVisualHint(true);
			pointCloudScene.enableMotionAgent();

			if (activeKinect != null) {

				// adding the Grabber for every triggerzone, this is needed
				// because the mousehandler loses grabbers when disabled..
				for (InteractiveTriggerbox box : controller.getDisplayableBoxForKinect(activeKinect)) {

					pointCloudScene.motionAgent().addGrabber(box.getInteractiveFrame());
				}
			}

		} else {

			pointCloudScene.setPickingVisualHint(false);
			pointCloudScene.disableMotionAgent();
		}
	}

	/**
	 * Draws the GUI
	 */
	private void drawGUI() {
		hint(DISABLE_DEPTH_TEST);
		camera();
		fill(dockPanelColor);

		// TODO change like the others
		rect(50, 720, 200, 100);
		rect(321, 720, 200, 100);
		rect(863, 720, 200, 100);
		if (displaySwitch == DisplayMode.Server) {
			rect(startServer.getPosition()[0], startServer.getPosition()[1], startServer.getWidth(),
					startServer.getHeight());
			rect(startSending.getPosition()[0], startSending.getPosition()[1], startSending.getWidth(),
					startSending.getHeight());
		}

		rect(threshold.getPosition()[0], threshold.getPosition()[1], threshold.getWidth(), threshold.getHeight());
		rect(contoursize.getPosition()[0], contoursize.getPosition()[1], contoursize.getWidth(),
				contoursize.getHeight());
		// Side Dock
		rect(0, 0, 350, 645);
		// Image Holder
		rect(410, 60, 682, 565);
		// Button Bar
		rect(kinects.getPosition()[0], kinects.getPosition()[1], kinects.getWidth(), kinects.getHeight());
		// Slider Control
		rect(0, 695, width, 150);
		// Image Buttons
		rect(depthImage.getPosition()[0], depthImage.getPosition()[1], depthImage.getWidth(), depthImage.getHeight());
		rect(infraredImage.getPosition()[0], infraredImage.getPosition()[1], infraredImage.getWidth(),
				infraredImage.getHeight());
		rect(maskImage.getPosition()[0], maskImage.getPosition()[1], maskImage.getWidth(), maskImage.getHeight());
		rect(pointCloud.getPosition()[0], pointCloud.getPosition()[1], pointCloud.getWidth(), pointCloud.getHeight());

		rect(serverSettings.getPosition()[0], serverSettings.getPosition()[1], serverSettings.getWidth(),
				serverSettings.getHeight());
		rect(triggerzoneSettings.getPosition()[0], triggerzoneSettings.getPosition()[1], triggerzoneSettings.getWidth(),
				triggerzoneSettings.getHeight());

		fill(255);
		textFont(headerFont);
		// Headline
		text("CONTROL", 20, 60);
		// Descriptions
		fill(255);
		textFont(textAreaFont);
		if (displaySwitch == DisplayMode.Server) {
			text("Receiver Settings", 50, 153);
			text("Send To Settings", 50, 383);
		} else {
			if (!triggerzoneList.isOpen())
				triggerzoneList.setOpen(true);
		}

		guiControl.draw();
		hint(ENABLE_DEPTH_TEST);
	}

	/**
	 * Click Event Handler for Button Bar
	 * 
	 * @param n
	 *            the index of the item that has been clicked
	 */
	public void kinects(int n) {
		String tempKinect = activeKinect;
		String[] values;
		List<Map<String, Object>> templist = kinects.getItems();
		values = new String[templist.size()];
		int counter = 0;

		for (Map<String, Object> map : templist) {
			values[counter++] = (String) map.get("name");
			// Search for clicked Kinect via Name
			if ((Integer) map.get("value") == n) {
				activeKinect = (String) map.get("name");
				// If found
				// Show Settings of chosen Kinect
				Settings kinectSettings = controller.getSettings(activeKinect);
				if (kinectSettings != null) {
					startTracking.setValue(kinectSettings.isTracking());
					threshold.setValue(kinectSettings.getThreshold());
					contoursize.setValue(kinectSettings.getMinimumContourSize());
					startDepthCalibration.setValue(kinectSettings.isRunCalibration());
					multiCameraCalibration.setValue(kinectSettings.isRunMultiKinectCalibration());
				}
			}
		}

		// This is an ugly workaround for missing functionality in controlP5
		// clears all items in the buttonbar and adds them anew
		// This is needed to return to a "default mode", not showing any kinect
		if (activeKinect == tempKinect) {

			kinects.clear();
			kinects.addItems(values);
			controller.setStreaming(activeKinect, Streams.NOSEND);
			activeKinect = null;
			startTracking.setValue(false);
			threshold.setValue(0);
			contoursize.setValue(0);
			startDepthCalibration.setValue(false);
			multiCameraCalibration.setValue(false);
		}
		// Disable Streaming of old Kinect
		if (tempKinect != null)
			controller.setStreaming(tempKinect, Streams.NOSEND);

		updateStreamSettings();
		updateZoneList();
	}

	/**
	 * Switches between different Streaming Modes
	 */
	private void updateStreamSettings() {
		Streams stream = Streams.NOSEND;
		if (activeKinect != null) {
			if (activeImage != null) {
				switch (activeImage.getAddress()) {
				case "/depthImage":
					stream = Streams.DEPTH;
					break;
				case "/infraredImage":
					stream = Streams.INFRARED;
					break;
				case "/maskImage":
					stream = Streams.MASK;
					break;
				case "/pointCloud":
					stream = Streams.POINTCLOUD;
					break;
				default:
					stream = Streams.NOSEND;
				}
			} else {
				stream = Streams.NOSEND;
			}
			if (startTracking.getBooleanValue() && stream != Streams.POINTCLOUD)
				stream = Streams.NOSEND;
			controller.setStreaming(activeKinect, stream);
		}
		pointCloudScene.camera().setPosition(0, 0, 0);
		pointCloudScene.camera().setOrientation(radians(180), 0);
	}

	/**
	 * Event Handler for Start Server Toggle
	 * 
	 * @param isActive
	 */
	public void startServer(boolean isActive) {

		if (isActive) {
			try {
				controller.serverStart(Integer.parseInt(receivePort.getText()));
				receivePort.setColorBackground(controlColor2);
				receivePort.setLock(true);
				guiControl.saveProperties("./data/communicationSettings", "communicationSettings");
			} catch (NumberFormatException e) {
				startServer.setValue(false);
			}
		} else {
			kinects.clear();
			controller.serverStop();
			activeKinect = null;
			receivePort.setColorBackground(backgroundColor);
			receivePort.setLock(false);
			guiControl.saveProperties("./data/communicationSettings", "communicationSettings");
		}
	}

	/**
	 * Event Handler for Start Send Toggle
	 * 
	 * @param isActive
	 */
	public void startSending(boolean isActive) {
		if (isActive) {
			String ipAdress = sendIP.getText();
			try {
				int port = Integer.parseInt(sendPort.getText());
				if (OSCSender.setNetAddress(ipAdress, port)) {

					sendIP.setColorBackground(controlColor2);
					sendPort.setColorBackground(controlColor2);
					sendIP.setLock(true);
					sendPort.setLock(true);
					guiControl.saveProperties("./data/communicationSettings", "communicationSettings");
				} else {
					startSending.setValue(false);
				}
			} catch (NumberFormatException e) {
				startSending.setValue(false);
			}
		} else {
			OSCSender.stop();
			sendIP.setLock(false);
			sendPort.setLock(false);
			sendIP.setColorBackground(backgroundColor);
			sendPort.setColorBackground(backgroundColor);
			guiControl.saveProperties("./data/communicationSettings", "communicationSettings");
		}
	}

	/**
	 * Event Handler for Start Depth Calibration Toggle
	 * 
	 * @param isActive
	 */
	public void startDepthCalibration(boolean isActive) {

		if (activeKinect != null) {
			controller.setDepthCalibration(activeKinect, isActive);
		}
	}

	/**
	 * Event Handler for Start Camera Calibration Toggle
	 * 
	 * @param isActive
	 */
	public void startMultiCameraCalibration(boolean isActive) {

		if (activeKinect != null) {
			controller.setMultiCameraCalibration(activeKinect, isActive);
		}
	}

	/**
	 * Event Handler for Threshold Slider
	 * 
	 * @param i
	 */
	public void threshold(int i) {
		if (activeKinect != null) {
			controller.setThreshold(activeKinect, i);
		}
	}

	/**
	 * Event Handler for Contoursize Slider
	 * 
	 * @param i
	 */
	public void contoursize(int i) {
		if (activeKinect != null) {
			controller.setContoursize(activeKinect, i);
		}
	}

	/**
	 * Event Handler for Start Tracking Toggle
	 * 
	 * @param isActive
	 */
	public void startTracking(boolean isActive) {
		if (activeKinect != null) {
			controller.setTracking(activeKinect, isActive);
			if (isActive)
				controller.setStreaming(activeKinect, Streams.NOSEND);
			else
				updateStreamSettings();
		}
	}

	public void kinectRecognized(String name, String ip) {

		List<Map<String, Object>> templist = kinects.getItems();

		boolean alreadyConnected = false;
		for (Map<String, Object> map : templist) {
			if (map.containsValue(name)) {
				alreadyConnected = true;
			}
		}

		if (!alreadyConnected) {
			kinects.addItem(name, templist.size());
			controller.kinectRecognized(name, ip);
		}
	}

	/**
	 * Event Handler for depth Image Toggle
	 * 
	 * @param isActive
	 */
	public void depthImage(boolean isActive) {
		if (isActive) {
			if (activeImage == null) {
				activeImage = depthImage;
			} else {
				makeImageBlack();
				activeImage.setValue(false);
				activeImage = depthImage;
			}
		} else {
			activeImage = null;
		}
		updateStreamSettings();
	}

	/**
	 * Event Handler for Infrared Image Toggle
	 * 
	 * @param isActive
	 */
	public void infraredImage(boolean isActive) {
		if (isActive) {
			if (activeImage == null) {
				activeImage = infraredImage;
			} else {
				makeImageBlack();
				activeImage.setValue(false);
				activeImage = infraredImage;
			}
		} else {
			activeImage = null;
		}
		updateStreamSettings();
	}

	/**
	 * Event Handler for Mask Image Toggle
	 * 
	 * @param isActive
	 */
	public void maskImage(boolean isActive) {
		if (isActive) {
			if (activeImage == null) {
				activeImage = maskImage;
			} else {
				makeImageBlack();
				activeImage.setValue(false);
				activeImage = maskImage;
			}
		} else {
			activeImage = null;
		}
		updateStreamSettings();
	}

	/**
	 * Event Handler for Point Cloud Image Toggle
	 * 
	 * @param isActive
	 */
	public void pointCloud(boolean isActive) {
		if (isActive) {
			if (activeImage == null) {
				activeImage = pointCloud;
			} else {
				makeImageBlack();
				activeImage.setValue(false);
				activeImage = pointCloud;
			}
		} else {
			activeImage = null;
		}
		updateStreamSettings();
	}

	private void makeImageBlack() {
		streamingImage.loadPixels();
		for (int i = 0; i < streamingImage.pixels.length; i++) {
			streamingImage.pixels[i] = 0;
		}
		streamingImage.updatePixels();
	}

	/**
	 * Sets the imageHandler from which the Kinect Stream is derived
	 * 
	 * @param image
	 */
	public void setImageFromKinect(KinectImage image) {
		this.imageFromKinect = image;
	}

	/**
	 * Handler for change Settings "Buttonbar"
	 * 
	 * @param isActive
	 */
	public void serverSettings(boolean isActive) {
		if (isActive) {

			startSending.show();
			startServer.show();
			receivePort.show();
			sendIP.show();
			sendPort.show();
			addTriggerzone.hide();
			deleteTriggerzone.hide();
			triggerzoneList.hide();
			triggerzoneDepth.hide();
			triggerzoneHeight.hide();
			triggerzoneRotateX.hide();
			triggerzoneRotateY.hide();
			triggerzoneRotateZ.hide();
			triggerzoneWidth.hide();
			triggerzoneX.hide();
			triggerzoneY.hide();
			triggerzoneZ.hide();
			triggerzoneSettings.setValue(false);
			displaySwitch = DisplayMode.Server;
		} else {
			if (!triggerzoneSettings.getBooleanValue())
				serverSettings.setValue(true);
		}
	}

	/**
	 * Handler for change Settings "Buttonbar"
	 * 
	 * @param isActive
	 */
	public void triggerzoneSettings(boolean isActive) {
		if (isActive) {

			startSending.hide();
			startServer.hide();
			receivePort.hide();
			sendIP.hide();
			sendPort.hide();
			addTriggerzone.show();
			deleteTriggerzone.show();
			triggerzoneList.show();
			triggerzoneDepth.show();
			triggerzoneHeight.show();
			triggerzoneRotateX.show();
			triggerzoneRotateY.show();
			triggerzoneRotateZ.show();
			triggerzoneWidth.show();
			triggerzoneX.show();
			triggerzoneY.show();
			triggerzoneZ.show();
			serverSettings.setValue(false);
			displaySwitch = DisplayMode.Triggerzone;

		} else {
			if (!serverSettings.getBooleanValue())
				triggerzoneSettings.setValue(true);
		}
	}

	/**
	 * Event Handler for "add Triggerzone" Button
	 */
	public void addTriggerzone() {

		AbstractTriggerZone zone = new TriggerBox(0, 0, 2000, 400, 400, 400);
		// Add only if a Kinect and Image are selected
		if (activeKinect != null) {
			if (activeImage != null) {

				controller.addTriggerZone(zone, activeKinect);
				updateZoneList();
			}
		}
		controller.updateTriggerzones(activeKinect);

	}

	/**
	 * Changes the values of the numberboxes if the value of the scrollable list
	 * changes.
	 * 
	 * @param value
	 *            the index of the active Item in the list
	 */
	public void triggerzoneList(int value) {

		activeTriggerzone = (String) triggerzoneList.getItem(value).get("name");

		AbstractTriggerZone triggerzone = controller.getTriggerZoneForKinect(activeKinect, activeTriggerzone);

		// Set display values

		float[] size = triggerzone.getSize();

		triggerzoneDepth.setValue(size[2]);
		triggerzoneHeight.setValue(size[1]);
		triggerzoneWidth.setValue(size[0]);
		triggerzoneX.setValue(triggerzone.getX());
		triggerzoneY.setValue(triggerzone.getY());
		triggerzoneZ.setValue(triggerzone.getZ());
		triggerzoneRotateX.setValue(triggerzone.getRotateX());
		triggerzoneRotateY.setValue(triggerzone.getRotateY());
		triggerzoneRotateZ.setValue(triggerzone.getRotateZ());
	}

	/**
	 * Clears the triggerzonelist and fills it with the triggerzones of the
	 * currently selected kinect
	 */
	private void updateZoneList() {

		if (activeKinect != null) {

			triggerzoneList.clear();

			for (AbstractTriggerZone zone : controller.getTriggerzonesForKinect(activeKinect)) {

				triggerzoneList.addItem(zone.getID(), zone.getID());
				CColor color = new CColor();
				color.setBackground(color(zone.getColor(), 150));
				triggerzoneList.getItem(zone.getID()).put("color", color);
			}
		} else {
			triggerzoneList.clear();
		}
	}

	/**
	 * Handler for "Delete Triggerzone" Click Event
	 */
	public void deleteTriggerzone() {

		if (activeKinect != null) {
			if (activeTriggerzone != null) {

				InteractiveTriggerbox[] boxes = controller.getDisplayableBoxForKinect(activeKinect);

				// Remove the interactive box
				for (int i = 0; i < boxes.length; i++) {

					if (boxes[i].getID() == activeTriggerzone) {
						pointCloudScene.motionAgent().removeGrabber(boxes[i].getInteractiveFrame());
						break;
					}
				}

				controller.deleteTriggerzone(activeKinect, activeTriggerzone);

				updateZoneList();
				activeTriggerzone = null;
			}
			controller.updateTriggerzones(activeKinect);
		}
	}

	/**
	 * Sets the x value of the Triggerzone
	 * 
	 * @param value
	 */
	public void triggerzoneX(int value) {

		if (activeTriggerzone != null) {
			AbstractTriggerZone triggerZone = controller.getTriggerZoneForKinect(activeKinect, activeTriggerzone);
			triggerZone.setX(value);
		}
		controller.updateTriggerzones(activeKinect);
	}

	/**
	 * Sets the y value of the Triggerzone
	 * 
	 * @param value
	 */
	public void triggerzoneY(int value) {

		if (activeTriggerzone != null) {
			AbstractTriggerZone triggerZone = controller.getTriggerZoneForKinect(activeKinect, activeTriggerzone);
			triggerZone.setY(value);
		}
		controller.updateTriggerzones(activeKinect);

	}

	/**
	 * Sets the z value of the Triggerzone
	 * 
	 * @param value
	 */
	public void triggerzoneZ(int value) {

		if (activeTriggerzone != null) {
			AbstractTriggerZone triggerZone = controller.getTriggerZoneForKinect(activeKinect, activeTriggerzone);
			triggerZone.setZ(value);
		}
		controller.updateTriggerzones(activeKinect);

	}

	/**
	 * Sets the Width of a Triggerzone
	 * 
	 * @param value
	 */
	public void triggerzoneWidth(int value) {

		if (activeTriggerzone != null) {
			AbstractTriggerZone triggerZone = controller.getTriggerZoneForKinect(activeKinect, activeTriggerzone);
			triggerZone.setSize(value, triggerZone.getSize()[1], triggerZone.getSize()[2]);
		}

		controller.updateTriggerzones(activeKinect);
	}

	/**
	 * Sets the Height of a Triggerzone
	 * 
	 * @param value
	 */
	public void triggerzoneHeight(int value) {

		if (activeTriggerzone != null) {
			AbstractTriggerZone triggerZone = controller.getTriggerZoneForKinect(activeKinect, activeTriggerzone);
			triggerZone.setSize(triggerZone.getSize()[0], value, triggerZone.getSize()[2]);
		}

		controller.updateTriggerzones(activeKinect);
	}

	/**
	 * Sets the Depth of a Triggerzone
	 * 
	 * @param value
	 */
	public void triggerzoneDepth(int value) {

		if (activeTriggerzone != null) {
			AbstractTriggerZone triggerZone = controller.getTriggerZoneForKinect(activeKinect, activeTriggerzone);
			triggerZone.setSize(triggerZone.getSize()[0], triggerZone.getSize()[1], value);
		}

		controller.updateTriggerzones(activeKinect);
	}

	/**
	 * Sets the X Rotation of a Triggerzone
	 * 
	 * @param value
	 */
	public void triggerzoneRotateX(float value) {

		if (activeTriggerzone != null) {
			AbstractTriggerZone triggerZone = controller.getTriggerZoneForKinect(activeKinect, activeTriggerzone);
			triggerZone.setRotateX(value);

		}

		controller.updateTriggerzones(activeKinect);
	}

	/**
	 * Sets the Y Rotation of a Triggerzone
	 * 
	 * @param value
	 */
	public void triggerzoneRotateY(float value) {

		if (activeTriggerzone != null) {
			AbstractTriggerZone triggerZone = controller.getTriggerZoneForKinect(activeKinect, activeTriggerzone);
			triggerZone.setRotateY(value);

		}

		controller.updateTriggerzones(activeKinect);
	}

	/**
	 * Sets the Z Rotation of a Triggerzone
	 * 
	 * @param value
	 */
	public void triggerzoneRotateZ(float value) {

		if (activeTriggerzone != null) {
			AbstractTriggerZone triggerZone = controller.getTriggerZoneForKinect(activeKinect, activeTriggerzone);
			triggerZone.setRotateZ(value);

		}

		controller.updateTriggerzones(activeKinect);
	}
}
