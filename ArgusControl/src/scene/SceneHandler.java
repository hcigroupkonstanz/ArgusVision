package scene;

import java.util.ArrayList;
import java.util.HashMap;
import processing.core.PVector;

/**
 * This class handles distribution of all Scene related data.
 * 
 * @author Moritz Skowronski
 *
 */
public class SceneHandler implements OSCUpdateListener {

	/**
	 * Holds all the Scenes, is pretty bad perfomance wise, but shouldn't be a
	 * problem for a small number of Scenes
	 */
	private ArrayList<Scene> scenes;

	public SceneHandler() {

		scenes = new ArrayList<Scene>();
	}

	/**
	 * Resets all saved Scenes and ID Mappings. Use if one wants to add a new
	 * configuration
	 */
	public void reset() {
		scenes.clear();
	}

	/**
	 * Combines the Scene which holds Kinect1 with the Scene which holds Kinect2
	 * 
	 * TODO not yet implemented
	 * 
	 * @param kinect1
	 *            the Name of the first Kinect
	 * @param kinect2
	 *            the Name of the second Kinect
	 */
	public void combineKinectsToScene(String kinect1, String kinect2) {

	}

	/**
	 * Creates a new Scene out of a newly connected Kinect
	 * 
	 * @param name
	 *            the name of the Kinect
	 */
	public void createScene(String name) {

		if (getKinectScene(name) == null) {
			scenes.add(new Scene(name));
		} else {
			System.out.println("Kinect already has a Scene, using the old one");
		}
	}

	/**
	 * Gets the id of the scene in which the Kinect is. If the Kinect isn't in
	 * any scene return null
	 * 
	 * @param name
	 *            the name of the Kinect
	 * @return the scene, otherwise null
	 */
	public Scene getKinectScene(String name) {

		for (Scene scene : scenes) {
			for (String kinectName : scene.getKinectKeys()) {
				if (kinectName.equals(name)) {
					return scene;
				}
			}
		}
		return null;
	}

	@Override
	public void personEntered(String name, Person person) {

		Scene kinectScene = getKinectScene(name);

		if (kinectScene != null) {

			kinectScene.personEntered(name, person);
		}
	}

	@Override
	public void personLeft(String name, int id) {

		Scene kinectScene = getKinectScene(name);

		if (kinectScene != null) {

			kinectScene.personLeft(name, id);
		}
	}

	@Override
	public void personMoved(String name, Person person) {

		Scene kinectScene = getKinectScene(name);

		if (kinectScene != null) {

			getKinectScene(name).personMoved(name, person);
		}
	}

	// TODO not yet implemented
	public void updateKinectCalibration(String first, String second, ArrayList<PVector> firstPoints,
			ArrayList<PVector> secondPoints) {

	}

	@Override
	public void updateTriggerZone(String name, String id, int depthPointsInZone,
			HashMap<Integer, Integer> pointsPerPerson, Long updateTime) {

		getKinectScene(name).updateTriggerzone(name, id, depthPointsInZone, pointsPerPerson, updateTime);
	}

}
