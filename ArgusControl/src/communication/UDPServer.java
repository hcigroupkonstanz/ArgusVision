package communication;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import visualization.ImageUpdateListener;

/**
 * This class listens to all incoming UDP Broadcasts, checks whether they are
 * coming from a valid Kinect Source and returns its IP and 3 valid ports for
 * UDP, TCP and OSC Communication
 * 
 * @author Moritz Skowronski
 *
 */
public class UDPServer implements Runnable {

	private DatagramSocket socket;

	private DatagramPacket sendPacket;

	private DataInputStream dInputStream;
	private ByteArrayInputStream bInputStream;

	private int listenPort;

	private boolean listening;

	// start of answer of Server to client
	private static final String ANSWER = "DISCOVER_SERVER_RESPONSE/";

	private static final String MESSAGESTART = "DISCOVER_SERVER_REQUEST/";

	// All classes that need to know about new Kinects
	private ArrayList<KinectConnectListener> listeners;

	private ImageUpdateListener imageListener;

	public UDPServer() {

		listeners = new ArrayList<KinectConnectListener>();
	}

	public UDPServer(int listenPort) {

		listeners = new ArrayList<KinectConnectListener>();
		setListenPort(listenPort);
		listening = true;
	}

	public void run() {
		try {

			System.out.println("UDP Server listening on Port " + listenPort);

			socket = new DatagramSocket(listenPort);
			socket.setBroadcast(true);

			while (listening) {

				// Receive a packet
				byte[] recvBuf = new byte[65536];
				DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
				socket.receive(packet);
				recvBuf = packet.getData();

				bInputStream = new ByteArrayInputStream(recvBuf);
				dInputStream = new DataInputStream(bInputStream);
				dInputStream.mark(recvBuf.length);

				byte[] input = new byte[65536];
				dInputStream.readFully(input);
				// See if the packet holds the right command
				String message = new String(input).trim();
				// Connection Handling
				if (message.startsWith("DISCOVER_SERVER_REQUEST/")) {
					// Get Kinect Name
					String kinectName = message.substring(MESSAGESTART.length());
					String answer = ANSWER + (listenPort + 1);
					byte[] sendData = answer.getBytes();

					// Send a response
					sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
					socket.send(sendPacket);
					
					// Notify all relevant classes about the new Kinect
					notifyAboutNewKinect(kinectName, packet.getAddress().getHostAddress());
				}
				// Image Handling
				else {
					dInputStream.reset();
					switch (dInputStream.readInt()) {
					case 0:
						notifyImageUpdate(ImageIO.read(dInputStream));
						break;
					case 1:
						notifyPointCloudUpdate(ImageIO.read(dInputStream));
						break;
					default:
						System.out.println("Unknown UDP Input");
						break;
					}
				}

			}

		} catch (SocketException e) {
			listening = false;
			System.err.println("Error on Socket, Closed?");
			e.printStackTrace();
		} catch (IOException e) {
			listening = false;
			System.err.println("Error on IO Handling for UDP Broadcast Server");
			e.printStackTrace();
		}

	}

	/**
	 * Interrupts this Thread
	 */
	public void interrupt() {
		System.out.println("UDP Server shutting down");
		socket.close();
		listening = false;
	}

	/**
	 * Set UDP Listen Port
	 * 
	 * @param port
	 */
	public void setListenPort(int port) {
		this.listenPort = port;
		listening = true;
	}

	/**
	 * Gets the current status of the Thread, true if running or ready to run
	 * 
	 * @return
	 */
	public boolean getStatus() {
		return listening;
	}

	/*----------------Listener Methods--------------*/

	/**
	 * Adds a new Kinect listener to the objects that are notified when theres a
	 * new Kincet
	 * 
	 * @param listener
	 *            the new Listener
	 */
	public void addListener(KinectConnectListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Adds a new Listener to the Kinect Image Listener
	 * 
	 * @param listener
	 */
	public void addImageListener(ImageUpdateListener listener) {
		this.imageListener = listener;
	}

	/**
	 * Notifies the listener about Point Cloud Update
	 * 
	 * @param pointCloudData
	 */
	public void notifyPointCloudUpdate(BufferedImage pointCloudData) {
		imageListener.updatePointCloud(pointCloudData);
	}

	/**
	 * Notifies the listener about image Update
	 * 
	 * @param image
	 */
	public void notifyImageUpdate(BufferedImage image) {
		imageListener.updateImage(image);
	}

	public void notifyAboutNewKinect(String name, String ip) {
		for (KinectConnectListener kinectConnectListener : listeners) {
			kinectConnectListener.kinectRecognized(name, ip);
		}
	}
}
