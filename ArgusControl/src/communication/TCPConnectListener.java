package communication;

import java.net.Socket;

/**
 * Small Interface for parsing new TCP connections to the Communication Handler
 * @author Moritz Skowronski
 *
 */
public interface TCPConnectListener {

	/**
	 * Is called when a new TCP connection is incoming
	 * @param socket
	 */
	public void tcpConnectionIncoming(Socket socket);
	
}
