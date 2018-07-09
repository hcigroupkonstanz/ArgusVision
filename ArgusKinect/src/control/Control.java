package control;

import processing.core.PApplet;
import scene.PersonHandler;
import helpers.OSChooser;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import communication.OSCClient;
import communication.TCPReceiver;
import communication.UDPBroadcaster;
import communication.UDPStreamer;
import helpers.Enum.OS;
import helpers.Enum.Streams;
import imageProcessing.ImageProcessor2D;
import imageProcessing.PointCloudCreation;
import kinect.*;

/**
 * This class exposes all control settings and manages the general functionality
 * of the application during runtime.
 * 
 * @author Moritz Skowronski
 */
public class Control {

	private PApplet p;

	private AbstractKinect kinect;

	private ImageProcessor2D imageProcessor;

	private PersonHandler personHandler;

	private UDPBroadcaster udpBroadcaster;

	private UDPStreamer udpStream;

	private TCPReceiver tcpReceiver;

	private Settings settings;

	private OSCClient oscClient;

	private Thread broadcastThread;
	private Thread tcpThread;

	private PointCloudCreation pointCloudCreator;

	public Control(PApplet p) {

		this.p = p;
		try {
			setupKinect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		imageProcessor = new ImageProcessor2D(this.p, AbstractKinect.DEPTH_WIDTH, AbstractKinect.DEPTH_HEIGHT);
		oscClient = new OSCClient();
		personHandler = new PersonHandler(this.p, oscClient);
		udpBroadcaster = new UDPBroadcaster();
		try {
			udpStream = new UDPStreamer();
		} catch (SocketException e) {
			System.err.println("Error creating UDP Socket");
			e.printStackTrace();
		}
		tcpReceiver = new TCPReceiver();
		settings = new Settings();
		pointCloudCreator = new PointCloudCreation(this.p);
	}

	/*--------------Kinect Control--------------*/

	/*
	 * Updates all Kinect Streams
	 */
	private void updateKinect() {

		kinect.updateDepthImage();
		kinect.updateIRImage();
	}

	/**
	 * Executes all steps for image processing and communication based on
	 * Settings class
	 */
	public void execute() {

		updateKinect();
		// if udpBroadcaster has found a server, use the information gained from
		// it to start all the information streams
		if (!tcpReceiver.lostConnection()) {
			if (udpBroadcaster.isPortSet()) {

				String ip = udpBroadcaster.getServerIP();
				if (!OSCClient.isRunning()) {

					OSCClient.startClient(ip, udpBroadcaster.getOSCPort());
				}
				if (!udpStream.isRunning()) {

					udpStream.startStreamer(ip, udpBroadcaster.getUDPPort());
				}
				if (!tcpReceiver.isListening()) {

					try {
						// Start TCP Thread
						tcpReceiver.startReceiver(ip, udpBroadcaster.getTCPPort());
						tcpThread = new Thread(tcpReceiver);
						tcpThread.setDaemon(true);
						tcpThread.start();
					} catch (UnknownHostException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
				}
			}
		}
		// Restart Routine if TCP Connection failed, restart all connections.
		else {
			tcpReceiver.setListening(false);
			tcpThread.interrupt();
			// Reset all
			udpBroadcaster.reset();
			tcpReceiver.reset();
			OSCClient.reset();
			udpStream.reset();
			broadcastThread = new Thread(udpBroadcaster);
			broadcastThread.setDaemon(true);
			broadcastThread.start();
		}
		// Main logic, set all processing details via Settings class
		// Only execute these when new settings arrive
		if (tcpReceiver.isListening() && tcpReceiver.hasNewSettings()) {
			settings = tcpReceiver.getSettings();
			imageProcessor.setBlobSize(settings.getMinimumContourSize());
			imageProcessor.setDifferenceThreshold(settings.getThreshold());
			personHandler.setTriggerZones(settings.getTriggerZones());

			// Calculate Edges & Vertices for all the Triggerzones
			personHandler.initializeTriggerZones();
		}

		// Execute these on every iteration
		if (settings.isRunCalibration()) {
			
			imageProcessor.calibrate(kinect.getRawDepthData());
		}
		if (settings.isRunMultiKinectCalibration()) {
			// TODO Additional functionality to combine multiple kinects
			// together
		}

		// Switches Stream modes
		if (udpStream.isRunning()) {
			switch (settings.getStreamMode()) {
			case DEPTH:
				udpStream.stream(kinect.getDepthImage(), Streams.DEPTH);
				break;
			case INFRARED:
				udpStream.stream(kinect.getIRImage(), Streams.INFRARED);
				break;
			case MASK:
				udpStream.stream(imageProcessor.getDifferencedImage(), Streams.PERSON);
				break;
			case POINTCLOUD:
				udpStream.stream(pointCloudCreator.rawDepthToPointCloud(kinect.getRawDepthData()), Streams.POINTCLOUD);
				break;
			default:
				break;
			}
		}
		
		// analyzes the image, extracts contour from image
		imageProcessor.process(kinect.getRawDepthData());

		if (settings.isTracking()) {

			// performs the tracking
			personHandler.analyzeContour(imageProcessor.getContours(), kinect.getRawDepthData());
		}

	}

	/**
	 * Starts up the Kinect with different libraries depending on OS
	 * 
	 * @throws Exception
	 */
	private void setupKinect() throws Exception {

		OS os = OSChooser.getLibrary();
		if (os == OS.WINDOWS) {
			kinect = new KinectWindows(p);
		}
		if (os == OS.MAC || os == OS.LINUX) {
			kinect = new KinectMac(p);
		}
		if (os == OS.UNKNOWN) {
			throw new Exception("Unknown OS when instantiating Kinect");
		}
		kinect.startKinect();
	}

	/*----------Communication Control-----------*/

	/**
	 * Sets the Broadcast Name and Port of the UDP Broadcaster. Used by the UI
	 * 
	 * @param name
	 * @param port
	 */
	public void setBroadcast(String name, int port) {

		udpBroadcaster.setClientName(name);
		udpBroadcaster.setBroadcastPort(port);
	}

	/**
	 * Called when Broadcast Button of the UI is pressed. Either starts up the
	 * UDP Broadcaster as Thread or shuts it down
	 * 
	 * @param broadcast
	 */
	public void broadCast(boolean broadcast) {

		if (broadcast) {

			udpBroadcaster.setRunning(true);
			broadcastThread = new Thread(udpBroadcaster);
			broadcastThread.setDaemon(true);
			broadcastThread.start();
			System.out.println("started");
		}
		if (!broadcast) {
			udpBroadcaster.setRunning(false);
			broadcastThread.interrupt();
		}
	}

}
