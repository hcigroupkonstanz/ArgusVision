package communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import control.Settings;

/**
 * Receives Data via TCP from Server
 * @author Moritz Skowronski
 *
 */
public class TCPReceiver implements Runnable {

	private Socket socket;
	private ObjectInputStream inputStream;
	private boolean listening;
	private boolean available;
	private boolean connectionLost;
	private int counter;
	private Settings settings;

	public void startReceiver(String serverIP, int port) throws UnknownHostException, IOException {
		socket = new Socket(InetAddress.getByName(serverIP), port);
		counter = 0;
		listening = true;
	}

	public void run() {
		while (listening) {
			try {
				inputStream = new ObjectInputStream(socket.getInputStream());
				// Get Settings and tell control that new settings are available
				settings = (Settings) inputStream.readObject();
				counter = 0;
				available = true;
			} catch (ClassNotFoundException e) {
				connectionLost = true;
				listening = false;
				System.err.println("Settings Class not found");
				e.printStackTrace();
			} catch (IOException e) {
				// If there was an IO Exception 5 times, stop the conncetion
				// and conncet anew using UDP Broadcasting
				counter++;
				if (counter == 160) {
					connectionLost = true;
					listening = false;
				}
				System.err.println("Exception reading new settings from inputStream");
				e.printStackTrace();
			}
		}
	}

	/**
	 * True if new Settings are available, false if not
	 * @return
	 */
	public boolean hasNewSettings() {
		return available;
	}

	/**
	 * Returns the new settings and sets available to false
	 * @return
	 */
	public Settings getSettings() {
		available = false;
		return settings;
	}

	/**
	 * Returns true if TCP Receiver is up and listening on a port
	 * @return
	 */
	public boolean isListening() {
		return listening;
	}
	
	/**
	 * Resets the TCP Receiver for restart
	 */
	public void reset(){
		connectionLost = false;
	}

	/**
	 * Set listening variable. Used to stop the server from listening to a port.
	 * @param listening
	 */
	public void setListening(boolean listening) {
		this.listening = listening;
	}

	/**
	 * true if connection is lost
	 * @return
	 */
	public boolean lostConnection() {
		return connectionLost;
	}

}
