package communication;

/**
 * Interface for handling new Kinects via UDP Broadcasting. To be implemented by
 * every class that needs to know about a new Kinect.
 * 
 * @author Moritz Skowronski
 *
 */
public interface KinectConnectListener {
	
	/**
	 * Is called when a new Kinect is recognized
	 * 
	 * @param name
	 * @param ip
	 */
	public void kinectRecognized(String name, String ip);

}
