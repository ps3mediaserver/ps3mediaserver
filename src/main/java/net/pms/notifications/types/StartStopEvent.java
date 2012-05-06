package net.pms.notifications.types;

import net.pms.dlna.DLNAResource;

public class StartStopEvent {
	private DLNAResource resource;
	private Event event;
	
	public StartStopEvent(DLNAResource resource, Event event) {
		this.resource = resource;
		this.event = event;
	}
	
	public DLNAResource getDlnaResource() {
		return resource;
	}

	public Event getEvent() {
		return event;
	}

	public enum Event {
		Start,
		Stop
	}
}
