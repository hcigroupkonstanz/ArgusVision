package scene;

import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PVector;

/**
 * Interface for handling update Events from OSC
 * 
 * @author Moritz Skowronski
 *
 */
public interface OSCUpdateListener {

	/**
	 * Is called when a new Person has entered the Viewport of a Kinect
	 * 
	 * @param name
	 *            name of the Kinect
	 * @param person
	 *            new Person
	 */
	public void personEntered(String name, Person person);

	/**
	 * Is called when a Person has left the Viewport of a Kinect
	 * 
	 * @param name
	 *            name of the Kinect
	 * @param id
	 *            id of the Person
	 */
	public void personLeft(String name, int id);

	/**
	 * Is called when a Person in the Viewport of the Kinect has moved
	 * 
	 * @param name
	 *            name of the Kinect
	 * @param person
	 *            Updated Person
	 */
	public void personMoved(String name, Person person);

	/**
	 * Is called when a Triggerzone is updated
	 * 
	 * @param kinectName
	 * @param id
	 * @param depthPointsInZone
	 * @param pointsPerPerson
	 * @param updateTime
	 */
	public void updateTriggerZone(String kinectName, String id, int depthPointsInZone,
			HashMap<Integer, Integer> pointsPerPerson, Long updateTime);

	public void updateKinectCalibration(String first, String second, ArrayList<PVector> firstPoints,
			ArrayList<PVector> secondPoints);

}
