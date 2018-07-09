package helpers;

/**
 * Enum Class. Holds different useful enums.
 * 
 * @author HCI-User
 *
 */
public class Enum {

	public enum ImageTypes {
		COLOR, DEPTH, BODYTRACK, INFRARED
	}

	public enum OSCPersonMode {
		PERSON, PERSONCONTOURS, NOSEND
	}

	public enum OSCTriggerZoneMode {
		TRIGGERZONE, TRIGGERZONEPERSON, TRIGGERZONECONTOURS, NOSEND
	}

	public enum Streams {
		DEPTH, INFRARED, PERSON, MASK, POINTCLOUD, NOSEND
	}

	public enum DisplayMode {
		Server, Triggerzone
	}
}
