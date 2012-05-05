package net.pms.notification;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * This class exposes methods to subscribe and unsubscribe {@link NotificationSubscriber} 
 * to be notified of messages posted to certain message queues.
 */
public class NotificationCenter {
	private static Map<String, List<NotificationSubscriber>> subscribersPerQueue = new Hashtable<String, List<NotificationSubscriber>>(); //key=queue name, value=list of subscribers

	/**
	 * Private constructor which will never be accessed. All accessible methods are static
	 */
	private NotificationCenter() {

	}

	/**
	 * Start notifying the subscriber of messages being posted to the queue named queueName
	 *
	 * @param queueName the name of the queue
	 * @param subscriber the subscriber to notify
	 */
	public static void subscribe(String queueName, NotificationSubscriber subscriber) {
		List<NotificationSubscriber> subscribers = subscribersPerQueue.get(queueName);
		if (subscribers == null) {
			subscribers = new ArrayList<NotificationSubscriber>();
			subscribersPerQueue.put(queueName, subscribers);
		}

		if (!subscribers.contains(subscriber)) {
			subscribers.add(subscriber);
		}
	}


	/**
	 * Stop notifying the subscriber of messages being posted to the queue named queueName
	 *
	 * @param queueName the name of the queue
	 * @param subscriber the subscriber to notify
	 */
	public static void unsubscribe(String queueName, NotificationSubscriber subscriber) {
		List<NotificationSubscriber> subscribers = subscribersPerQueue.get(queueName);
		if (subscribers != null) {
			subscribers.remove(subscriber);
		}
	}

	/**
	 * Post a message to the queue named queueName
	 *
	 * @param queueName the queue name
	 * @param message the message
	 * @param obj the obj
	 */
	public static void post(String queueName, String message, Object obj) {
		List<NotificationSubscriber> subscribers = subscribersPerQueue.get(queueName);
		if (subscribers != null) {
			for (NotificationSubscriber subscriber : subscribers) {
				subscriber.sendMessage(message, obj);
			}
		}
	}
}
