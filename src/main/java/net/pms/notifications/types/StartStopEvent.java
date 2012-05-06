package net.pms.notifications.types;

import net.pms.dlna.DLNAResource;

/**
 * Event raised when a file starts or stops being played
 * 
 * @author pw
 *
 */
public class StartStopEvent {
	private DLNAResource resource;
	private Event event;
	
	/**
	 * Get a new instance of StartStopEvent
	 * @param resource the resource being played
	 * @param event start or stop
	 */
	public StartStopEvent(DLNAResource resource, Event event) {
		this.resource = resource;
		this.event = event;
	}
	
	/**
	 * Gets the DLNA resource
	 * @return the DLNA resource
	 */
	public DLNAResource getDlnaResource() {
		return resource;
	}

	/**
	 * Gets the event (start or stop)
	 * @return the event
	 */
	public Event getEvent() {
		return event;
	}
	
	@Override
	public String toString() {
		return String.format("Resource=%s, Event=%s", getDlnaResource(), getEvent());
	}

	/**
	 * The type of event raised by the StartStopEvent
	 * 
	 * @author pw
	 *
	 */
	public enum Event {
		Start,
		Stop
	}
}
