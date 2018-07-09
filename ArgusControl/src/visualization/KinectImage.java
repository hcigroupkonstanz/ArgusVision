package visualization;

import java.awt.image.BufferedImage;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * This class provides the handling of encoded or compressed Data from UDP
 * Stream and converts them into displayable / handable values. The last updated
 * values are permanently stored in a variable to guarantee a smooth change
 * between pictures, even if the streaming of a new picture takes longer than
 * expected.
 * 
 * @author Moritz Skowronski
 *
 */
public class KinectImage implements ImageUpdateListener {

	private static final int CAMERAWIDTH = 512;
	private static final int CAMERAHEIGHT = 424;
	

	private PImage image;
	private PImage pointCloudImage;
	private int[] depthData;

	private boolean available;

	public KinectImage(PApplet p) {

		image = p.createImage(CAMERAWIDTH, CAMERAHEIGHT, PApplet.RGB);
		pointCloudImage = p.createImage(CAMERAWIDTH / 4, CAMERAHEIGHT / 4, PApplet.RGB);
		depthData = new int[pointCloudImage.pixels.length];
	}

	/**
	 * Receives the encoded Image of the UDP Stream and decodes it to point
	 * cloud data, transferring the data to Real World Values.
	 * 
	 * @param encodedImage
	 *            the encoded depth values
	 */
	public void updatePointCloud(BufferedImage encodedImage) {
		pointCloudImage.loadPixels();
		encodedImage.getRGB(0, 0, pointCloudImage.width, pointCloudImage.height, pointCloudImage.pixels, 0,
				pointCloudImage.width);
		pointCloudImage.updatePixels();
		for (int i = 0; i < depthData.length; i++) {
			depthData[i] = decodeRGBIntoDepth(pointCloudImage.pixels[i]);
		}
		available = true;
	}

	/**
	 * Simply writes the BufferedImage from the Server into the Image of the
	 * ImageProcessor
	 * 
	 * @param imageFromBuffer
	 *            Image from Server
	 */
	public void updateImage(BufferedImage imageFromBuffer) {
		image.loadPixels();
		imageFromBuffer.getRGB(0, 0, image.width, image.height, image.pixels, 0, image.width);
		image.updatePixels();
		available = true;
	}

	/**
	 * Returns the Point Cloud if the point cloud is from the correct Kinect,
	 * otherwise returns null
	 * 
	 * @return
	 */
	public PImage getPointCloudImage() {
		available = false;
		return pointCloudImage;
	}

	/**
	 * Returns the imageif the image is from the correct Kinect, otherwise
	 * returns null
	 * 
	 * @return
	 */
	public PImage getImage() {
		available = false;
		return image;
	}
	
	public int[] returnDepthData(){
		return depthData;
	}

	/**
	 * Decodes an RGB Value into Real World Depth Value
	 * 
	 * @param rgb
	 *            an encoded rgb Value
	 * @return decoded depth value
	 */
	private int decodeRGBIntoDepth(int rgb) {

		// TODO Check if Shift is working
		float r = (rgb >> 16) & 0xFF;
		float g = (rgb >> 8) & 0xFF;
		float b = rgb & 0xFF;
		int depth = 0;
		if (r == 255) {
			depth += 7000;
		}
		if (r == 223) {
			depth += 6000;
		}
		if (r == 191) {
			depth += 5000;
		}
		if (r == 159) {
			depth += 4000;
		}
		if (r == 127) {
			depth += 3000;
		}
		if (r == 95) {
			depth += 2000;
		}
		if (r == 63) {
			depth += 1000;
		}
		if (r == 31) {
			depth += 0;
		}
		if (r == 0) {
			return 0;
		}
		if (g == 225) {
			depth += 900;
		}
		if (g == 200) {
			depth += 800;
		}
		if (g == 175) {
			depth += 700;
		}
		if (g == 150) {
			depth += 600;
		}
		if (g == 125) {
			depth += 500;
		}
		if (g == 100) {
			depth += 400;
		}
		if (g == 75) {
			depth += 300;
		}
		if (g == 50) {
			depth += 200;
		}
		if (g == 25) {
			depth += 100;
		}
		if (g == 0) {
			depth += 0;
		}
		depth += b;

		return depth;
	}

	/**
	 * Returns the status of the Image
	 * 
	 * @return true, if new image is available
	 */
	public boolean hasNew() {
		return available;
	}

}
