package net.pms.notifications;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class exposes methods to subscribe and unsubscribe {@link NotificationSubscriber} 
 * to be notified of messages posted to the message queue.<br>
 * Get a singleton using the static getInstance method.<br>
 * 
 * @author pw
 */
public class NotificationCenter<T> {
	private static final Logger log = LoggerFactory.getLogger(NotificationCenter.class);
	
	/**
	 * Holds a map of instances of {@link NotificationCenter} per {@link Class}
	 */
	private static Map<Class<?>, NotificationCenter<?>> notificationCenters = new Hashtable<Class<?>, NotificationCenter<?>>();
	
	/**
	 * TODO: figure out how to manage the multiple threads best.
	 */
	private static ExecutorService executorService = Executors.newFixedThreadPool(50);
	
	/**
	 * The subscribers to notify when a message is being posted to the queue
	 */
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
		NotificationCenter<?> nc = null;
		synchronized (notificationCenters) {
			nc = notificationCenters.get(c);
			if(nc == null) {
				nc = new NotificationCenter<T>();
				notificationCenters.put(c, nc);
			}			
		}
		
		return (NotificationCenter<T>) nc;
	}
	
	/**
	 * Gets an array of classes for which a notification queue has been created
	 * 
	 * @return an array of classes for which a notification que has been created
	 */
	public static Class<?>[] getExistingMessageQueues() {
		Class<?>[] res = new Class<?>[0];
		synchronized (notificationCenters) {
			res = notificationCenters.keySet().toArray(new Class<?>[notificationCenters.size()]);			
		}
		return res;
	}

	/**
	 * Start notifying the subscriber of messages being posted to the queue
	 *
	 * @param subscriber the subscriber to notify
	 */
	public void subscribe(NotificationSubscriber<T> subscriber) {
		synchronized (subscribers) {
			if (!subscribers.contains(subscriber)) {
				subscribers.add(subscriber);
			}
		}
	}

	/**
	 * Stop notifying the subscriber of messages being posted to the queue
	 *
	 * @param subscriber the subscriber to remove
	 */
	public void unsubscribe(NotificationSubscriber<T> subscriber) {
		synchronized (subscribers) {
			subscribers.remove(subscriber);
		}
	}

	/**
	 * Post a message to the queue<br>
	 * All subscribers will be called asynchronously to avoid slowing down the process if a subscriber is slow
	 *
	 * @param message the message
	 * @param obj the object to post
	 */
	public void post(final T obj) {
		log.debug(String.format("Posting %s to queue of type %s", obj.toString(), obj.getClass().getSimpleName()));
		synchronized (subscribers) {
			for (final NotificationSubscriber<T> subscriber : subscribers) {
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						Thread.currentThread().setName("Notify" + obj.getClass().getSimpleName());
						subscriber.onMessage(obj);
					}
				});
			}
		}
	}
}
