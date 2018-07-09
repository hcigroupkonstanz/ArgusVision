package helpers;

import processing.core.PVector;

/**
 * Provides Transformation functions for the Kinect Point Cloud
 * 
 * @author Moritz Skowronski
 *
 */
public class KinectTransformations {

	private static final float CX = 254.878f;
	private static final float CY = 205.395f;
	private static final float FX = 365.456f;
	private static final float FY = 365.456f;
	private static final float K1 = 0.0905474f;
	private static final float K2 = -0.26819f;
	private static final float K3 = 0.0950862f;
	private static final float P1 = 0.0f;
	private static final float P2 = 0.0f;

	/**
	 * Transforms the x and y values of the kinect into real world values using
	 * values derived from testing. TODO implement a camera calibration for
	 * better results
	 * 
	 * @param x
	 *            the pixel x position
	 * @param y
	 *            the pixel y position
	 * @param depthValue
	 *            the depthvalue in milimeters
	 * @return
	 */
	public static PVector depthToPointCloudPos(int x, int y, float depthValue) {

		PVector point = new PVector();
		point.z = depthValue;
		point.x = (x - CX) * point.z / FX;
		point.y = (y - CY) * point.z / FY;
		return point;
	}

	/**
	 * Transforms Real World Values into Kinect Pixels. TODO implement a camera
	 * calibration for better results
	 * 
	 * @param x
	 *            the real world x position
	 * @param y
	 *            the real world y position
	 * @param depthValue
	 *            the depthvalue in milimeters
	 * @return
	 */
	public static PVector PointCloudToDepthPos(float x, float y, float depthValue) {

		PVector point = new PVector();
		point.z = depthValue;
		point.x = (int) (x * FX / point.z) + CX;
		point.y = (int) (y * FY / point.z) + CY;
		return point;
	}

}
