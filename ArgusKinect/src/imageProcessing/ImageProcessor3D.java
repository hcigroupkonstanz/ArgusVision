package imageProcessing;

import java.util.ArrayList;

import helpers.CloudPoint3D;
import helpers.Enum;
import processing.core.PImage;
import processing.core.PVector;

/**
 * This class provides functionality for processing a 3d cloud Image TODO not yet implemented
 * @author Moritz Skowronski
 *
 */
public class ImageProcessor3D {

	private static final int K_WIDTH = 512;
	private static final int K_HEIGHT = 424;
	private static ArrayList<ArrayList<CloudPoint3D>> blobs = new ArrayList<ArrayList<CloudPoint3D>>();
	private static int k_Pix;

	public static void findBlobs(PImage maskedImage, CloudPoint3D[] cloudPoints, PVector thresh3D, int thresh2D,
			int minPoints, float minVol, float maxVol, int maxBlobs) {
		k_Pix = maskedImage.width * maskedImage.height;

		ArrayList<ArrayList<CloudPoint3D>> tempBlobs = new ArrayList<ArrayList<CloudPoint3D>>();
		int queueIndex = 0;
		int lastQueued = 0;
		int pixIndex = 0;
		int pixelsToProcess = k_Pix;

		int[] queue = new int[pixelsToProcess];

		int numBlobs = 0;
		blobs.clear();

		float minX, minY, minZ, maxX, maxY, maxZ = 0f;

		PVector minXPos, minYPos, minZPos, maxXPos, maxYPos, maxZPos;

		while ((pixIndex < k_Pix) && (pixelsToProcess > 0) && tempBlobs.size() < maxBlobs) {
			queueIndex = 0;
			lastQueued = 0;
			minX = minY = minZ = 100;
			maxX = maxY = maxZ = -100;

			// Jump to the first pixel that needs to be processed
			while ((pixIndex < k_Pix) && cloudPoints[pixIndex].flag != Enum.PointCloudFlag.FLAG_IDLE) {
				pixIndex++;
			}

			// All Pixels processed, break
			if (pixIndex == k_Pix)
				break;
			else
				queue[0] = pixIndex;

			int queueIndexPix = queue[queueIndex];
			// While not end of queue
			while ((lastQueued < k_Pix) && (queueIndex <= lastQueued) && queueIndex >= 0 && (queueIndexPix < k_Pix)
					&& (pixelsToProcess > 0)) {
				
			}
		}
	}
}
