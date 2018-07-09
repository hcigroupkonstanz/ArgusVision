package control;

import java.io.Serializable;

import helpers.Enum.OSCPersonMode;
import helpers.Enum.OSCTriggerZoneMode;
import helpers.Enum.Streams;
import scene.AbstractTriggerZone;

/**
 * Settings for the Client. Holds all relevant info that the Server can send to the Client
 * @author Moritz Skowronski
 *
 */
public class Settings implements Serializable {

	private static final long serialVersionUID = 7327301546422697639L;

	private boolean runCalibration;
	
	private boolean isTracking;

	private int minimumContourSize;
	private int threshold;

	private AbstractTriggerZone[] triggerZones;

	private Streams streamMode;

	private OSCPersonMode personMode;
	private OSCTriggerZoneMode triggerMode;

	private boolean runMultiKinectCalibration;

	/**
	 * Start up with default Settings
	 */
	public Settings() {
		isTracking = false;
		runCalibration = false;
		minimumContourSize = 5000;
		threshold = 50;
		triggerZones = new AbstractTriggerZone[0];
		streamMode = Streams.NOSEND;
		personMode = OSCPersonMode.NOSEND;
		triggerMode = OSCTriggerZoneMode.NOSEND;
		runMultiKinectCalibration = false;
	}

	// *------------------------Getter & Setter------------------*/

	public boolean isRunCalibration() {
		return runCalibration;
	}

	public void setRunCalibration(boolean runCalibration) {
		this.runCalibration = runCalibration;
	}

	public int getMinimumContourSize() {
		return minimumContourSize;
	}

	public void setMinimumContourSize(int minimumContourSize) {
		this.minimumContourSize = minimumContourSize;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public AbstractTriggerZone[] getTriggerZones() {
		return triggerZones;
	}

	public void setTriggerZones(AbstractTriggerZone[] triggerZones) {
		this.triggerZones = triggerZones;
	}

	public Streams getStreamMode() {
		return streamMode;
	}

	public void setStreamMode(Streams streamMode) {
		this.streamMode = streamMode;
	}

	public OSCPersonMode getPersonMode() {
		return personMode;
	}

	public void setPersonMode(OSCPersonMode personMode) {
		this.personMode = personMode;
	}

	public OSCTriggerZoneMode getTriggerMode() {
		return triggerMode;
	}

	public void setTriggerMode(OSCTriggerZoneMode triggerMode) {
		this.triggerMode = triggerMode;
	}

	public boolean isRunMultiKinectCalibration() {
		return runMultiKinectCalibration;
	}

	public void setRunMultiKinectCalibration(boolean runMultiKinectCalibration) {
		this.runMultiKinectCalibration = runMultiKinectCalibration;
	}

	public boolean isTracking() {
		return isTracking;
	}

	public void setTracking(boolean isTracking) {
		this.isTracking = isTracking;
	}

}
