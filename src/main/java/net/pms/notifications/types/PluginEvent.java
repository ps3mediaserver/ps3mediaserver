package net.pms.notifications.types;

/**
 * Event raised when plugins change
 * 
 * @author pw
 *
 */
public class PluginEvent {
	private Event event;
	
	/**
	 * Constructor
	 * @param event the type of event to raise
	 */
	public PluginEvent(Event event) {
		this.event = event;
	}
	
	/**
	 * Gets the event
	 * @return the event
	 */
	public Event getEvent() {
		return event;
	}
	
	@Override
	public String toString() {
		return String.format("Event=%s", getEvent());
	}

	/**
	 * The type of event raised by the PluginEvent
	 * 
	 * @author pw
	 *
	 */
	public enum Event {
		PluginsLoaded,
	}
}
