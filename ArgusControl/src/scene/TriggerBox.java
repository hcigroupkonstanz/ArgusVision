package scene;

import java.io.Serializable;
import java.util.HashMap;

import remixlab.dandelion.geom.Mat;
import remixlab.dandelion.geom.Quat;
import remixlab.dandelion.geom.Vec;

/**
 * A box shaped Triggerzone.
 * 
 * @author Moritz Skowronski
 *
 */
public class TriggerBox extends AbstractTriggerZone implements Serializable {

	private static final long serialVersionUID = -6214197918433289422L;

	private static int triggerBoxCounter = 0;

	private String id;

	// internal position for drawing
	private float x;
	private float y;
	private float z;

	// Box Sides
	private float volume;

	private float sizeX;
	private float sizeY;
	private float sizeZ;

	private float rotateX;
	private float rotateY;
	private float rotateZ;

	private long lastUpdate;
	
	private Vec[] edges;

	private Vec[] normals;

	// color
	private int color;

	// amount of PersonPoints in the Box
	private int depthPointsInBox;
	
	// <PersonID, Points of that Person>
	private HashMap<Integer, Integer> pointsPerPerson;

	/**
	 * Constructs a new TriggerBox
	 * 
	 * @param id
	 *            ID
	 * @param x
	 *            x Position
	 * @param y
	 *            y Position
	 * @param z
	 *            z Position
	 * @param width
	 *            width of Box
	 * @param height
	 *            height of Box
	 * @param depth
	 *            depth of Box
	 * @param color
	 *            color of Box
	 */
	public TriggerBox(float x, float y, float z, float width, float height, float depth) {
		this.id = "Triggerbox " + triggerBoxCounter++;
		this.x = x;
		this.y = y;
		this.z = z;
		this.rotateX = 0;
		this.rotateY = 0;
		this.rotateZ = 0;
		sizeX = width;
		sizeY = height;
		sizeZ = depth;

		pointsPerPerson = new HashMap<Integer, Integer>();

		// Make random color for better visualization
		int r = (int) (Math.random() * 256);
		int g = (int) (Math.random() * 256);
		int b = (int) (Math.random() * 256);
		int a = 255;
		a = a << 24;
		r = r << 16;
		g = g << 8;
		color = (a | r | g | b);

		lastUpdate = System.currentTimeMillis();
	}

	/*-------------Setters--------------*/

	@Override
	public void setID(String id) {
		this.id = id;
	}

	@Override
	public void setX(float x) {
		this.x = x;
	}

	@Override
	public void setY(float y) {
		this.y = y;
	}

	@Override
	public void setZ(float z) {
		this.z = z;
	}

	@Override
	public void setRotateX(float x) {

		this.rotateX = x;
	}

	@Override
	public void setRotateY(float y) {

		this.rotateY = y;
	}

	@Override
	public void setRotateZ(float z) {

		this.rotateZ = z;
	}

	@Override
	public void setSize(float width, float height, float depth) {
		this.sizeX = width;
		this.sizeY = height;
		this.sizeZ = depth;
		this.volume = sizeX * sizeY * sizeZ;
	}

	@Override
	public void setColor(int col) {
		this.color = col;
	}

	@Override
	public void setPointsPerPerson(HashMap<Integer, Integer> depthPointsPerPerson) {
		
		this.pointsPerPerson = depthPointsPerPerson;
	}

	@Override
	public void setPointsInsideBox(int points) {
		this.depthPointsInBox = points;
	}

	@Override
	public void setLastUpdate(Long timestamp) {

		lastUpdate = timestamp;
	}

	public static void setStartCounter(int start){
		triggerBoxCounter = start;
	}

	/*-------------Getters--------------*/

	@Override
	public String getID() {
		return id;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public float getZ() {
		return z;
	}

	@Override
	public float[] getSize() {
		return new float[] { sizeX, sizeY, sizeZ };
	}

	@Override
	public float getVolume() {
		return volume;
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public int getPointsInsideBox() {
		return depthPointsInBox;
	}

	@Override
	public HashMap<Integer, Integer> getPointsPerPerson() {
		return pointsPerPerson;
	}

	@Override
	public float getRotateX() {

		return rotateX;
	}

	@Override
	public float getRotateY() {

		return rotateY;
	}

	@Override
	public float getRotateZ() {

		return rotateZ;
	}

	@Override
	public long getLastUpdate() {

		return lastUpdate;
	}
	
	/**
	 * Returns the rotation matrix for the triggerbox
	 * @return
	 */
	public Mat getRotationMatrix() {

		Quat buildQuat = new Quat(0, 0, 0);
		return buildQuat.matrix();
	}

}
