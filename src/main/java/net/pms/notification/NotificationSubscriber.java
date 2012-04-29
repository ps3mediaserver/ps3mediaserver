package net.pms.notification;

public interface NotificationSubscriber {
	
	void sendMessage(String msg, Object obj);

}
