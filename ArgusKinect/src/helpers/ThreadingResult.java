package helpers;

import java.util.HashMap;
import java.util.concurrent.Callable;

import kinect.AbstractKinect;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import scene.AbstractTriggerZone;
import scene.TriggerBox;

/**
 * Is used for a multithreaded processing of a maskimage. A maskimage contains
 * only a white silhouette of a person, the rest is black. Checks whether a pixel
 * belongs to a person, if so, check if the pixel belongs to a triggerzone and 
 * add this pixel to further centroid calculation.
 * 
 * @author Moritz Skowronski
 *
 */
public class ThreadingResult implements Callable<ThreadingResult> {

	public PVector centroid;

	public int centroidCounter;

	private PGraphics maskGraphic;

	private int xstart, xend, ystart, yend;

	private AbstractTriggerZone[] triggerzones;
	
	public HashMap<String, Integer> triggerPoints;

	private int[] rawDepthData;

	private PApplet p;

	public ThreadingResult(AbstractTriggerZone[] zones, int[] rawdepthdata, PGraphics maskGraphic, PApplet p,
			int xstart, int xend, int ystart, int yend) {

		centroid = new PVector();

		centroidCounter = 0;

		this.p = p;

		this.xstart = xstart;

		this.xend = xend;

		this.ystart = ystart;

		this.yend = yend;

		this.triggerzones = zones;

		this.rawDepthData = rawdepthdata;

		this.maskGraphic = maskGraphic;
		
		triggerPoints = new HashMap<String, Integer>();
	}

	@Override
	public ThreadingResult call() throws Exception {

		// Adding 1 to the bounding box to avoid missing a pixel due to float to
		// integer casting
		// TODO performanter von 40 auf 10
		for (int x = xstart; x < xend; x += 2) {
			for (int y = ystart; y < yend; y += 2) {

				int offset = y * AbstractKinect.DEPTH_WIDTH + x;
				// if the pixel is white, it's part of the person
				if (maskGraphic.pixels[offset] == p.color(255)) {

					// Add to Centroid
					centroid.add(x, y, rawDepthData[offset]);
					centroidCounter++;
					// test if inside triggerzone
					for (int i = 0; i < triggerzones.length; i++) {

						PVector pointToCheck = PersonMath.depthToPointCloudPos(x, y, rawDepthData[offset]);

						// test whether abstract class is triggerbox
						if (triggerzones[i].getClass() == TriggerBox.class) {
							if (PersonMath.isInsideTriggerBox(pointToCheck, (TriggerBox) triggerzones[i])) {

								// if so perform check and add points to person
								// and triggerzone
								triggerzones[i].addToPoints(4);
								String id = triggerzones[i].getID();
								
								// save which points belong to which triggerzone
								// This is done, so we can later check which person was 
								// in the triggerzone since we don't know the id for now
								if(triggerPoints.containsKey(id))
									triggerPoints.put(id, triggerPoints.get(id)+4);
								else
									triggerPoints.put(id, 4);
							}
						}

					}
				}
			}
		}

		return this;
	}

}
