package kinect;

import processing.core.PImage;

/**
 * Abstract Implementation of the Kinect functionality. This enables usage of
 * Kinect regardless of Operating System. TODO In further work, add automatic or
 * manual change between own image processing and normal Body Tracking.
 * 
 * @author Moritz Skowronski
 *
 */
public abstract class AbstractKinect {
	
	public static final int DEPTH_WIDTH = 512;
	public static final int DEPTH_HEIGHT = 424;
	public static final int COLOR_WIDTH = 1920;
	public static final int COLOR_HEIGHT = 1080;

	/**
	 * Starts up the Kinect, enables all relevant Images
	 */
	abstract public void startKinect();

	/**
	 * 
	 * @return PImage of the Color Image
	 */
	abstract public PImage getColorImage();

	/**
	 * 
	 * @return PImage of the Depth Image
	 */
	abstract public PImage getDepthImage();

	/**
	 * 
	 * @return PImage of the IR Image
	 */
	abstract public PImage getIRImage();

	/**
	 * 
	 * @return PImage of tracked Bodies
	 */
	abstract public PImage getBodyTrackImage();
	
	/**
	 * Gets new Color Image from Stream
	 */
	abstract public void updateColorImage();
	
	/**
	 * Gets new Depth Image from Stream
	 */
	abstract public void updateDepthImage();
	
	/**
	 * Gets new IR Image from Stream
	 */
	abstract public void updateIRImage();
	
	/**
	 * Gets new Body Track Image from Image Processor
	 */
	abstract public void updateBodyTrackImage();

	/**
	 * Raw Kinect Depth data. 
	 * @return int array of raw depth per pixel (512 * 424)
	 */
	abstract public int[] getRawDepthData();
	
	/**
	 * Shut down Kinect
	 */
	abstract public void closeKinect();

}
