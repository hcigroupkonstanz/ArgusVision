package control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.thoughtworks.xstream.XStream;

import communication.CommunicationHandler;
import helpers.Enum.Streams;
import scene.AbstractTriggerZone;
import scene.Person;
import scene.SceneHandler;
import scene.TriggerBox;
import visualization.InteractiveTriggerbox;

/**
 * Main class for controlling the model of this application. Handles the UI
 * Commands and parses them to the respective objects
 * 
 * @author Moritz Skowronski
 *
 */
public class Control {

	private HashMap<String, Settings> settings;

	private XStream settingsParser;

	private CommunicationHandler communicationHandler;

	private SceneHandler sceneHandler;

	private remixlab.proscene.Scene displayableScene;
	
	private boolean triggerzonesChanged;
	
	/**
	 * Temporary save for triggerzones, that have been loaded from Settings
	 * This is needed because we can only make OpenGL calls from the main thread
	 */
	private ConcurrentHashMap<String, AbstractTriggerZone[]> temporaryZones;

	public Control(CommunicationHandler communicationHandler, SceneHandler sceneHandler) {

		settings = new HashMap<String, Settings>();

		this.communicationHandler = communicationHandler;

		this.sceneHandler = sceneHandler;

		settingsParser = new XStream();
		
		temporaryZones = new ConcurrentHashMap<String, AbstractTriggerZone[]>();

		settingsParser.alias("settings", Settings.class);
	}

	public void update(String kinectName) {

		Settings settingsToSend = settings.get(kinectName);

		communicationHandler.sendSettings(kinectName, settings.get(kinectName));
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("./data/" + kinectName + ".xml"), "utf-8"))) {
			settingsParser.toXML(settingsToSend, writer);
		} catch (Exception e) {
			System.err.println("INFO: Couldn't save settings for Kinect " + kinectName);
			e.printStackTrace();
		}

	}

	/**
	 * Starts the TCP, UDP & OSC Server on the specific port
	 * 
	 * @param port
	 */
	public void serverStart(int port) {
		communicationHandler.setPort(port);
		communicationHandler.startup();
	}

	/**
	 * Stops the TCP, UDP & OSC Server
	 */
	public void serverStop() {

		settings.clear();
		communicationHandler.shutdown();
	}

	/**
	 * Sets whether the Depth Calibration should run
	 * 
	 * @param activeKinect
	 *            the ID of the Kinect
	 * @param isActive
	 *            true, if run Calibration
	 */
	public void setDepthCalibration(String activeKinect, boolean isActive) {
		settings.get(activeKinect).setRunCalibration(isActive);
		update(activeKinect);
	}

	/**
	 * Sets whether the Multi Camera Calibration should run
	 * 
	 * @param activeKinect
	 *            the ID of the Kinect
	 * @param isActive
	 *            true, if run Calibration
	 */
	public void setMultiCameraCalibration(String activeKinect, boolean isActive) {
		settings.get(activeKinect).setRunMultiKinectCalibration(isActive);
		update(activeKinect);
	}

	/**
	 * Sets the Depth Threshold of the selected Kinect
	 * 
	 * @param activeKinect
	 * @param i
	 */
	public void setThreshold(String activeKinect, int i) {
		settings.get(activeKinect).setThreshold(i);
		update(activeKinect);
	}

	/**
	 * Sets the Minimum Contoursize of the selected Kinect
	 * 
	 * @param activeKinect
	 * @param i
	 */
	public void setContoursize(String activeKinect, int i) {
		settings.get(activeKinect).setMinimumContourSize(i);
		update(activeKinect);
	}

	/**
	 * Sets whether Tracking should run
	 * 
	 * @param activeKinect
	 * @param isActive
	 */
	public void setTracking(String activeKinect, boolean isActive) {
		settings.get(activeKinect).setTracking(isActive);
		update(activeKinect);
	}

	/**
	 * Returns the Settings for the given Kinect
	 * 
	 * @param activeKinect
	 * @return
	 */
	public Settings getSettings(String activeKinect) {
		return settings.get(activeKinect);
	}

	public void kinectRecognized(String name, String ip) {

		communicationHandler.kinectRecognized(name, ip);
		sceneHandler.createScene(name);
		settings.put(name, new Settings());
	}

	public void sendSettingsOnTCPConnect(String name) {

		File loadedFile = new File("./data/" + name + ".xml");

		if (!(loadedFile.exists() && !loadedFile.isDirectory())) {

			update(name);

		} else {

			Settings temporarySettings = (Settings) settingsParser.fromXML(loadedFile);

			// Update all
			setContoursize(name, temporarySettings.getMinimumContourSize());
			setDepthCalibration(name, temporarySettings.isRunCalibration());
			setMultiCameraCalibration(name, temporarySettings.isRunMultiKinectCalibration());
			setStreaming(name, temporarySettings.getStreamMode());
			setThreshold(name, temporarySettings.getThreshold());
			setTracking(name, temporarySettings.isTracking());

			temporaryZones.put(name, temporarySettings.getTriggerZones());
			triggerzonesChanged = true;
			
		}
	}

	/**
	 * Sets the Streaming mode for the given Kinect
	 * 
	 * @param activeKinect
	 * @param stringValue
	 */
	public void setStreaming(String activeKinect, Streams streamMode) {
		settings.get(activeKinect).setStreamMode(streamMode);
		update(activeKinect);
	}

	/**
	 * Returns all persons in the specified scene
	 * 
	 * @param name
	 *            name of the scene
	 * @return
	 */
	public Person[] getPersonsForScene(String name) {

		return sceneHandler.getKinectScene(name).getPersons();
	}

	/**
	 * Returns a list of Triggerzones for the specified Kinect.
	 * 
	 * @param name
	 *            the id of the Kinect
	 * @return ArrayList of Triggerzones
	 */
	public AbstractTriggerZone[] getTriggerzonesForKinect(String name) {

		return sceneHandler.getKinectScene(name).getTriggerZones();
	}

	public InteractiveTriggerbox[] getDisplayableBoxForKinect(String name) {

		return sceneHandler.getKinectScene(name).getInteractiveZones();
	}

	/**
	 * Returns the Triggerzone of the given Kinect on the position in the
	 * arraylist.
	 * 
	 * @param kinect
	 *            the kinect
	 * @param position
	 *            the position in the Array
	 * @return
	 */
	public AbstractTriggerZone getTriggerZoneForKinect(String kinect, String name) {

		return sceneHandler.getKinectScene(kinect).getTriggerZone(name);
	}

	/**
	 * Deletes a Triggerzone from the ArrayList
	 * 
	 * @param activeKinect
	 *            the Kinect the Triggerzone belongs to
	 * @param activeTriggerzone
	 *            the Triggerzone name
	 */
	public void deleteTriggerzone(String activeKinect, String activeTriggerzone) {

		sceneHandler.getKinectScene(activeKinect).deleteTriggerzone(activeTriggerzone);
		
		updateTriggerzones(activeKinect);
	}

	/**
	 * Adds a triggerZone to the given Kinect
	 * 
	 * @param kinect
	 */
	public void addTriggerZone(AbstractTriggerZone zone, String kinect) {

		// TODO Right now, only supports boxes
		sceneHandler.getKinectScene(kinect).addNewTriggerZone(zone, displayableScene);
		
		updateTriggerzones(kinect);
	}

	/**
	 * Updates the Triggerzones of a specific kinect and sends it to the client
	 * 
	 * @param kinect
	 */
	public void updateTriggerzones(String kinect) {
		if (kinect == null)
			return;
		settings.get(kinect).setTriggerZones(sceneHandler.getKinectScene(kinect).getTriggerZones());
		update(kinect);
	}

	/**
	 * Used to set the scene, so we can create interactive triggerzones from
	 * here
	 * 
	 * @param scene
	 */
	public void setScene(remixlab.proscene.Scene scene) {

		this.displayableScene = scene;
	}
	
	/**
	 * Returns if triggerzones have changed and if so, returns true and switches back to false
	 * @return
	 */
	public boolean triggerzonesChangedBang(){
		
		if(triggerzonesChanged){
			triggerzonesChanged = false;
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the Temporary Zones with their respective Kinects
	 * @return
	 */
	public ConcurrentHashMap<String, AbstractTriggerZone[]> getTemporaryZones(){
		
		return temporaryZones;
	}

}
