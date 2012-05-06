package net.pms.notifications.types;

public class PluginEvent {
	private Event event;
	
	public PluginEvent(Event event) {
		this.event = event;
	}
	
	public Event getEvent() {
		return event;
	}

	public enum Event {
		PluginsLoaded,
	}	
}
