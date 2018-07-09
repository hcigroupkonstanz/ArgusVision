package visualization;

import java.awt.image.BufferedImage;

/**
 * Interface for parsing the UDP Images to the Display
 * 
 * @author Moritz Skowronski
 *
 */
public interface ImageUpdateListener {

	/**
	 * Is called when the UDP Server has received a point cloud
	 * 
	 * @param pointCloudData
	 *            the encoded point cloud data
	 */
	public void updatePointCloud(BufferedImage pointCloudData);

	/**
	 * Is called when the UDP Server has received an image
	 * 
	 * @param image
	 *            the image
	 */
	public void updateImage(BufferedImage image);

}
