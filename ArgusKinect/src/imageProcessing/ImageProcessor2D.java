package imageProcessing;

import java.util.ArrayList;

import gab.opencv.Contour;
import gab.opencv.OpenCV;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * This class performs Computer Vision Operations to difference possible Persons
 * from the Background using the Depth Stream of the Kinect.
 * 
 * NOTE: There are probably a lot of helpful additions one can add to this class
 * since this approach, although sufficient in most cases, can provide false
 * positives due to the missing logic of how persons actually look and behave.
 * 
 * @author Moritz Skowronski
 *
 */
public class ImageProcessor2D {

	private PApplet p;
	private OpenCV cv;

	private PImage differencedImage;
	private ArrayList<Contour> contours;

	private int[] calibratedDepthValues;

	private int differenceThreshold;
	private int blobSize;

	public ImageProcessor2D(PApplet p, int width, int height) {
		this.p = p;
		this.cv = new OpenCV(this.p, width, height);

		differencedImage = this.p.createImage(width, height, PApplet.RGB);
		contours = new ArrayList<Contour>();

		differenceThreshold = 100;
		blobSize = 5000;

		calibratedDepthValues = new int[width * height];
	}

	/**
	 * Processes the input Data, performs background subtraction and finds
	 * Persons and their contours in the subtracted image
	 * 
	 * @param depthData
	 *            the depthData to be processed
	 */
	public void process(int[] depthData) {
		// subtracts new image from old image
		backgroundSubtraction(depthData);

		// finds Persons in that Subtraction
		findContours();
	}

	/*-------------Image Processing-------------*/

	/*
	 * Calculates a depth average for all pixels to catch false readings
	 */
	public void calibrate(int[] depthData) {

		for (int i = 0; i < depthData.length; i++) {
			if (depthData[i] != 0) {
				if (calibratedDepthValues[i] != 0) {
					// calculates average of old and new depth Reading
					calibratedDepthValues[i] = (calibratedDepthValues[i] + depthData[i]) / 2;
				} else {
					// If there was no depth Reading or the Reading was 0,
					// use
					// the new Reading
					calibratedDepthValues[i] = depthData[i];
				}
			}
		}
	}

	/**
	 * Subtracts the current image from the calibrated Image, leaving only the
	 * changes in depth marked white.
	 * 
	 * @param depthData
	 *            between 0 - 8000
	 */
	private void backgroundSubtraction(int[] depthData) {

		differencedImage.loadPixels();
		for (int i = 0; i < depthData.length; i++) {

			if (depthData[i] == 0) {
				differencedImage.pixels[i] = p.color(0);
			} else {
				if (PApplet.abs(depthData[i] - calibratedDepthValues[i]) > differenceThreshold) {
					differencedImage.pixels[i] = p.color(255);
				} else {
					differencedImage.pixels[i] = p.color(0);
				}
			}
		}
		differencedImage.updatePixels();
	}

	/**
	 * Finds Silhouettes and their contours in an image
	 */
	private void findContours() {

		contours.clear();

		// Load differenced image into opencv
		cv.loadImage(differencedImage);

		// Dilate and Erode for better recognition
		cv.dilate();
		cv.erode();

		// if the Area of the contour is smaller than the threshold, disregard
		// it
		for (Contour contour : cv.findContours()) {

			if (contour.area() < blobSize)
				continue;

			// Add z component to contour
			contours.add(contour);
		}
	}

	/*------------------Getter------------------*/

	/**
	 * Returns the minimum difference in depth between an old and new pixel for
	 * the pixel to be recognized as changed pixel
	 * 
	 * @return
	 */
	public int getDifferenceThreshold() {
		return differenceThreshold;
	}

	/**
	 * Returns the Image derived from comparing the depth background image with
	 * the current depth image. Differences are white, the rest is black
	 * 
	 * @return
	 */
	public PImage getDifferencedImage() {
		return differencedImage;
	}

	/**
	 * Returns the Contours derived from Image Processing
	 * 
	 * @return
	 */
	public ArrayList<Contour> getContours() {
		return contours;
	}

	/*------------------Setter------------------*/

	/**
	 * Sets the minimum difference in depth between an old and new pixel for the
	 * pixel to be recognized as changed pixel
	 * 
	 * @param differenceThreshold
	 */
	public void setDifferenceThreshold(int differenceThreshold) {
		this.differenceThreshold = differenceThreshold;
	}

	/**
	 * Sets the minimum size a contour must have to be recognized as a person
	 * 
	 * @param blobSize
	 */
	public void setBlobSize(int blobSize) {
		this.blobSize = blobSize;
	}

}
