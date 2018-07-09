package control;

import communication.CommunicationHandler;
import communication.OSCSender;
import communication.TCPServer;
import communication.UDPServer;
import processing.core.PApplet;
import scene.SceneHandler;
import visualization.KinectImage;

public class Main {

	public static void main(String[] args) {

		TCPServer tcpServer = new TCPServer();

		UDPServer udpServer = new UDPServer();
		SceneHandler sceneHandler = new SceneHandler();
		
		CommunicationHandler communicationHandler = new CommunicationHandler(udpServer, tcpServer, sceneHandler);

		Control control = new Control(communicationHandler, sceneHandler);
		
		communicationHandler.setControl(control);

		String[] runString = { "run" };
		Display display = new Display(control);
		OSCSender.p = display;
		KinectImage imageStream = new KinectImage(display);
		display.setImageFromKinect(imageStream);
		udpServer.addImageListener(imageStream);

		udpServer.addListener(display);

		// Start the Display Thread and Processing
		PApplet.runSketch(runString, display);
	}
}
