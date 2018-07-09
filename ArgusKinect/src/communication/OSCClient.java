package communication;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import netP5.NetAddress;
import oscP5.*;
import processing.core.PVector;
import scene.Person;
import scene.AbstractTriggerZone;

/**
 * Class for sending formatted OSC Messages to Kinect Server. Very closely
 * related to TUIO, however, because of the amount of additional data, we don't
 * directly use TUIO Processing.
 * 
 * 
 * 
 * @author Moritz Skowronski
 *
 */
public class OSCClient implements UpdateListener {

	// By default, send to localhost
	private static NetAddress netAdress = new NetAddress("127.0.0.1", 3333);
	private static boolean running;

	public static void startClient(String ip, int sendPort) {
		netAdress = new NetAddress(ip, sendPort);
		running = true;
	}

	/**
	 * Send all person related Data via OSC to the Server
	 * 
	 * @param persons
	 *            an Arraylist of all currently tracked Persons
	 * @throws Exception
	 *             if IP is not valid
	 */
	public void personEntered(Person person) {

		OscMessage message = new OscMessage("personEntered");
		message.add("set");
		// ID and Age
		message.add(person.getId());
		message.add(person.getAge());

		// Centroid Data
		message.add("centroid");
		PVector tempVector = person.getScreenCentroid();
		message.add(tempVector.x);
		message.add(tempVector.y);
		message.add(tempVector.z);

		message.add("velocity");
		// Velocity Data
		tempVector = person.getScreenVelocity();
		message.add(tempVector.x);
		message.add(tempVector.y);
		message.add(tempVector.z);

		message.add("acceleration");
		// Acceleration Data
		tempVector = person.getScreenAcceleration();
		message.add(tempVector.x);
		message.add(tempVector.y);
		message.add(tempVector.z);

		message.add("center");
		// Center Data
		tempVector = person.getScreenCenter();
		message.add(tempVector.x);
		message.add(tempVector.y);
		message.add(tempVector.z);

		message.add("contour");
		// Contour Data
		ArrayList<PVector> contour = person.getScreenContour();
		for (int i = 0; i < contour.size(); i += 4) {
			tempVector = contour.get(i);
			message.add(tempVector.x);
			message.add(tempVector.y);
			message.add(tempVector.z);
		}

		sendMessage(message);
	}

	/**
	 * Is called when a Person leaves the ViewPort
	 * 
	 * @param id
	 */
	public void personLeft(int id) {
		OscMessage message = new OscMessage("personLeft");
		message.add(id);

		sendMessage(message);
	}

	/**
	 * Is called when a Person in the ViewPort moved
	 * 
	 * @param person
	 * @param sendContour
	 */
	public void personMoved(Person person) {

		OscMessage message = new OscMessage("personUpdated");
		message.add("set");
		// ID and Age
		message.add(person.getId());
		message.add(person.getAge());

		// Centroid Data
		message.add("centroid");
		PVector tempVector = person.getScreenCentroid();
		message.add(tempVector.x);
		message.add(tempVector.y);
		message.add(tempVector.z);

		message.add("velocity");
		// Velocity Data
		tempVector = person.getScreenVelocity();
		message.add(tempVector.x);
		message.add(tempVector.y);
		message.add(tempVector.z);

		message.add("acceleration");
		// Acceleration Data
		tempVector = person.getScreenAcceleration();
		message.add(tempVector.x);
		message.add(tempVector.y);
		message.add(tempVector.z);

		message.add("center");
		// Center Data
		tempVector = person.getScreenCenter();
		message.add(tempVector.x);
		message.add(tempVector.y);
		message.add(tempVector.z);

		message.add("contour");
		// Contour Data
		ArrayList<PVector> contour = person.getScreenContour();
		for (int i = 0; i < contour.size(); i += 4) {
			tempVector = contour.get(i);
			message.add(tempVector.x);
			message.add(tempVector.y);
			message.add(tempVector.z);
		}

		sendMessage(message);
	}

	/**
	 * Add Timestamp. Then flush message to Server
	 * 
	 * @param message
	 */
	private static void sendMessage(OscMessage message) {

		// Send Timestamp as String, because we can't send long
		message.add("fseq");
		message.add(String.valueOf(System.currentTimeMillis()));

		OscP5.flush(message, netAdress);
	}

	/**
	 * Sent when some action is happening in the triggerzones
	 * 
	 * @param zone
	 * @throws Exception
	 */
	private static void sendTriggerZoneData(AbstractTriggerZone zone) {

		OscMessage message = new OscMessage("triggerzoneUpdate");

		message.add("set");
		// ID
		message.add(zone.getID());
		// Points insideBox
		message.add(zone.getPointsInsideBox());

		Iterator<Entry<Integer, Integer>> mapIterator = zone.getPointsPerPerson().entrySet().iterator();

		while (mapIterator.hasNext()) {

			Map.Entry<Integer, Integer> pair = (Map.Entry<Integer, Integer>) mapIterator.next();
			message.add(pair.getKey());
			message.add(pair.getValue());
		}

		sendMessage(message);
	}

	public void updateTriggerzones(AbstractTriggerZone[] zones) {

		for (int i = 0; i < zones.length; i++) {
			sendTriggerZoneData(zones[i]);
		}
	}

	/**
	 * Send calibration Points for Kinect Adjustment TODO not readily
	 * implemented yet
	 * 
	 * @param points
	 *            4 points of an AR Marker
	 * @throws Exception
	 */
	public static void sendCalibrationPoints(ArrayList<PVector> points) throws Exception {

		if (points.size() != 4) {
			if (!netAdress.isvalid())
				throw new Exception("No valid IP for Client Send");

			OscBundle bundle = new OscBundle();
			Object[] messageContent = new Object[2];
			messageContent[0] = "source";
			String hostIP = "unknown";
			try {
				hostIP = Inet4Address.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				System.err.println("Can't find HostIP");
				e.printStackTrace();
			}
			messageContent[1] = "Kinect@" + hostIP;
			bundle.add(new OscMessage("Kinect/Osc", messageContent));

			messageContent = new Object[2];
			messageContent[0] = "object";
			messageContent[1] = "arMarker";
			bundle.add(new OscMessage("Kinect/Osc", messageContent));

			messageContent = new Object[12];
			for (int i = 0; i < points.size(); i += 3) {
				PVector point = points.get(i);
				messageContent[i] = point.x;
				messageContent[i + 1] = point.y;
				messageContent[i + 2] = point.z;
			}
			bundle.add(new OscMessage("Kinect/Osc", messageContent));

			messageContent = new Object[2];
			messageContent[0] = "fseq";
			messageContent[1] = System.currentTimeMillis();
			bundle.add(new OscMessage("Kinect/Osc", messageContent));

			OscP5.flush(bundle, netAdress);
		} else {
			throw new Exception("Point size isn't 4");
		}
	}

	/**
	 * Returns whether the OSC Client is running
	 * 
	 * @return
	 */
	public static boolean isRunning() {
		return running;
	}

	/**
	 * Sets running variable to false. Only send data when running is true!
	 */
	public static void reset() {
		running = false;
	}

}
