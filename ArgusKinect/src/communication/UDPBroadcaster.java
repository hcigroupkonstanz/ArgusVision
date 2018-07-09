package communication;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

/**
 * Sends a UDP Broadcast to all available IPs in the Network. Network has to support broadcasting!!
 * @author Moritz Skowronski
 *
 */
public class UDPBroadcaster implements Runnable {

	private DatagramSocket socket;
	private String serverIP;
	private int tcpPort;
	private int oscPort;
	private int udpPort;
	private int broadcastPort;
	private volatile boolean running;
	private boolean portSet;
	private byte[] sendData;
	private final String anticipatedResponse = "DISCOVER_SERVER_RESPONSE/";

	/**
	 * Constructs a Client that handles UDP Communication using a Name as
	 * Identifier
	 * 
	 * @param name
	 *            Identifier
	 */
	public UDPBroadcaster() {
		running = true;
		sendData = ("DISCOVER_SERVER_REQUEST/Default").getBytes();
		broadcastPort = 8888;
	}

	/**
	 * Finds the Server using UDP Broadcasting
	 */
	public void run() {
		try {
			// Open a random port to send the package
			socket = new DatagramSocket();
			socket.setBroadcast(true);
			// Send broadcast every 5 seconds until a server responds
			socket.setSoTimeout(5000);

			while (running) {
				// Try the 255.255.255.255 first
				try {
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
							InetAddress.getByName("255.255.255.255"), broadcastPort);
					socket.send(sendPacket);
					System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
				} catch (Exception e) {
					System.err.println("Sending Packet failed");
					e.printStackTrace();
				}

				// Broadcast the message over all the network interfaces
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

					if (networkInterface.isLoopback() || !networkInterface.isUp()) {
						continue; // Don't want to broadcast to the loopback
									// interface
					}

					for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
						InetAddress broadcast = interfaceAddress.getBroadcast();
						if (broadcast == null) {
							continue;
						}

						// Send the broadcast package!
						try {
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast,
									broadcastPort);
							socket.send(sendPacket);
						} catch (Exception e) {
							System.err.println("Sending Packet failed");
							e.printStackTrace();
						}

						System.out.println(getClass().getName() + ">>> Request packet sent to: "
								+ broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
					}
				}

				System.out.println(getClass().getName()
						+ ">>> Done looping over all network interfaces. Now waiting for a reply!");

				// Wait for a response
				byte[] recvBuf = new byte[65536];
				DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
				try {
					socket.receive(receivePacket);

					System.out.println(getClass().getName() + ">>> Broadcast response from server: "
							+ receivePacket.getAddress().getHostAddress());

					// Check if the message is correct
					String message = new String(receivePacket.getData()).trim();
					if (message.startsWith(anticipatedResponse)) {
						serverIP = receivePacket.getAddress().getHostAddress();
						// Using length is ok, because of the / after the
						// response and before the port
						String port = message.substring(anticipatedResponse.length());
						tcpPort = receivePacket.getPort();
						udpPort = tcpPort;
						oscPort = Integer.parseInt(port);
						portSet = true;
					}
					running = false;
				} catch (SocketTimeoutException e) {
					System.out.println("No Server reachable");
				}
			}
			// Close the port!
			socket.close();
		} catch (IOException ex) {
			System.err.println("UDP Broadcasting failed");
			ex.printStackTrace();
		}
	}

	/**
	 * Returns if ports are set, which means that tcp and udp communication can start
	 * @return
	 */
	public boolean isPortSet() {
		return portSet;
	}
	
	/**
	 * Resets the UDP Broadcaster
	 */
	public void reset(){
		portSet = false;
		running = true;
	}

	/**
	 * use this to interrupt the broadcaster
	 * @param running
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Sets the Client Name. Used by the UI
	 * @param name
	 */
	public void setClientName(String name) {
		sendData = ("DISCOVER_SERVER_REQUEST/" + name).getBytes();
	}

	/**
	 * Sets the Broadcast Port. Used by the UI
	 * @param broadcastPort
	 */
	public void setBroadcastPort(int broadcastPort) {
		this.broadcastPort = broadcastPort;
	}

	/**
	 * Returns TCP Port received from Server
	 * @return
	 */
	public int getTCPPort() {
		return tcpPort;
	}

	/**
	 * Returns UDP Port received from Server
	 * @return
	 */
	public int getUDPPort() {
		return udpPort;
	}

	/**
	 * Received OSC Port received from Server
	 * @return
	 */
	public int getOSCPort() {
		return oscPort;
	}

	/**
	 * Returns the IP of the Server
	 * @return
	 */
	public String getServerIP() {
		return serverIP;
	}

}
