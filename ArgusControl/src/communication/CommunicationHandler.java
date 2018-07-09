package communication;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import control.Control;
import control.Settings;
import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscStatus;
import processing.core.PVector;
import scene.Person;
import scene.OSCUpdateListener;

/**
 * This is a management class handling all incoming and outgoind communications.
 * It notifies all affected classes about the changes in data.
 * 
 * @author Moritz Skowronski
 *
 */
public class CommunicationHandler implements TCPConnectListener, OscEventListener {

	private HashMap<String, TCPSender> tcpSenders;

	// Used to validate and assign incoming data
	private HashMap<String, String> ipNameMap;

	private UDPServer udpServer;

	private Thread udpserverThread;

	private TCPServer tcpServer;

	private Thread tcpserverThread;

	private OSCReceiver oscReceiver;

	private OSCUpdateListener updateListener;

	private Control control;

	// Port for UDP, TCP, OSC listen & registration of new Kinects
	private int port;

	public CommunicationHandler(UDPServer udpServer, TCPServer tcpServer, OSCUpdateListener updateListener) {

		ipNameMap = new HashMap<String, String>();

		this.udpServer = udpServer;

		this.tcpServer = tcpServer;
		tcpServer.addListener(this);

		tcpSenders = new HashMap<String, TCPSender>();

		this.updateListener = updateListener;
	}
	
	/**
	 * Sets the control value to the specified control
	 * @param control
	 */
	public void setControl(Control control){

		this.control = control;
	}

	/**
	 * Starts a UDP Thread, a TCP Server Thread and an OSC Receiver
	 * 
	 * @param port
	 *            port for TCP & UDP, OSC is port + 1
	 */
	public void startup() {
		// Create a UDP Listener Thread

		udpServer.setListenPort(port);
		tcpServer.setServerSocket(port);

		// Create a TCP Server Thread
		tcpserverThread = new Thread(tcpServer);
		tcpserverThread.setDaemon(true);
		tcpserverThread.start();

		udpserverThread = new Thread(udpServer);
		udpserverThread.setDaemon(true);
		udpserverThread.start();

		// Create a OSC Receiver Thread for receiving person Data
		oscReceiver = new OSCReceiver(port + 1, this);
	}

	/**
	 * Is called when a new Kinect was recognized, add a valid ip name mapping
	 * to kinect handling
	 */
	public void kinectRecognized(String name, String ip) {

		ipNameMap.put(ip, name);
	}

	/**
	 * Set Port for TPC & UDP Communication
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public void tcpConnectionIncoming(Socket socket) {
		/*
		 * There has to be a valid ip to name mapping due to udp broadcasting.
		 * If not, then the connection comes from somebody else.
		 */
		String ip = socket.getInetAddress().getHostAddress();
		String name = ipNameMap.get(ip);
		if (name == null) {
			System.err.println("Unknown Connection Attempt");
		} else {
			try {
				// Create a new tcp sender instance and add it to the already
				// existing ones
				TCPSender sender = new TCPSender(socket, name);
				tcpSenders.put(name, sender);
				//send new or existing settings
				control.sendSettingsOnTCPConnect(name);
			} catch (IOException e) {
				System.err.println("Error on creating a new Socket for TCP Connection");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Main method to handle all OSC Events. Anazyles the messagetypes of all
	 * incoming messages and creates events from it.
	 */
	public void oscEvent(OscMessage message) {
		// Get Name from IP
		String name = ipNameMap.get(message.address().substring(1));

		String typetag = message.typetag();

		String messageType = message.addrPattern();

		// Switch message handling via Adress Pattern
		if (messageType.equals("personEntered") || messageType.equals("personUpdated")) {
			Person tempPerson = null;
			for (int i = 0; i < typetag.length(); i++) {
				if (typetag.charAt(i) == 's') {
					switch (message.get(i).stringValue()) {
					case "set":
						tempPerson = new Person(message.get(++i).intValue(), message.get(++i).intValue());
						break;
					case "centroid":
						tempPerson.setCentroid(new PVector(message.get(++i).floatValue(), message.get(++i).floatValue(),
								message.get(++i).floatValue()));
						break;
					case "velocity":
						tempPerson.setVelocity(new PVector(message.get(++i).floatValue(), message.get(++i).floatValue(),
								message.get(++i).floatValue()));
						break;
					case "acceleration":
						tempPerson.setAcceleration(new PVector(message.get(++i).floatValue(),
								message.get(++i).floatValue(), message.get(++i).floatValue()));
						break;
					case "center":
						tempPerson.setCenter(new PVector(message.get(++i).floatValue(), message.get(++i).floatValue(),
								message.get(++i).floatValue()));
						break;
					case "contour":
						ArrayList<PVector> contour = new ArrayList<PVector>();
						while (typetag.charAt(++i) != 's') {
							contour.add(new PVector(message.get(i).floatValue(), message.get(++i).floatValue(),
									message.get(++i).floatValue()));
						}
						i--;
						tempPerson.setContour(contour);
						break;
					case "fseq":
						tempPerson.setLastUdate(Long.valueOf(message.get(++i).stringValue()));
						if (messageType.equals("personEntered")) {
							updateListener.personEntered(name, tempPerson);
						} else {
							updateListener.personMoved(name, tempPerson);
						}
						break;
					default:
						System.out.println("Couldn't parse new Person");
						break;
					}
				}
			}
		} else {
			if (messageType.equals("personLeft")) {
				updateListener.personLeft(name, message.get(0).intValue());
			} else {
				if (messageType.equals("triggerzoneUpdate")) {

					String id = null;
					int pointsInsideBox = -1;
					HashMap<Integer, Integer> pointsperPerson = new HashMap<Integer, Integer>();
					for (int i = 0; i < typetag.length(); i++) {

						if (typetag.charAt(i) == 's') {
							if (message.get(i).stringValue().equals("set")) {

								id = message.get(++i).stringValue();
								pointsInsideBox = message.get(++i).intValue();
								i++;
								while (typetag.charAt(i) == 'i') {
									
									pointsperPerson.put(message.get(i++).intValue(), message.get(i++).intValue());
								}
								i--;
							} else {
								if (message.get(i).stringValue().equals("fseq")) {

									updateListener.updateTriggerZone(name, id, pointsInsideBox, pointsperPerson,
											Long.valueOf(message.get(++i).stringValue()));
								}
							}
						}
					}
				}
			}
		}

	}

	/**
	 * Needed for Interface purposes
	 */
	public void oscStatus(OscStatus arg0) {
	}

	/**
	 * Shuts down the communication Handler
	 */
	public void shutdown() {
		try {
			udpServer.interrupt();
			udpserverThread.interrupt();
			try {
				tcpServer.interrupt();
			} catch (IOException e) {
				System.err.println("Couldn't close TCP Socket!");
				e.printStackTrace();
			}
			tcpserverThread.interrupt();
			oscReceiver.interrupt();
		} catch (NullPointerException e) {
			return;
		}

		Iterator<TCPSender> tcpIterator = tcpSenders.values().iterator();
		while (tcpIterator.hasNext()) {
			TCPSender temp = tcpIterator.next();
			try {
				temp.closeSocket();
			} catch (IOException e) {
				System.err.println("Couldn't close TCP Socket!");
				e.printStackTrace();
			}
		}
		tcpSenders.clear();
	}

	/**
	 * Send Settings for kinectName to Server
	 * @param kinectName
	 * @param settings
	 */
	public void sendSettings(String kinectName, Settings settings) {

		try {

			tcpSenders.get(kinectName).sendSettings(settings);
		} catch (IOException e) {
			System.err.println("Error on sending Settings");
			e.printStackTrace();
		}
	}
}
