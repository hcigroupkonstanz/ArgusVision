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

	public enum OS {
		WINDOWS, MAC, LINUX, UNKNOWN
	}

	public enum PointCloudFlag {
		FLAG_OFF_THRESHOLD, FLAG_BACKGROUND, FLAG_IDLE, FLAG_QUEUED, FLAG_PROCESSED
	}

	public enum Streams {
		MASK, DEPTH, INFRARED, PERSON, POINTCLOUD, NOSEND
	}
}
