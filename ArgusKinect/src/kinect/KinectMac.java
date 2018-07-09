package kinect;

import org.openkinect.processing.Kinect2;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Implementation of Kinect library functionality for Mac
 * @author Moritz Skowronski
 *
 */
public class KinectMac extends AbstractKinect {

	private PApplet p;
	private Kinect2 kinect;
	private int[] depthData;
	private PImage colorImage;
	private PImage depthImage;
	private PImage bodyTrackImage;
	private PImage irImage;

	public KinectMac(PApplet p) {
		this.p = p;
		this.kinect = new Kinect2(this.p);
	}

	@Override
	public void startKinect() {
		kinect.initVideo();
		kinect.initDepth();
		kinect.initIR();
		kinect.initDevice();

		colorImage = p.createImage(kinect.colorWidth, kinect.colorHeight, PApplet.RGB);
		depthImage = p.createImage(kinect.depthWidth, kinect.depthHeight, PApplet.GRAY);
		bodyTrackImage = p.createImage(kinect.depthWidth, kinect.depthHeight, PApplet.RGB);
		irImage = p.createImage(kinect.depthWidth, kinect.depthHeight, PApplet.RGB);
	}

	public void closeKinect() {
		kinect.stopDevice();
	}

	@Override
	public PImage getColorImage() {
		return colorImage;
	}

	@Override
	public PImage getDepthImage() {
		return depthImage;
	}

	@Override
	public PImage getIRImage() {
		return irImage;
	}

	@Override
	public PImage getBodyTrackImage() {
		return bodyTrackImage;
	}

	@Override
	public void updateColorImage() {
		colorImage = kinect.getVideoImage();
	}

	@Override
	public void updateDepthImage() {
		depthData = kinect.getRawDepth();
		depthImage.loadPixels();
		for (int i = 0; i < depthData.length; i++) {
			depthImage.pixels[i] = (int)PApplet.map(depthData[i], 0, 8000, 0, 256);
		}
		depthImage.updatePixels();
		depthImage = kinect.getDepthImage();
	}

	@Override
	public void updateIRImage() {
		irImage = kinect.getIrImage();
	}

	@Override
	public void updateBodyTrackImage() {
		// TODO This isn't working right now, make it black so it doesn't throw
		// exception
		bodyTrackImage.loadPixels();
		for (int i = 0; i < bodyTrackImage.pixels.length; i++) {
			bodyTrackImage.pixels[i] = 0;
		}
		bodyTrackImage.updatePixels();
	}

	@Override
	public int[] getRawDepthData() {
		return depthData;
	}

}
