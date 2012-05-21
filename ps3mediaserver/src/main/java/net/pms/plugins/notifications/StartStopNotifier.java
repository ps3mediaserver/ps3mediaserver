package net.pms.plugins.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.notifications.NotificationCenter;
import net.pms.notifications.NotificationSubscriber;
import net.pms.notifications.types.StartStopEvent;
import net.pms.plugins.PluginsFactory;
import net.pms.plugins.StartStopListener;

/**
 * Listens to the {@link StartStopEvent} notification queue and notifies {@link StartStopListener} 
 * asynchronously when getting a notification
 * 
 * @author pw
 *
 */
public class StartStopNotifier {
	private static final Logger log = LoggerFactory.getLogger(StartStopNotifier.class);
	private static int nbThreads = 0;
	
	/**
	 * Starts listening for start stop notifications
	 */
	public static void initialize() {		
		NotificationCenter.getInstance(StartStopEvent.class).subscribe(new NotificationSubscriber<StartStopEvent>() {			
			@Override
			public void onMessage(final StartStopEvent obj) {
				for (final StartStopListener plugin : PluginsFactory.getStartStopListeners()) {
					Runnable fireStartStopEvent = new Runnable() {
						@Override
						public void run() {
							try {
								switch(obj.getEvent()) {
								case Start:
									plugin.nowPlaying(obj.getDlnaResource().getMedia(), obj.getDlnaResource());
									break;
								case Stop:
									plugin.donePlaying(obj.getDlnaResource().getMedia(), obj.getDlnaResource());
									break;
								}
							} catch (Throwable t) {
								log.error(String.format("Notification of startPlaying event failed for StartStopListener %s", plugin.getClass()), t);
							}
						}
					};
					new Thread(fireStartStopEvent, "NotifyStartStop" + nbThreads++).start();
				}
			}
		});
	}
}
