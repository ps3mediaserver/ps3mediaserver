package net.pms.notifications;

/**
 * 
 * @author pw
 *
 * @param <T> The type of the queue for the subscription
 */
public interface NotificationSubscriber<T> {
	
	/**
	 * Send a message to the subscriber
	 * @param obj the object of type T
	 */
	void onMessage(final T obj);

}
