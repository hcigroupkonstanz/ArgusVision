package communication;

import oscP5.OscMessage;

import oscP5.OscP5;
import processing.core.PApplet;
import processing.core.PVector;
import scene.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import netP5.NetAddress;

/**
 * Provides functionality to send OSC Data to a NetAdress
 * 
 * @author Moritz Skowronski
 *
 */
public class OSCSender {

	// used to have a commong frameCounter
	public static PApplet p;

	private static NetAddress remoteLocation;

	private static boolean isReady;

	/**
	 * Sets the netAddress the osc client is going to send to to the ip and port
	 * arguments.
	 * 
	 * @param ip
	 * @param port
	 * @return true, if successful
	 */
	public static boolean setNetAddress(String ip, int port) {

		if (validIP(ip)) {
			remoteLocation = new NetAddress(ip, port);
			isReady = true;
		} else {
			isReady = false;
		}

		return isReady;
	}

	/**
	 * Sets the PApplet to get the framecount from
	 * 
	 * @param applet
	 */
	public static void setPApplet(PApplet applet) {
		p = applet;
	}

	/**
	 * Tests whether IP is valid
	 * 
	 * @param ip
	 * @return
	 */
	private static boolean validIP(String ip) {
		try {
			if (ip == null || ip.isEmpty()) {
				return false;
			}

			String[] parts = ip.split("\\.");
			if (parts.length != 4) {
				return false;
			}

			for (String s : parts) {
				int i = Integer.parseInt(s);
				if ((i < 0) || (i > 255)) {
					return false;
				}
			}
			if (ip.endsWith(".")) {
				return false;
			}

			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	/**
	 * Sends the new Person to the specified netAddress
	 * 
	 * @param person
	 */
	public static void personEntered(int sceneID, int personID, Person person) {

		if (isReady) {

			OscMessage message = new OscMessage("/argusControl/personEntered");

			message.add("set");

			message.add(sceneID + "-" + personID);
			message.add(person.getAge());

			PVector tempVector = person.getCentroid();
			message.add("centroid");
			message.add(tempVector.x);
			message.add(tempVector.y);
			message.add(tempVector.z);

			message.add("contour");
			ArrayList<PVector> contour = person.getContour();
			for (int i = 0; i < contour.size(); i++) {
				tempVector = contour.get(i);
				message.add(tempVector.x);
				message.add(tempVector.y);
				message.add(tempVector.z);
			}
			sendMessage(message);
		}
	}

	/**
	 * Sends the updated Person to the specified netAdress
	 * 
	 * @param sceneID
	 * @param personID
	 * @param person
	 */
	public static void personMoved(int sceneID, int personID, Person person) {

		if (isReady) {

			OscMessage message = new OscMessage("/argusControl/personUpdated");
			message.add("set");

			message.add(sceneID + "-" + personID);
			message.add(person.getAge());

			PVector tempVector = person.getCentroid();
			message.add("centroid");
			message.add(tempVector.x);
			message.add(tempVector.y);
			message.add(tempVector.z);

			message.add("velocity");
			tempVector = person.getVelocity();
			message.add(tempVector.x);
			message.add(tempVector.y);
			message.add(tempVector.z);

			message.add("acceleration");
			tempVector = person.getAcceleration();
			message.add(tempVector.x);
			message.add(tempVector.y);
			message.add(tempVector.z);

			message.add("contour");
			ArrayList<PVector> contour = person.getContour();
			for (int i = 0; i < contour.size(); i++) {
				tempVector = contour.get(i);
				message.add(tempVector.x);
				message.add(tempVector.y);
				message.add(tempVector.z);
			}
			sendMessage(message);
		}
	}

	/**
	 * Sends the id of the person that left the scene to the specified netAdress
	 * 
	 * @param sceneID
	 * @param personID
	 */
	public static void personLeft(int sceneID, int personID) {

		if (isReady) {

			OscMessage message = new OscMessage("/argusControl/personLeft");

			message.add("set");

			message.add(sceneID + "-" + personID);

			sendMessage(message);
		}
	}

	/**
	 * Sends the updated Triggerzone Data to the specified netAdress
	 * 
	 * @param zone
	 */
	public static void triggerzoneUpdate(int sceneID, String zoneID, int pointsInsideZone,
			HashMap<Integer, Integer> personsInsideZone) {

		if (isReady) {

			OscMessage message = new OscMessage("/argusControl/triggerzone");

			message.add("set");

			message.add(sceneID + "-" + zoneID);

			message.add("pointsOccupied");
			message.add(pointsInsideZone);

			message.add("personsInsideZone");

			Iterator<Entry<Integer, Integer>> mapIterator = personsInsideZone.entrySet().iterator();

			while (mapIterator.hasNext()) {

				Entry<Integer, Integer> pair = (Entry<Integer, Integer>) mapIterator.next();

				message.add(sceneID + "-" + pair.getKey());
				
				message.add(pair.getValue());
			}

			sendMessage(message);
		}
	}

	/**
	 * Adds the frameCount to the message and sends the message to the specified
	 * Address
	 * 
	 * @param message
	 */
	private static void sendMessage(OscMessage message) {

		message.add("fseq");
		message.add(p.frameCount);

		OscP5.flush(message, remoteLocation);
	}

	/**
	 * Stops the OSCSender
	 */
	public static void stop() {

		isReady = false;
	}

}
