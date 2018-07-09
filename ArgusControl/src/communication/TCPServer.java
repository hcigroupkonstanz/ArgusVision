package communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server that listens on a port for incoming connections, accepts these
 * connections and creates new Threads
 * 
 * @author Moritz Skowronski
 *
 */
public class TCPServer implements Runnable {

	private int listenPort;

	private ServerSocket serverSocket;

	private boolean listening;

	private TCPConnectListener listener;

	/**
	 * Listens for incoming TCP Requests
	 */
	public void run() {

		try {
			serverSocket = new ServerSocket(listenPort);
			System.out.println("TCP Server listening on Port " + listenPort);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (serverSocket == null) {
			listening = false;
		}

		while (listening) {
			try {
				notifyListener(serverSocket.accept());
			} catch (IOException e) {
				System.err.println("Couldn't process incoming TCP Request");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set listen port, needed for starting the thread
	 * 
	 * @param port
	 * @throws IOException
	 */
	public void setServerSocket(int port) {
		listenPort = port;
		listening = true;
	}

	/**
	 * Interrupts the Thread, if it is running
	 * 
	 * @throws IOException
	 */
	public void interrupt() throws IOException {
		serverSocket.close();
		System.out.println("TCP Server shutting down");
		listening = false;
	}

	/**
	 * Adds a class as listener
	 * 
	 * @param listener
	 */
	public void addListener(TCPConnectListener listener) {
		this.listener = listener;
	}

	/**
	 * Notifies the listener of a new Socket
	 * 
	 * @param socket
	 */
	public void notifyListener(Socket socket) {
		System.out.println("New Kinect connecting with IP: " + socket.getInetAddress().getHostAddress());
		listener.tcpConnectionIncoming(socket);
	}
}
