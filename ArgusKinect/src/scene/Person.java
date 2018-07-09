package scene;

import processing.core.PShape;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;

import helpers.PersonMath;
import kinect.AbstractKinect;

/**
 * Person Class: gives access to all person related data
 * 
 * @author Moritz Skowronski
 * 
 *         TODO add simplified Contours
 *
 */
public class Person {

	// Used to give every new person its own id
	private static int next_id = 0;
	// The id of a person
	private int id;
	// Time of Person entry
	private long starttime;
	// How long the person has been in the scene
	private long timestamp;
	// how many frames the person has been in the view of the camera
	private int age;
	// average of all pixels of a person
	private PVector centroid;
	// absolute distance between two frames
	private float distance;
	// Velocity of a Person
	private PVector velocity;
	// Acceleration of a Person
	private PVector acceleration;
	// center of the bounding box
	private PVector center;
	// Contour of a person, consists of many points
	private ArrayList<PVector> contour;
	// Smallest possible rectangle around Contour
	private PShape boundingBox;
	// how many points of a specific triggerzone the person occupies
	private HashMap<String, Integer> occupiesPointsPerTriggerzone;

	public int color;

	/*------------------Constructor------------------*/

	public Person(PVector centroid, PVector center, ArrayList<PVector> contour, PShape boundingBox) {
		id = next_id++;
		starttime = System.currentTimeMillis();
		age = 0;
		this.centroid = centroid;
		this.center = center;
		this.contour = contour;
		this.boundingBox = boundingBox;
		velocity = new PVector(0, 0, 0);
		acceleration = new PVector(0, 0, 0);
		int r = (int) Math.random() * 256;
		int g = (int) Math.random() * 256;
		int b = (int) Math.random() * 256;
		int a = 255;
		a = a << 24;
		r = r << 16;
		g = g << 8;
		color = a | r | g | b;
	}

	// Constructor used for temporary Persons
	public Person() {
		age = -1;
		centroid = new PVector();
		acceleration = new PVector();
		velocity = new PVector();
		center = new PVector();
		occupiesPointsPerTriggerzone = new HashMap<String, Integer>();
	}

	/*------------------Methods------------------*/

	/**
	 * Updates the Person with new Data
	 * 
	 * @param centroid
	 * @param center
	 * @param contour
	 * @param boundingBox
	 */
	public void update(PVector centroid, PVector center, ArrayList<PVector> contour, PShape boundingBox) {
		age++;
		long lasttime = timestamp;
		timestamp = System.currentTimeMillis() - starttime;
		long dt = timestamp - lasttime;
		PVector dVec = PVector.sub(centroid, this.centroid);
		PVector lastVelocity = velocity.copy();
		velocity = PersonMath.calcVelocity(dVec, dt);
		distance = (float) Math.sqrt(Math.pow(dVec.x, 2) + Math.pow(dVec.y, 2));
		acceleration = PVector.sub(velocity, lastVelocity).div(dt);
		this.centroid = centroid;
		this.center = center;
		this.contour = contour;
		this.boundingBox = boundingBox;
	}

	/**
	 * Resets the id counter, only to be used if there are no persons in the map
	 */
	public static void reset_counter() {
		next_id = 0;
	}

	/*------------------Getter------------------*/

	/**
	 * Returns the centroid
	 * 
	 * @return
	 */
	public PVector getCentroid() {
		return centroid;
	}

	/**
	 * Returns the ID
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the age
	 * 
	 * @return
	 */
	public int getAge() {
		return age;
	}

	/**
	 * Returns the velocity
	 * 
	 * @return
	 */
	public PVector getVelocity() {
		return velocity;
	}

	/**
	 * Returns the Center
	 * 
	 * @return
	 */
	public PVector getCenter() {
		return center;
	}

	/**
	 * Returns the Contour
	 * 
	 * @return
	 */
	public ArrayList<PVector> getContour() {
		return contour;
	}

	/**
	 * Returns the Bounding Box
	 * 
	 * @return
	 */
	public PShape getBoundingBox() {
		return boundingBox;
	}

	/**
	 * Returns the Acceleration
	 * 
	 * @return
	 */
	public PVector getAcceleration() {
		return acceleration;
	}

	/**
	 * Returns the Distance
	 * 
	 * @return
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * Returns a normalized Centroid, which means that the values calculated by
	 * the Kinect are divided by the size of the Kinect Viewport. <br>
	 * <b> Example:</b> centroid.x = 260 => screenCentroid.x = 260/512 = 0.508.
	 * This can be used for easy Transformations onto any screen size.
	 * 
	 * @return normalized Centroid
	 */
	public PVector getScreenCentroid() {

		return new PVector(centroid.x / AbstractKinect.DEPTH_WIDTH, centroid.y / AbstractKinect.DEPTH_HEIGHT,
				centroid.z);
	}

	/**
	 * Returns a normalized Center, which means that the values calculated by
	 * the Kinect are divided by the size of the Kinect Viewport. <br>
	 * <b> Example:</b> center.x = 260 => screenCenter.x = 260/512 = 0.508. This
	 * can be used for easy Transformations onto any screen size.
	 * 
	 * @return normalized Center
	 */
	public PVector getScreenCenter() {

		return new PVector(center.x / AbstractKinect.DEPTH_WIDTH, center.y / AbstractKinect.DEPTH_HEIGHT, center.z);
	}

	/**
	 * Returns a normalized Velocity Vector, which means that the values
	 * calculated by the Kinect are divided by the size of the Kinect Viewport.
	 * 
	 * @return normalized Velocity
	 */
	public PVector getScreenVelocity() {

		return new PVector(velocity.x / AbstractKinect.DEPTH_WIDTH, velocity.y / AbstractKinect.DEPTH_HEIGHT,
				velocity.z / 8000);
	}

	/**
	 * Returns a normalized Acceleration Vector, which means that the values
	 * calculated by the Kinect are divided by the size of the Kinect Viewport.
	 * 
	 * @return normalized Acceleration
	 */
	public PVector getScreenAcceleration() {

		return new PVector(acceleration.x / AbstractKinect.DEPTH_WIDTH, acceleration.y / AbstractKinect.DEPTH_HEIGHT,
				acceleration.z / 8000);
	}

	/**
	 * Returns a normalized Contour, which means that the values calculated by
	 * the Kinect are divided by the size of the Kinect Viewport. <br>
	 * <b> Example:</b> contour.x = 260 => screenContour.x = 260/512 = 0.508.
	 * This can be used for easy Transformations onto any screen size.
	 * 
	 * @return normalized Contour
	 */
	public ArrayList<PVector> getScreenContour() {

		ArrayList<PVector> screenContour = new ArrayList<PVector>();

		for (PVector vector : contour) {

			screenContour.add(new PVector(vector.x / AbstractKinect.DEPTH_WIDTH, vector.y / AbstractKinect.DEPTH_HEIGHT,
					vector.z));
		}

		return screenContour;
	}

	/**
	 * Returns which Triggerzone the Person occupies.
	 * 
	 * @return
	 */
	public HashMap<String, Integer> getOccupiesPointsPerTriggerzone() {
		return occupiesPointsPerTriggerzone;
	}

	/**
	 * Adds a point to the triggerzone with the given id
	 * 
	 * @param id
	 */
	public void addToPointsPerPerson(String id, int amount) {

		if (occupiesPointsPerTriggerzone.containsKey(id)) {

			int points = occupiesPointsPerTriggerzone.get(id);
			occupiesPointsPerTriggerzone.put(id, points + amount);
		} else {
			occupiesPointsPerTriggerzone.put(id, amount);
		}
	}

}
