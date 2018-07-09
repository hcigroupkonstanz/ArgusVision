package imageProcessing;

import kinect.AbstractKinect;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * This class transforms Pointcloud values into rgb Pixels to be able to stream
 * them via UDP
 * 
 * @author Moritz Skowronski
 *
 */
public class PointCloudCreation {

	private PApplet p;
	private static int recalcX = 128;
	private static int recalcY = 106;

	public PointCloudCreation(PApplet p) {
		this.p = p;
	}

	/**
	 * transforms a depthStream into a 2d PImage with "Point Cloud Pixels"
	 * 
	 * @param depth
	 * @return
	 */
	public PImage rawDepthToPointCloud(int[] depth) {
		PImage image = p.createImage(recalcX, recalcY, PApplet.RGB);
		
		float factorX = recalcX/AbstractKinect.DEPTH_WIDTH;
		float factorY = recalcY/AbstractKinect.DEPTH_HEIGHT;
		
		image.loadPixels();
		for (int x = 0; x < 512; x += 4) {
			for (int y = 0; y < 424; y += 4) {
				int offset = y * 512 + x;
				image.pixels[y / 4 * recalcX + x / 4] = depthToPointCloudPixel(depth[offset]);
			}
		}
		image.updatePixels();
		return image;
	}

	/**
	 * transforms depth into an rgb pixel
	 * 
	 * @param depth
	 * @return
	 */
	private int depthToPointCloudPixel(int depth) {
		int r = 0;
		int g = 0;
		int b = 0;

		if (depth == 0) {
			return p.color(0, 0, 0);
		}
		
		if (depth == 8000) {
			return p.color(255, 255, 255);
		}
		
		if (depth < 8000) {
			r = 255;
		}
		if (depth < 7000) {
			r = 223;
		}
		if (depth < 6000) {
			r = 191;
		}
		if (depth < 5000) {
			r = 159;
		}
		if (depth < 4000) {
			r = 127;
		}
		if (depth < 3000) {
			r = 95;
		}
		if (depth < 2000) {
			r = 63;
		}
		if (depth < 1000) {
			r = 31;
		}
		
		int temp1 = depth % 1000;
		
		if (temp1 < 1000) {
			g = 225;
		}
		if (temp1 < 900) {
			g = 200;
		}
		if (temp1 < 800) {
			g = 175;
		}
		if (temp1 < 700) {
			g = 150;
		}
		if (temp1 < 600) {
			g = 125;
		}
		if (temp1 < 500) {
			g = 100;
		}
		if (temp1 < 400) {
			g = 75;
		}
		if (temp1 < 300) {
			g = 50;
		}
		if (temp1 < 200) {
			g = 25;
		}
		if (temp1 < 100) {
			g = 0;
		}
		
		temp1 %= 100;
		b = temp1;
		return p.color(r, g, b);
	}
}
