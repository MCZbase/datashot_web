/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.mcz.imagecapture.jsfclasses;

import edu.harvard.mcz.imagecapture.ejb.MessageBean;
import edu.harvard.mcz.imagecapture.ejb.UsersFacadeLocal;

import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.bean.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

/** JSF Managed bean for interaction with the Chat and server messaging system.
 *
 * @author mole
 */
//@Named("chatController")
@ManagedBean
@SessionScoped
public class ChatController implements Serializable {

	private final static Logger logger = Logger.getLogger(ChatController.class.getName());

	private static final long serialVersionUID = 8413534072633540582L;

	@Resource(name= "jms/InsectChatTopic", type=javax.jms.Topic.class, mappedName = "jms/InsectChatTopic")
	private Topic insectChatTopic;
	@Resource(mappedName = "jms/InsectChatTopicFactory")
	private ConnectionFactory insectChatTopicFactory;

	@EJB(beanName="messageBean")
	private MessageBean messageBean;
	@EJB(beanName="usersFacade")
	private UsersFacadeLocal usersFacade;

	private String message = "";
	private String username = "";
	private boolean loginRecorded = false;

	public ChatController() {
		logger.log(Level.INFO, "Instantiating new ChatController");
		storeLogin();
	}

	/**Looks up the full name for the user from the session remote user, stores
	 * it, and writes the login to the chat.
	 *
	 * As this is a session scoped bean, this method can be invoked when the
	 * ChatController is initialized to record the login of a user.  This will be reasonably
	 * close to the actual login time if the chat controller is initialzed on the
	 * main page for the application.
	 *
	 */
	private void storeLogin() {
		logger.log(Level.INFO, "Called storeLogin()");
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (null != externalContext) {
			String remoteUser = externalContext.getRemoteUser();
			if (remoteUser != null) {
				logger.log(Level.INFO, remoteUser);
				if (usersFacade != null) {
					// on initialization, ejb facade seems unavailable
		            logger.log(Level.INFO, "usersFacade is not null.");
					if (usersFacade.findByName(remoteUser) != null) {
						username = usersFacade.findByName(remoteUser).getFullname();
		                logger.log(Level.INFO, "Fullname:" + username);
						//messageBean.addMessage(username, " is online.");
						messageBean.incrementUserCount();
						loginRecorded = true;
						try {
							sendJMSMessageToInsectChatTopic(username + " is online.", "System (cc)");
						} catch (JMSException ex) {
							logger.log(Level.SEVERE, null, ex);
						}
					} else {
		                logger.log(Level.INFO, "usersFacade.findByName(" + remoteUser + ") is null.");
					}
				} else {
		            logger.log(Level.INFO, "usersFacade is null.");
				}

			}
		}
		logger.log(Level.INFO, "Username:  " + username);
	}

	/** If not already done in creation of the ChatController bean, registers
	 * that a user has logged in, storing the name of the user and writing
	 * the login event to the chat system.
	 *
	 */
	private void registerLogin() {
		if (!loginRecorded) {
			// try to find out the username
			storeLogin();
		}
	}

	/**Notifies the chat system that the user for the current session has logged out.
	 *
	 */
	public void registerLogout() {
		if (!loginRecorded) {
			// try to find out the username
			storeLogin();
		}
		logger.log(Level.INFO, "Called registerLogout() for [" + username + "]");
		//messageBean.addMessage(username, " has gone offline.");
		messageBean.decrementUserCount();
		try {
			sendJMSMessageToInsectChatTopic(username + " has gone offline.", "System (cc)");
		} catch (JMSException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}

	/** Sends the value of ChatController.message to the chat system from
	 * the current user for this session.
	 */
	public void send() {
		logger.log(Level.INFO, "Called send().");
		send(null);
	}

	public void send(ActionEvent event) {
		logger.log(Level.INFO, "Called send() for [" + username + "]");
		if (!loginRecorded) {
			// try to find out the username
			storeLogin();
		}
		logger.log(Level.INFO, "Chat: " + username + ":" + message);
		try {
			EventBus eventBus = EventBusFactory.getDefault().eventBus();
			eventBus.publish("/chat", new FacesMessage(username, username + ": " + message));
			//PushContext context = PushContextFactory.getDefault().getPushContext();
			//context.push("chat", username + ": " + message);
		} catch (Exception e) {
			logger.log(Level.FINE, e.getMessage());
		}
		//messageBean.addMessage(username, message);
		try {
			sendJMSMessageToInsectChatTopic(message, username);
		} catch (JMSException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
		message = null;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**Get the current list of messages from the chat system as a single string.
	 *
	 * @return the current list of chat messages.
	 */
	public String getMessages() {
		if (!loginRecorded) {
			storeLogin();
		}
		return messageBean.getMessages();
	}

	public String getTruncatedMessages() {
		String truncated = messageBean.getMessages();
		int end = truncated.length();
		if (end > 100) {
			int target = truncated.indexOf("/p>");
			truncated = truncated.substring(0, target);
			truncated = truncated.replace("<", "");
		}
		return truncated;
	}

    public boolean isLatestFromServer() {
		return messageBean.isLatestFromServer();
	}

	public String getStyleForChatHead() {
		String returnValue = " ";
		if (isLatestFromServer()) {
			returnValue = " border: solid red; border-width: thick; ";
		}
		return returnValue;
	}

	public int getOnlineUserCount() {
		registerLogin();
		return messageBean.getUserCount();
	}

	public int getMessageCount() {
		return messageBean.getMessageCount();
	}

	private Message createJMSMessageForjmsInsectChatTopic(Session session, Object messageData, String originator) throws JMSException {
		TextMessage tm = session.createTextMessage();
		tm.setStringProperty("Originator", originator);
		tm.setText(messageData.toString());
		return tm;
	}

	/** Send a message to the JMS chat queue from the user for the current session.
	 *
	 * @param messageData
	 * @throws JMSException
	 */
	private void sendJMSMessageToInsectChatTopic(Object messageData) throws JMSException {
		registerLogin();
		sendJMSMessageToInsectChatTopic(messageData, username);
	}

	private void sendJMSMessageToInsectChatTopic(Object messageData, String originator) throws JMSException {
		Connection connection = null;
		Session session = null;
		try {
			connection = insectChatTopicFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer messageProducer = session.createProducer(insectChatTopic);
			messageProducer.send(createJMSMessageForjmsInsectChatTopic(session, messageData, originator));
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (JMSException e) {
					logger.log(Level.WARNING, "Cannot close session", e);
				}
			}
			if (connection != null) {
				connection.close();
			}
		}
	}

}
