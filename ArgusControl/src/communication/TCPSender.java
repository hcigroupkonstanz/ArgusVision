package communication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import control.Settings;

/**
 * Used to send Settings to a Kinect Broadcaster
 * 
 * @author Moritz Skowronski
 *
 */
public class TCPSender {

	private String name;

	private Socket socket;

	private ObjectOutputStream outStream;

	public TCPSender(Socket socket, String name) throws IOException {

		this.socket = socket;

		this.name = name;
	}

	/**
	 * Sends the settings via TCP
	 * 
	 * @param settings
	 * @throws IOException
	 */
	public void sendSettings(Settings settings) throws IOException {
		outStream = new ObjectOutputStream(socket.getOutputStream());
		outStream.writeObject(settings);
	}

	/**
	 * Closes the socket
	 * 
	 * @throws IOException
	 */
	public void closeSocket() throws IOException {
		outStream.close();
		socket.close();
	}

	/**
	 * Get the Name of the Kinect the Socket is associated with
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
}
