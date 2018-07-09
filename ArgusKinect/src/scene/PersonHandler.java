package scene;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import communication.UpdateListener;
import gab.opencv.Contour;
import helpers.PersonMath;
import helpers.ThreadingResult;
import kinect.AbstractKinect;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import scene.TriggerBox;

/**
 * This class listens to changes from the ImageProcessor, analyzes the contours
 * and updates all the Persons in the View.
 * 
 * 
 * @author Moritz Skowronski
 *
 */
public class PersonHandler {

	private PApplet p;

	private ArrayList<Person> persons;

	private AbstractTriggerZone[] triggerzones;

	private UpdateListener listener;

	private PGraphics maskGraphic;

	// This is used to check whether a person is the same,
	private float idRadius;

	public PersonHandler(PApplet p, UpdateListener listener) {

		this.p = p;
		this.listener = listener;

		maskGraphic = p.createGraphics(AbstractKinect.DEPTH_WIDTH, AbstractKinect.DEPTH_HEIGHT, PApplet.P2D);
		persons = new ArrayList<Person>();
		triggerzones = new AbstractTriggerZone[0];
		idRadius = 1000.0f;
	}

	/*---------------Main Methods---------------*/

	/**
	 * Analyzes all Contours provided in an ArrayList and creates Persons from
	 * it
	 * 
	 * @param contours
	 *            List of OpenCV Contours
	 */
	public void analyzeContour(ArrayList<Contour> contours, int[] rawDepthData) {
		// Reset the Triggerzones, so that they are filled again
		for (int i = 0; i < triggerzones.length; i++) {
			triggerzones[i].clearPoints();
		}

		// Create an Arraylist that stores all updated Persons, so we don't
		// update them twice
		ArrayList<Person> tempPersons = new ArrayList<Person>();
		for (Contour contour : contours) {

			// Calculates all Person Infos, like Centroid, Bounding Box etc.
			Person tempPerson;
			try {
				tempPerson = calculateAll(contour.getPoints(), rawDepthData);
				tempPersons.add(tempPerson);
			} catch (InterruptedException e) {
				System.err.println("Error in multithreaded code @ People Analyzing");
				e.printStackTrace();
			} catch (ExecutionException e) {
				System.err.println("Error in multithreaded code @ People Analyzing");
				e.printStackTrace();
			}

		}
		updatePersons(tempPersons);
	}

	/**
	 * Updates the existing persons with new Persons (performs the tracking)
	 * 
	 * @param tempPersons
	 *            new Persons
	 */
	private void updatePersons(ArrayList<Person> tempPersons) {

		// Create a distance Matrix
		float[][] distanceMatrix = new float[tempPersons.size()][persons.size()];

		// Fill the distance Matrix with the distances between Centroids
		for (int i = 0; i < distanceMatrix.length; i++) {
			for (int j = 0; j < distanceMatrix[i].length; j++) {
				distanceMatrix[i][j] = PVector.dist(tempPersons.get(i).getCentroid(), persons.get(j).getCentroid());
			}
		}

		// We have to process all new Contours and either update existing
		// Persons or add new ones
		int oldPersonsToProcess = persons.size();
		int newPersonsToProcess = tempPersons.size();
		boolean[] processedNewPersons = new boolean[newPersonsToProcess];
		boolean[] processedOldPersons = new boolean[oldPersonsToProcess];

		float temporaryMinimum = Float.MAX_VALUE;

		// This can be done, because of the temporary Minimum < idRadius check.
		// Could lead to NullPointer
		// Exceptions or false positives, if temporaryMinimum wouldn't be Max
		// Value
		int oldPersonID = 0;
		int newPersonID = 0;

		/**
		 * TODO k�nnte ich nicht am anfang einmal die doppelte for durchlaufen
		 * und dann sortiert die werte abspeichern, die unter dem threshold
		 * liegen?!
		 */

		while (oldPersonsToProcess > 0) {

			temporaryMinimum = Float.MAX_VALUE;

			for (int i = 0; i < distanceMatrix.length; i++) {

				if (processedNewPersons[i])
					continue;

				for (int j = 0; j < distanceMatrix[i].length; j++) {

					if (processedOldPersons[j])
						continue;

					if (distanceMatrix[i][j] < temporaryMinimum) {

						// if we found a minimum, save the distance as well
						// as
						// the current ids
						temporaryMinimum = distanceMatrix[i][j];
						oldPersonID = j;
						newPersonID = i;
					}

				}
			}

			// If the minimum is below the threshold it's an update of an old
			// Person
			if (Math.ceil(temporaryMinimum) <= idRadius) {

				processedOldPersons[oldPersonID] = true;
				processedNewPersons[newPersonID] = true;

				newPersonsToProcess--;

				Person tempPerson = tempPersons.get(newPersonID);
				persons.get(oldPersonID).update(tempPerson.getCentroid(), tempPerson.getCenter(),
						tempPerson.getContour(), tempPerson.getBoundingBox());
				for (int i = 0; i < triggerzones.length; i++) {
					if (tempPerson.getOccupiesPointsPerTriggerzone().containsKey(triggerzones[i].getID())) {
						// Add the Points with the correct ID to the Triggerzone
						triggerzones[i].addToPointsPerPerson(persons.get(oldPersonID).getId(),
								tempPerson.getOccupiesPointsPerTriggerzone().get(triggerzones[i].getID()));
					}
				}
				personMoved(persons.get(oldPersonID));
			} else {
				// Because there won't be anything smaller we are done
				break;
			}

			oldPersonsToProcess--;
		}

		// now delete old not updated Persons
		// if there are not updated old Persons
		if (oldPersonsToProcess > 0) {
			// run backwards, so the indexes stay the same
			for (int i = processedOldPersons.length - 1; i >= 0; i--) {

				// if person hasn't been processed
				if (!processedOldPersons[i]) {
					// delete
					personLeft(persons.get(i).getId());
					persons.remove(i);
				}
			}
		}

		// add new unprocessed Persons

		if (newPersonsToProcess > 0) {

			for (int i = 0; i < processedNewPersons.length; i++) {

				if (!processedNewPersons[i]) {

					// Add new Person

					Person tempPerson = tempPersons.get(i);

					Person person = new Person(tempPerson.getCentroid(), tempPerson.getCenter(),
							tempPerson.getContour(), tempPerson.getBoundingBox());

					for (int j = 0; j < triggerzones.length; j++) {

						if (tempPerson.getOccupiesPointsPerTriggerzone().containsKey(triggerzones[j].getID())) {

							// Add the Points with the correct ID to the
							// Triggerzone
							triggerzones[j].addToPointsPerPerson(person.getId(),
									tempPerson.getOccupiesPointsPerTriggerzone().get(triggerzones[j].getID()));
						}
					}

					personEntered(person);

					persons.add(person);
				}
			}
		}

		updateTriggerzones();
	}

	/**
	 * Calculates all relevant person Infos
	 * 
	 * @param contour
	 *            the contour of a person
	 * @param triggerzone
	 *            the triggerzones for which to check whether a person is inside
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public Person calculateAll(ArrayList<PVector> contour, int[] rawDepthData)
			throws InterruptedException, ExecutionException {

		// Construct a maskimage as background buffer
		maskGraphic.beginDraw();
		maskGraphic.clear();
		maskGraphic.fill(255);
		maskGraphic.beginShape();

		for (int i = 0; i < contour.size(); i += 4) {
			maskGraphic.vertex(contour.get(i).x, contour.get(i).y);
		}
		maskGraphic.vertex(contour.get(0).x, contour.get(0).y);
		maskGraphic.endShape();
		maskGraphic.endDraw();

		Person person = new Person();

		// Calculate Bounding Box
		PShape boundingBox = PersonMath.getBoundingBox(p, contour);

		PVector centroid = new PVector();

		// counts the number of pixel of the person
		int centroidCounter = 0;

		/**
		 * Multithread this operation as this is very expensive Divide the
		 * buffer into parts (depending on how many processors we have) and
		 * analyze the image seperately
		 */
		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService service = Executors.newFixedThreadPool(threads);
		List<Future<ThreadingResult>> futures = new ArrayList<Future<ThreadingResult>>();

		// Divide
		int xstart = (int) boundingBox.getParam(0);
		int xend = (int) boundingBox.getParam(2) + 1;
		int ystart = (int) boundingBox.getParam(1);

		maskGraphic.loadPixels();
		int ystep = (int) (boundingBox.getParam(3) - ystart) / threads;

		int yend = (int) ystart + ystep;

		// Start threads
		for (int i = 0; i < threads; i++) {
			if (i == threads - 1)
				yend = (int) boundingBox.getParam(3) + 1;
			Callable<ThreadingResult> callable = new ThreadingResult(triggerzones, rawDepthData, maskGraphic, p, xstart,
					xend, ystart, yend);

			futures.add(service.submit(callable));

			ystart += ystep;
			yend += ystep;
		}

		service.shutdown();

		// then combine all the results
		List<ThreadingResult> results = new ArrayList<ThreadingResult>();
		for (Future<ThreadingResult> threadingResult : futures) {
			results.add(threadingResult.get());
		}

		for (ThreadingResult threadingResult : results) {
			centroid.add(threadingResult.centroid);
			centroidCounter += threadingResult.centroidCounter;

			Iterator<Map.Entry<String, Integer>> triggerIterator = threadingResult.triggerPoints.entrySet().iterator();
			while (triggerIterator.hasNext()) {

				Map.Entry<String, Integer> entry = triggerIterator.next();

				person.addToPointsPerPerson(entry.getKey(), entry.getValue());
			}
		}
		// Divide by all pixels so we get an average result
		centroid.div(centroidCounter);

		// calculates the center
		PVector center = new PVector(boundingBox.getParam(0) + (boundingBox.getParam(2) - boundingBox.getParam(0)) / 2,
				boundingBox.getParam(1) + (boundingBox.getParam(3) - boundingBox.getParam(1)) / 2);
		// Done
		person.update(centroid, center, contour, boundingBox);

		return person;
	}

	/*------------------Setter------------------*/

	/**
	 * Set Triggerzones to given value
	 * 
	 * @param triggerzones
	 */
	public void setTriggerZones(AbstractTriggerZone[] triggerzones) {

		this.triggerzones = triggerzones;
	}

	public void initializeTriggerZones() {

		for (int i = 0; i < triggerzones.length; i++) {
			if (triggerzones[i].getClass() == TriggerBox.class) {

				TriggerBox box = (TriggerBox) triggerzones[i];
				box.calculateEdges();
				box.calculateNormalizedNormals();
			}

		}
	}

	/*--------------Notifications-------------*/

	/**
	 * Passes the new Person to the listener
	 * 
	 * @param person
	 */
	private void personEntered(Person person) {
		listener.personEntered(person);
	}

	/**
	 * Passes the id of the person who left to the listener
	 * 
	 * @param id
	 */
	private void personLeft(int id) {
		listener.personLeft(id);
	}

	/**
	 * Passes the Person who moved to the listener
	 * 
	 * @param person
	 */
	private void personMoved(Person person) {
		listener.personMoved(person);
	}

	/**
	 * Passes the updated triggerzones to the listener
	 */
	private void updateTriggerzones() {
		listener.updateTriggerzones(triggerzones);
	}

}
