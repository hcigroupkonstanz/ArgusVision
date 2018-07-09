package helpers;

import helpers.Enum.PointCloudFlag;
import processing.core.PVector;

/**
 * TODO used for 3d Tracking, not yet implemented
 * @author Moritz Skowronski
 *
 */
public class CloudPoint3D {
	
	private int x;
	private int y;
	private float z;
	private PVector pos;
	public PointCloudFlag flag;
	
	public CloudPoint3D(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
		
		pos = depthToPointCloudPos(this.x, this.y, this.z);
	}
	
	public CloudPoint3D(){
	}
	
	public static PVector depthToPointCloudPos(int x, int y, float depthValue) {
		PVector point = new PVector();
		point.z = (depthValue) * 0.001f;
		point.x = (x - CameraParams.cx) * point.z / CameraParams.fx;
		point.y = (y - CameraParams.cy) * point.z / CameraParams.fy;
		return point;
	}

	public PVector getPos() {
		return pos;
	}
	
}
