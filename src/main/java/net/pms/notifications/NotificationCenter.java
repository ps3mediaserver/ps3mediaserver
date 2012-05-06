package net.pms.notifications;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class exposes methods to subscribe and unsubscribe {@link NotificationSubscriber} 
 * to be notified of messages posted to the message queue.<br>
 * Get a singleton using the static getInstance method.<br>
 * 
 * @author pw
 */
public class NotificationCenter<T> {
	/**
	 * Holds a map of instances of {@link NotificationCenter} per {@link Class}
	 */
	private static Map<Class<?>, NotificationCenter<?>> notificationCenters = new Hashtable<Class<?>, NotificationCenter<?>>();	
	/**
	 * TODO: figure out how to manage the multiple threads best.
	 */
	private static ExecutorService executorService = Executors.newFixedThreadPool(50);
	
	private List<NotificationSubscriber<T>> subscribers = new ArrayList<NotificationSubscriber<T>>();

	/**
	 * Private constructor. Use getInstance to get a singleton
	 */
	private NotificationCenter() { }

	/**
	 * Get an instance of {@link NotificationCenter} which can subscribe/unsubscribe
	 * {@link NotificationSubscriber} to messages of type c
	 * 
	 * @param c the type of messages the subscriber will receive
	 * @return an instance of {@link NotificationCenter}
	 */
	@SuppressWarnings("unchecked")
	public static <T> NotificationCenter<T> getInstance(Class<T> c) {
		NotificationCenter<?> nc = notificationCenters.get(c);
		if(nc == null) {
			nc = new NotificationCenter<T>();
			notificationCenters.put(c, nc);
		}
		
		return (NotificationCenter<T>) nc;
	}

	/**
	 * Start notifying the subscriber of messages being posted to the queue
	 *
	 * @param subscriber the subscriber to notify
	 */
	public void subscribe(NotificationSubscriber<T> subscriber) {
		if (!subscribers.contains(subscriber)) {
			subscribers.add(subscriber);
		}
	}


	/**
	 * Stop notifying the subscriber of messages being posted to the queue
	 *
	 * @param subscriber the subscriber to remove
	 */
	public void unsubscribe(NotificationSubscriber<T> subscriber) {
		subscribers.remove(subscriber);
	}

	/**
	 * Post a message to the queue<br>
	 * All subscribers will be called asynchronously to avoid slowing down the process if a subscriber is slow
	 *
	 * @param message the message
	 * @param obj the object to post
	 */
	public void post(final T obj) {
			for (final NotificationSubscriber<T> subscriber : subscribers) {
				executorService.execute(new Runnable() {					
					@Override
					public void run() {
						subscriber.onMessage(obj);
					}
				});
			}
	}
}
