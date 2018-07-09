package helpers;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;
import remixlab.dandelion.geom.Vec;
import scene.TriggerBox;

/**
 * Holds some useful mathematical operations
 * 
 * @author Moritz Skowronski
 *
 */
public class PersonMath {

	/**
	 * Test whether a point is inside a Triggerbox
	 * 
	 * @param pointToCheck
	 *            the point to check if it is inside the box
	 * @param box
	 *            the box for which to check if the point is inside
	 * @return
	 */
	public static boolean isInsideTriggerBox(PVector pointToCheck, TriggerBox box) {

		// Get the normals for the planes of the vertices and check
		// the distance of the
		// point to every plane. It has be to laying at the back of each plane
		// to be inside the box
		Vec[] normals = box.getNormalizedNormals();
		Vec[] edges = box.getEdges();
		Vec pointTransformed = new Vec(pointToCheck.x, pointToCheck.y, pointToCheck.z);

		if (distanceFromPointToPlane(edges[2], normals[0], pointTransformed) > 0)
			return false;

		if (distanceFromPointToPlane(edges[1], normals[1], pointTransformed) > 0)
			return false;

		if (distanceFromPointToPlane(edges[3], normals[2], pointTransformed) > 0)
			return false;

		if (distanceFromPointToPlane(edges[6], normals[3], pointTransformed) > 0)
			return false;

		if (distanceFromPointToPlane(edges[7], normals[4], pointTransformed) > 0)
			return false;

		if (distanceFromPointToPlane(edges[1], normals[5], pointTransformed) > 0)
			return false;

		return true;
	}

	/**
	 * Calculates the euclidean distance from a Plane to a Point
	 * 
	 * @param origin
	 *            a point on the plane
	 * @param normal
	 *            normal of the plane (normalized)
	 * @param point
	 *            a point in 3d space
	 * @return
	 */
	private static float distanceFromPointToPlane(Vec origin, Vec normal, Vec point) {

		float dist = Vec.dot(normal, Vec.subtract(point, origin));
		return dist;
	}

	/**
	 * Calculates a velocity vector based on length of distance and time it took
	 * to make the distance
	 * 
	 * @param dVec
	 * @param dt
	 * @return
	 */
	public static PVector calcVelocity(PVector dVec, long dt) {
		PVector velocity = new PVector();
		velocity.x = dVec.x / dt;
		velocity.y = dVec.y / dt;
		velocity.z = dVec.z / dt;
		return velocity;
	}

	/**
	 * Calculates the respective average over all x,y and z values of a PVector.
	 * 
	 * @deprecated use calculateAll instead
	 * @param contour
	 *            ArrayList<PVector>
	 * @return Centroid of the given Contour
	 */
	public static PVector centroid(ArrayList<PVector> contour) {

		float x = 0;
		float y = 0;
		float z = 0;
		for (PVector point : contour) {
			x += point.x;
			y += point.y;
			z += point.z;
		}
		x /= contour.size();
		y /= contour.size();
		z /= contour.size();
		return new PVector(x, y, z);
	}

	/**
	 * Checks whether the abs value lies in range
	 * 
	 * @param value
	 *            value to be checked
	 * @param low
	 *            lower border
	 * @param high
	 *            upper border
	 * @return true if in range, else false
	 */
	public static boolean valueInRange(float value, float low, float high) {
		return value >= low && value <= high;
	}

	/**
	 * Calculates Bounding box of a Contour
	 * 
	 * @param p
	 *            the Applet, needed for createShape
	 * @param contour
	 *            ArrayList<PVector>
	 * @return jawa.awt.Rectangle of a bounding box
	 */
	public static PShape getBoundingBox(PApplet p, ArrayList<PVector> contour) {
		float xMax = 0;
		float yMax = 0;
		float xMin = Float.MAX_VALUE;
		float yMin = Float.MAX_VALUE;

		if (contour.size() % 2 == 0) {
			for (int i = 0; i < contour.size(); i += 2) {
				PVector point1 = contour.get(i);
				PVector point2 = contour.get(i + 1);
				xMin = tripleMin(xMin, point1.x, point2.x);
				yMin = tripleMin(yMin, point1.y, point2.y);
				xMax = tripleMax(xMax, point1.x, point2.x);
				yMax = tripleMax(yMax, point1.y, point2.y);
			}
		} else {
			for (int i = 0; i < contour.size(); i += 2) {
				if (i == contour.size() - 1) {
					PVector point1 = contour.get(i);
					if (point1.x < xMin)
						xMin = point1.x;
					else {
						if (point1.x > xMax)
							xMax = point1.x;
					}
					if (point1.y < yMin)
						yMin = point1.y;
					else {
						if (point1.y > yMax)
							yMax = point1.y;
					}
				} else {
					PVector point1 = contour.get(i);
					PVector point2 = contour.get(i + 1);
					xMin = tripleMin(xMin, point1.x, point2.x);
					yMin = tripleMin(yMin, point1.y, point2.y);
					xMax = tripleMax(xMax, point1.x, point2.x);
					yMax = tripleMax(yMax, point1.y, point2.y);
				}
			}

		}

		return p.createShape(PApplet.RECT, (int) xMin, (int) yMin, (int) xMax, (int) yMax);
	}

	/**
	 * Calculates the minimum of three values
	 * 
	 * @param currentMin
	 *            float current Minimum
	 * @param value1
	 *            float
	 * @param value2
	 *            float
	 * @return Minimum
	 */
	public static float tripleMin(float currentMin, float value1, float value2) {
		if (value1 < value2) {
			if (value1 < currentMin) {
				return value1;
			}
		} else {
			if (value2 < currentMin) {
				return value2;
			}
		}
		return currentMin;
	}

	/**
	 * Calculates the maximum of three values
	 * 
	 * @param currentMin
	 *            float current Maximum
	 * @param value1
	 *            float
	 * @param value2
	 *            float
	 * @return Maximum
	 */
	public static float tripleMax(float currentMax, float value1, float value2) {
		if (value1 > value2) {
			if (value1 > currentMax) {
				return value1;
			}
		} else {
			if (value2 > currentMax) {
				return value2;
			}
		}
		return currentMax;
	}

	/**
	 * Calculates realworld values from pixel x,y values and real world depth
	 * values (in millimeters)
	 * 
	 * @param x
	 * @param y
	 * @param depthValue
	 * @return
	 */
	public static PVector depthToPointCloudPos(int x, int y, float depthValue) {
		PVector point = new PVector();
		point.z = depthValue;
		point.x = (x - CameraParams.cx) * point.z / CameraParams.fx;
		point.y = (y - CameraParams.cy) * point.z / CameraParams.fy;
		return point;
	}

}
