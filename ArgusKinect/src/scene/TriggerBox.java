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

	private Vec[] edges;

	private Vec[] normals;

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
		this.x = x;
		this.y = y;
		this.z = z;
		sizeX = width;
		sizeY = height;
		sizeZ = depth;

		pointsPerPerson = new HashMap<Integer, Integer>();
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
	public void addToPointsPerPerson(Integer id, Integer amount) {

		pointsPerPerson.put(id, amount);
	}

	@Override
	public void addToPoints(int points) {

		depthPointsInBox+=points;
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
	public Mat getRotationMatrix() {

		Quat buildQuat = new Quat(0, 0, 0);
		return buildQuat.matrix();
	}

	@Override
	public void clearPoints() {

		depthPointsInBox = 0;
		pointsPerPerson.clear();
	}

	public void calculateNormalizedNormals() {

		normals = new Vec[6];

		for (int i = 0; i < normals.length; i++) {

			normals[i] = new Vec();
		}
		
		Vec.cross(Vec.subtract(edges[5], edges[3]), Vec.subtract(edges[2], edges[3]), normals[0]);

		Vec.cross(Vec.subtract(edges[1], edges[2]), Vec.subtract(edges[0], edges[2]), normals[1]);

		Vec.cross(Vec.subtract(edges[6], edges[3]), Vec.subtract(edges[5], edges[3]), normals[2]);

		Vec.cross(Vec.subtract(edges[1], edges[6]), Vec.subtract(edges[4], edges[6]), normals[3]);

		Vec.cross(Vec.subtract(edges[7], edges[4]), Vec.subtract(edges[5], edges[4]), normals[4]);

		Vec.cross(Vec.subtract(edges[3], edges[6]), Vec.subtract(edges[1], edges[6]), normals[5]);

		for (int i = 0; i < normals.length; i++) {

			normals[i].normalize();
		}
	}

	public void calculateEdges() {
		edges = new Vec[8];

		for (int i = 0; i < edges.length; i++) {

			edges[i] = new Vec();
		}
		// for this we need the center
		Vec center = new Vec(x, y, z);
		// as well as the size and rotations
		float[] size = getSize();
		// Get the rotation matrix
		Mat rotation = getRotationMatrix();
		// Construct the vertices
		// Since its a rectangular shape, this is trivial
		edges[0] = new Vec(size[0] / 2, size[1] / 2, size[2] / 2);
		edges[1] = new Vec(size[0] / 2, size[1] / 2, -size[2] / 2);
		edges[2] = new Vec(size[0] / 2, -size[1] / 2, size[2] / 2);
		edges[3] = new Vec(-size[0] / 2, size[1] / 2, size[2] / 2);
		edges[4] = new Vec(-size[0] / 2, -size[1] / 2, -size[2] / 2);
		edges[5] = new Vec(-size[0] / 2, -size[1] / 2, size[2] / 2);
		edges[6] = new Vec(-size[0] / 2, size[1] / 2, -size[2] / 2);
		edges[7] = new Vec(size[0] / 2, -size[1] / 2, -size[2] / 2);

		// translate it to the correct position
		for (int i = 0; i < edges.length; i++) {
			edges[i] = rotation.multiply(edges[i]);
			edges[i].add(center);
		}
	}

	public Vec[] getEdges() {

		return edges;
	}

	public Vec[] getNormalizedNormals() {

		return normals;
	}

}
