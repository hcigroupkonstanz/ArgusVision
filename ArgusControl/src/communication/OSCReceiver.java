package communication;

import oscP5.*;

/**
 * Small base class used to route OSC messages to the Communication Handler
 * 
 * @author Moritz Skowronski
 *
 */
public class OSCReceiver {

	private OscP5 oscReceiver;
	
	public OSCReceiver(int listenerPort, OscEventListener listener){
		OscProperties properties = new OscProperties();
		properties.setDatagramSize(65536);
		properties.setListeningPort(listenerPort);
		oscReceiver = new OscP5(this, properties);
		oscReceiver.addListener(listener);
	}

	public void interrupt() {
		oscReceiver.stop();
	}


}
