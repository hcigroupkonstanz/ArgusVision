package communication;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;

import helpers.Enum.Streams;
import processing.core.PImage;

/**
 * Streams UDP Images and Point Cloud Data to the Server
 * 
 * @author Moritz Skowronski
 *
 */
public class UDPStreamer {

	private DatagramSocket socket;
	private String serverIP;
	private int port;
	private boolean running;

	public UDPStreamer() throws SocketException {
		socket = new DatagramSocket();
		socket.setSendBufferSize(65536);
	}

	/**
	 * Starts the streamer and sets the important variables for the Server
	 * 
	 * @param serverIP
	 * @param port
	 */
	public void startStreamer(String serverIP, int port) {
		this.serverIP = serverIP;
		this.port = port;
		running = true;
	}

	/**
	 * Sends the image as compressed JPG to the Server or the Pointcloud as
	 * altered, uncompressed PNG
	 * 
	 * @param image
	 */
	public void stream(PImage image, Streams mode) {
		
		// We need a buffered image to do the JPG encoding
		BufferedImage bimg = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB);

		// Transfer pixels from localFrame to the BufferedImage
		image.loadPixels();
		bimg.setRGB(0, 0, image.width, image.height, image.pixels, 0, image.width);

		// Need these output streams to get image as bytes for UDP communication
		ByteArrayOutputStream baStream = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baStream);
		DataOutputStream dataOutputStream = new DataOutputStream(baStream);

		// Turn the BufferedImage into a JPG or Point Cloud into PNG
		try {
			if (mode != Streams.POINTCLOUD) {
				dataOutputStream.writeInt(0);
				ImageIO.write(bimg, "jpg", bos);
			} else {
				dataOutputStream.writeInt(1);
				ImageIO.write(bimg, "PNG", bos);
			}
		} catch (IOException e) {
			System.out.println("Error compressing to jpg");
			e.printStackTrace();
		}

		// Transform to byteArrays
		byte[] packet = baStream.toByteArray();

		// Send JPEG data as a datagram
		try {
			socket.send(new DatagramPacket(packet, packet.length, InetAddress.getByName(serverIP), port));
		} catch (Exception e) {
			System.err.println("Sending image failed");
			System.err.println("Packetsize " + packet.length);
			e.printStackTrace();
		}
	}

	/**
	 * Returns the State of the UDP Streamer
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Turns int into bytes
	 * 
	 * @param x
	 *            the variable
	 * @param n
	 *            the number of bytes
	 * @return
	 */
	public static byte[] intToBytes(int x, int n) {
		byte[] bytes = new byte[n];
		for (int i = 0; i < n; i++, x >>>= 8)
			bytes[i] = (byte) (x & 0xFF);
		return bytes;
	}

	/**
	 * Stop the UDP Streamer
	 */
	public void reset() {
		running = false;
	}

}
