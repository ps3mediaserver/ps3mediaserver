package net.pms.notifications;

/**
 * Subscribe to messages being posted to the {@link NotificationCenter} with
 * this interface
 * 
 * @author pw
 * 
 * @param <T> The type of the queue to subscribe to
 */
public interface NotificationSubscriber<T> {

	/**
	 * Send a message to the subscriber
	 * 
	 * @param obj the object of type T
	 */
	void onMessage(final T obj);

}
