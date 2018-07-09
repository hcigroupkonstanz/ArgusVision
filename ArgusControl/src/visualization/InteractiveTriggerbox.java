package visualization;

import processing.core.PApplet;
import processing.core.PGraphics;
import remixlab.dandelion.geom.Quat;
import remixlab.dandelion.geom.Rotation;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.MouseAgent;
import remixlab.proscene.Scene;
import scene.TriggerBox;

/**
 * A 3D Visualiation of a Triggerbox
 * 
 * @author Moritz Skowronski
 *
 */
public class InteractiveTriggerbox {

	private TriggerBox triggerbox;
	private Scene scene;
	private InteractiveFrame frame;

	public InteractiveTriggerbox(TriggerBox box, Scene scene) {
		triggerbox = box;
		this.scene = scene;

		frame = new InteractiveFrame(this.scene);

		// This is used to turn off scaling with mousewheel
		frame.removeMotionBinding(MouseAgent.WHEEL_ID);
		setPosition();
		setOrientation();
	}

	/**
	 * Draws the model to the screen
	 * 
	 * @param isSelected
	 *            if true, make it highlighted
	 */
	public void draw(boolean isSelected, int color) {

		if (scene.motionAgent().trackedGrabber() != frame) {
			setPosition();
			setOrientation();
		}

		PGraphics pg = scene.pg();

		pg.pushStyle();
		scene.pushModelView();
		scene.applyTransformation(frame);

		if (isSelected)
			pg.fill(color, PApplet.map(triggerbox.getPointsInsideBox(), 0, 1000, 150, 255));
		else
			pg.fill(triggerbox.getColor(), PApplet.map(triggerbox.getPointsInsideBox(), 0, 1000, 150, 255));

		pg.box(triggerbox.getSize()[0], triggerbox.getSize()[1], triggerbox.getSize()[2]);
		scene.popModelView();
		pg.popStyle();

		if (scene.motionAgent().trackedGrabber() == frame) {
			setPosition(frame.translation());
			setRotation(frame.orientation());
		}
	}

	private void setRotation(Rotation rotation) {

		Quat rotationQuat = (Quat) rotation;
		Vec eulerAngles = rotationQuat.eulerAngles();
		triggerbox.setRotateX(eulerAngles.x());
		triggerbox.setRotateY(eulerAngles.y());
		triggerbox.setRotateZ(eulerAngles.z());
	}

	/**
	 * Sets the position of the frame according to the position of the
	 * Triggerbox
	 */
	private void setPosition() {

		frame.setPosition(-triggerbox.getX(), triggerbox.getY(), triggerbox.getZ());
	}

	/**
	 * Sets the Position of the Triggerbox according to the Translation that
	 * occured for the frame
	 * 
	 * @param translation
	 */
	private void setPosition(Vec translation) {

		triggerbox.setX(-translation.x());
		triggerbox.setY(translation.y());
		triggerbox.setZ(translation.z());
	}

	/**
	 * Sets the Orientation of the Frame to the orientation of the Triggerbox
	 */
	private void setOrientation() {

		Quat rotation = new Quat(triggerbox.getRotateX(), triggerbox.getRotateY(), triggerbox.getRotateZ());

		frame.setOrientation(rotation);
	}

	/**
	 * Returns the Interactive Frame of the Triggerbox
	 * 
	 * @return
	 */
	public InteractiveFrame getInteractiveFrame() {

		return frame;
	}

	/**
	 * Returns the ID of the Triggerbox
	 * 
	 * @return ID of Triggerbox
	 */
	public String getID() {

		return triggerbox.getID();
	}

}