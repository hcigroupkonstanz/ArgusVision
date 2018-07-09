package helpers;

import org.apache.commons.lang3.SystemUtils;

import helpers.Enum.OS;

/**
 * This Class provides information about which OS is used to run the
 * application. This is useful to decide which library for Kinect Tracking
 * should be used. NOTE: Skeleton Tracking only works on Windows for now
 * 
 * @author Moritz Skowronski
 *
 */
public class OSChooser {
	/**
	 * Returns the used OS type
	 * 
	 * @return either Linux, Windows, Mac or unkown 
	 */
	public static OS getLibrary() {
		if (SystemUtils.IS_OS_LINUX) {
			return OS.LINUX;
		}
		if (SystemUtils.IS_OS_WINDOWS) {
			return OS.WINDOWS;
		}
		if(SystemUtils.IS_OS_MAC_OSX){
			return OS.MAC;
		}
		return OS.UNKNOWN;
	}
}
