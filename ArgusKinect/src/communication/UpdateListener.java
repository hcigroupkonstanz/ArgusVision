package communication;

import scene.AbstractTriggerZone;
import scene.Person;

/**
 * Used for transmitting person Info via OSC
 * @author Moritz Skowronski
 *
 */
public interface UpdateListener {
	
	/**
	 * Called when a Person has entered the Scene
	 * @param person
	 */
	public void personEntered(Person person);
	
	/**
	 * Called when a Person has left the Scene
	 * @param id the id of the Person
	 */
	public void personLeft(int id);
	
	/**
	 * Called when a preexisting Person has moved in the Scene
	 * @param person
	 */
	public void personMoved(Person person);
	
	/**
	 * Called when triggerzones have been updated
	 * @param triggerzones
	 */
	public void updateTriggerzones(AbstractTriggerZone[] triggerzones);
}
