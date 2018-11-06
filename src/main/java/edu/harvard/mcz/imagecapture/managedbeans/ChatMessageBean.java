/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.ejb.MessageBean;

import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

//import javax.faces.application.FacesMessage;

/**
 * Connect the JMS messaging system to push to websockets, message driven bean that acts
 * upon JMS messages, listens to InsectChatTopic, and 
 * 
 * @author mole
 *
 */
@MessageDriven(mappedName = "jms/InsectChatTopic", activationConfig =  {
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
    })
@Named("chatMessageBean")
public class ChatMessageBean implements MessageListener {
	private final static Logger logger = Logger.getLogger(ChatMessageBean.class.getName());
	
    @EJB(beanName="messageBean")
	private MessageBean messageBean;
    
    @Inject
    private ChatResource chatResource;
    
    @Inject
    private ServerNotificationsResource serverNotificationsResource;
    
    public ChatMessageBean() {
    }
 
    /** Send a message to web socket messaging. 
     *  
     * @param message
     */
	public void sendMessage(Object message) {
	
		logger.log(Level.INFO, message.toString());
		
		chatResource.sendToSessions(message.toString());
		
	}

	/**
	 *  Check the websocket active user list, and update the list of users stored in MessageBean.
	 * 
	 */
	public void updateUserList() {
		HashSet<String> usernames = new HashSet<String>();
		Set<Principal> p = chatResource.getUsers();
		Iterator<Principal> i = p.iterator();
		while (i.hasNext()) { 
		    Principal principal = i.next();
		    String pname = principal.getName();
		    String username = pname;

		    usernames.add(username);
		}
		messageBean.updateCurrentUserList(usernames);
	}
	
	/* (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	@Override
	public void onMessage(Message message) {
		logger.log(Level.INFO, message.toString());

		try {
			TextMessage text = (TextMessage) message;
			String originator = text.getStringProperty("Originator");
			logger.log(Level.INFO, message.getJMSMessageID());
			logger.log(Level.INFO, text.getText());
			logger.log(Level.INFO, originator);
			messageBean.addMessage(originator, text.getText());
			
			if (originator.equals("Server")) { 
				// NOTE: To change back to using primefaces Growl for server messages, construct and send 
				// a faces message here.
				// FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, originator, text.getText());
				
				// Using a modal dialog instead of Growl, we'll just send a string to display in the dialog.
				serverNotificationsResource.sendToSessions(text.getText());
				
			} else { 

				sendMessage(text.getText());
			}

		} catch (JMSException ex) {
			logger.log(Level.SEVERE, ex.getMessage(), ex);
		}		
	
		// Note: if something happens here and the throwable isn't caught, then
		// the message will be attempted to be sent again, and the likely result
		// is infinite repetition of messages to open websocket listeners...
		try { 
	   	    updateUserList();
		} catch (Exception e) { 
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		
	}
	
}