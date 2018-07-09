package scene;

import java.util.HashMap;

import remixlab.dandelion.geom.Mat;

/**
 * Abstract Implementation for Triggerzones
 * 
 * @author Moritz Skowronski
 *
 */
public abstract class AbstractTriggerZone {

	/**
	 * Set X Position of TriggerZone
	 * 
	 * @param x
	 *            Position
	 */
	public abstract void setX(float x);

	/**
	 * Set Y Position of TriggerZone
	 * 
	 * @param x
	 *            Position
	 */
	public abstract void setY(float y);

	/**
	 * Set Z Position of TriggerZone
	 * 
	 * @param x
	 *            Position
	 */
	public abstract void setZ(float z);

	/**
	 * Set X Rotation of Triggerzone
	 * 
	 * @param x
	 */
	public abstract void setRotateX(float x);

	/**
	 * Set Y Rotation of Triggerzone
	 * 
	 * @param y
	 */
	public abstract void setRotateY(float y);

	/**
	 * Set Z Rotation of Triggerzone
	 * 
	 * @param z
	 */
	public abstract void setRotateZ(float z);

	/**
	 * Sets either with, depth & height or radius, cropRadius & height
	 * 
	 * @param x
	 *            width/radius
	 * @param y
	 *            height
	 * @param z
	 *            depth/cropradius
	 */
	public abstract void setSize(float x, float y, float z);


	/**
	 * Get ID of TriggerZone
	 * 
	 * @return ID of TriggerZone
	 */
	public abstract String getID();

	/**
	 * Get x Position of TriggerZone
	 * 
	 * @return x Position of TriggerZone
	 */
	public abstract float getX();

	/**
	 * Get y Position of TriggerZone
	 * 
	 * @return y Position of TriggerZone
	 */
	public abstract float getY();

	/**
	 * Get z Position of TriggerZone
	 * 
	 * @return z Position of TriggerZone
	 */
	public abstract float getZ();

	/**
	 * Set X Rotation of Triggerzone
	 * 
	 * @param x
	 */
	public abstract float getRotateX();

	/**
	 * Set Y Rotation of Triggerzone
	 * 
	 * @param y
	 */
	public abstract float getRotateY();

	/**
	 * Set Z Rotation of Triggerzone
	 * 
	 * @param z
	 */
	public abstract float getRotateZ();

	/**
	 * Get sizes of TriggerZone, is either in the format {width,height,depth} or
	 * {radius,height,cropradius}
	 * 
	 * @return sizes of the TriggerZone
	 */
	public abstract float[] getSize();

	/**
	 * Get Volume of the TriggerZone
	 * 
	 * @return Volume of the TriggerZone
	 */
	public abstract float getVolume();


	/**
	 * Get the number of all Points which are not there from the beginning
	 * 
	 * @return number of new Points
	 */
	public abstract int getPointsInsideBox();

	/**
	 * Get the ids of all persons that are inside the TriggerZone as well as the
	 * number of points they occupy
	 * 
	 * @return Map of Person IDs to Number of Points
	 */
	public abstract HashMap<Integer, Integer> getPointsPerPerson();

	/**
	 * Sets the ID of a Triggerzone to a String value
	 * 
	 * @param id
	 */
	public abstract void setID(String id);

	/**
	 * Adds to the Hashmap that show how many people are inside the Zone and how
	 * many points they are occupying
	 * 
	 * @param id
	 *            the id of the Person
	 * @param amount
	 *            the amount of points the person occupies
	 */
	public abstract void addToPointsPerPerson(Integer id, Integer amount);

	/**
	 * Adds points to occupied space
	 * 
	 * @param points
	 */
	public abstract void addToPoints(int points);

	/**
	 * Clears the pointsinsidebox and pointsperperson values
	 */
	public abstract void clearPoints();

	/**
	 * Returns the Rotation Matrix
	 * 
	 * @return
	 */
	public abstract Mat getRotationMatrix();
	
}
