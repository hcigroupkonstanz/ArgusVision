package scene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.map.MultiKeyMap;

import communication.OSCSender;
import visualization.InteractiveTriggerbox;

/**
 * A Scene describes a set of Kinects and the Persons and Triggerzones
 * inhabiting their Viewports. If the Kinect share no common characteristic, ie.
 * overlapping viewports, same room, etc.. then every Kinect should be put in
 * its own Scene.
 * 
 * @author Moritz Skowronski
 *
 */
public class Scene {

	private static int sceneCounter = 0;

	private static int personCounter = 0;

	/**
	 * Scene ID
	 */
	private int id;

	private ArrayList<String> kinects;

	/**
	 * Kinect to Person ID Mapping
	 */
	private MultiKeyMap personMapping;

	/**
	 * The Persons
	 */
	private ConcurrentHashMap<Integer, Person> persons;

	/**
	 * The Triggerzones
	 */
	private ConcurrentHashMap<String, AbstractTriggerZone> triggerzones;

	private HashMap<String, InteractiveTriggerbox> interactiveBoxes;

	/**
	 * Create a new Scene
	 * 
	 * @param name
	 *            Kinect Name
	 */
	public Scene(String name) {

		id = sceneCounter++;

		kinects = new ArrayList<String>();

		kinects.add(name);

		persons = new ConcurrentHashMap<Integer, Person>();

		triggerzones = new ConcurrentHashMap<String, AbstractTriggerZone>();

		interactiveBoxes = new HashMap<String, InteractiveTriggerbox>();

		personMapping = new MultiKeyMap();
	}

	public Scene() {

		id = sceneCounter++;

		kinects = new ArrayList<String>();

		persons = new ConcurrentHashMap<Integer, Person>();

		personMapping = new MultiKeyMap();

		interactiveBoxes = new HashMap<String, InteractiveTriggerbox>();

		triggerzones = new ConcurrentHashMap<String, AbstractTriggerZone>();
	}

	public void addKinect(String name) {
		kinects.add(name);
	}

	// /**
	// * Returns the KeySet of the Kinect Hashmap of the Scene
	// *
	// * @return
	// */
	// public Set<String> getKinectKeys() {
	//
	// return kinects.keySet();
	// }

	public ArrayList<String> getKinectKeys() {
		return kinects;
	}

	/**
	 * Adds a new Person the Scene and gives this Person a unique ID for this
	 * Scene. Persons can be retrieved by specifying their Kinect Name and
	 * Kinect ID
	 * 
	 * @param kinectName
	 *            The Name of the Kinect the Person belongs to
	 * @param person
	 *            the Person
	 */
	public void personEntered(String kinectName, Person person) {

		if (person != null) {
			personMapping.put(kinectName, person.getId(), personCounter);

			OSCSender.personEntered(id, personCounter, person);
			persons.put(personCounter++, person);
		}

	}

	/**
	 * Removes a Person from the Scene
	 * 
	 * @param kinectName
	 *            The Name of the Kinect the Person belongs to
	 * @param id
	 *            the id of the Person
	 */
	public void personLeft(String kinectName, int id) {

		if (personMapping.containsKey(kinectName, id)) {
			int personSceneID = (int) personMapping.get(kinectName, id);

			persons.remove(personSceneID);

			OSCSender.personLeft(this.id, personSceneID);
			personMapping.removeAll(kinectName, id);
		}
	}

	/**
	 * Updates the Persons current info
	 * 
	 * @param kinectName
	 *            The Name of the Kinect the Person belongs to
	 * @param person
	 *            the Person
	 */
	public void personMoved(String kinectName, Person person) {

		if (personMapping.containsKey(kinectName, person.getId())) {
			int personSceneID = (int) personMapping.get(kinectName, person.getId());

			Person personToUpdate = persons.get(personSceneID);

			if (personToUpdate != null) {

				if (personToUpdate.getLastUpdate() <= person.getLastUpdate())
					personToUpdate.update(person);

				OSCSender.personMoved(id, personSceneID, personToUpdate);
			}
		} else {

			personEntered(kinectName, person);
		}
	}

	/**
	 * Returns an array of current Persons
	 * 
	 * @return
	 */
	public Person[] getPersons() {

		Person[] personsArray = new Person[persons.values().size()];
		persons.values().toArray(personsArray);

		return personsArray;
	}

	public int getId() {
		return id;
	}

	/**
	 * Returns an array of all current triggerzones
	 * 
	 * @return
	 */
	public AbstractTriggerZone[] getTriggerZones() {

		AbstractTriggerZone[] triggerArray = new AbstractTriggerZone[triggerzones.values().size()];
		triggerzones.values().toArray(triggerArray);

		Arrays.sort(triggerArray, new Comparator<AbstractTriggerZone>() {
			@Override
			public int compare(AbstractTriggerZone a1, AbstractTriggerZone a2) {
				int x = (Integer.parseInt(a1.getID().substring(11)));
				int y = (Integer.parseInt(a2.getID().substring(11)));
				return x - y;
			}
		});

		return triggerArray;
	}

	public InteractiveTriggerbox[] getInteractiveZones() {

		InteractiveTriggerbox[] triggerArray = new InteractiveTriggerbox[interactiveBoxes.values().size()];
		interactiveBoxes.values().toArray(triggerArray);

		return triggerArray;
	}

	/**
	 * Add new Triggerzone to Hashmap
	 * 
	 * @param zone
	 */
	public void addNewTriggerZone(AbstractTriggerZone zone, remixlab.proscene.Scene displayableScene) {

		triggerzones.put(zone.getID(), zone);
		if (zone.getClass() == TriggerBox.class) {

			interactiveBoxes.put(zone.getID(), new InteractiveTriggerbox((TriggerBox) zone, displayableScene));
		}
	}

	/**
	 * Delete Triggerzone from Hashmap
	 * 
	 * @param triggerzone
	 */
	public void deleteTriggerzone(String triggerzone) {

		triggerzones.remove(triggerzone);
		interactiveBoxes.remove(triggerzone);
	}

	/**
	 * Returns the Triggerzone with the given name
	 * 
	 * @param name
	 * @return
	 */
	public AbstractTriggerZone getTriggerZone(String name) {

		return triggerzones.get(name);
	}

	/**
	 * Updates the triggerzone with the specified values.
	 * 
	 * @param kinectName
	 * @param triggerID
	 * @param depthPointsInZone
	 * @param pointsPerPerson
	 * @param updateTime
	 * @return
	 */
	public void updateTriggerzone(String kinectName, String triggerID, int depthPointsInZone,
			HashMap<Integer, Integer> pointsPerPerson, Long updateTime) {

		boolean changed = false;
		// Update Values

		AbstractTriggerZone zone = getTriggerZone(triggerID);

		if (zone != null) {
			if (zone.getLastUpdate() <= updateTime) {
				if (zone.getPointsInsideBox() != depthPointsInZone) {
					zone.setPointsInsideBox(depthPointsInZone);
					changed = true;
				}

				if (!pointsPerPerson.equals(zone.getPointsPerPerson())) {

					zone.setPointsPerPerson(pointsPerPerson);
					changed = true;
				}
				zone.setLastUpdate(updateTime);
			}

			// If there has been a change, send these changes to the OSC Sender
			// to
			// send them to the given listener.
			if (changed) {

				// We have to send the corrected IDs for the scene to the OSC
				// Receiver, so there are no duplicate
				// IDs
				HashMap<Integer, Integer> scenePersonsInTriggerZone = new HashMap<Integer, Integer>();

				Iterator<Entry<Integer, Integer>> iterator = zone.getPointsPerPerson().entrySet().iterator();

				while (iterator.hasNext()) {

					// Switch ids
					Entry<Integer, Integer> entry = iterator.next();

					// Triggerzones and persons are not synchronized due to OSC
					// If a person leaves this can cause a NullPointerException
					if (personMapping.containsKey(kinectName, entry.getKey())) {
						int id = (int) personMapping.get(kinectName, entry.getKey());

						// and put them in a new HashMap
						scenePersonsInTriggerZone.put(id, entry.getValue());
					}
				}

				OSCSender.triggerzoneUpdate(id, zone.getID(), zone.getPointsInsideBox(), scenePersonsInTriggerZone);
			}
		}
	}

}
