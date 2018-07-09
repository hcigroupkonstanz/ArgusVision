package kinect;

import KinectPV2.KinectPV2;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Implementation of Kinect library functionality for Windows
 * @author Moritz Skowronski
 *
 */
public class KinectWindows extends AbstractKinect{
	
	private PApplet p;
	private KinectPV2 kinect;
	private int[] depthData;
	private PImage colorImage;
	private PImage depthImage;
	private PImage bodyTrackImage;
	private PImage irImage;
	
	public KinectWindows(PApplet p) {
		this.p = p;
		kinect = new KinectPV2(this.p);
	}

	@Override
	public void startKinect() {
		kinect.enableColorImg(true);
		kinect.enableDepthImg(true);
		kinect.enableInfraredLongExposureImg(true);
		kinect.enableBodyTrackImg(true);
		kinect.init();
		colorImage = p.createImage(KinectPV2.WIDTHColor, KinectPV2.HEIGHTColor, PApplet.RGB);
		depthImage = p.createImage(KinectPV2.WIDTHDepth, KinectPV2.HEIGHTDepth, PApplet.RGB);
		bodyTrackImage = p.createImage(KinectPV2.WIDTHDepth, KinectPV2.HEIGHTDepth, PApplet.RGB);
		irImage = p.createImage(KinectPV2.WIDTHDepth, KinectPV2.HEIGHTDepth, PApplet.RGB);
	}
	
	@Override
	public void closeKinect() {
		kinect.dispose();
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
		colorImage = kinect.getColorImage();
	}

	@Override
	public void updateDepthImage() {

		depthData = kinect.getRawDepthData();
		depthImage.loadPixels();
		for (int i = 0; i < depthData.length; i++) {
			depthImage.pixels[i] = p.color((int)PApplet.map(depthData[i], 0, 8000, 0, 256));
		}
		depthImage.updatePixels();
	}

	@Override
	public void updateIRImage() {
		irImage = kinect.getInfraredLongExposureImage();
	}

	@Override
	public void updateBodyTrackImage() {
		bodyTrackImage = kinect.getBodyTrackImage();
	}

	@Override
	public int[] getRawDepthData() {
		return depthData;
	}
	
	

}
