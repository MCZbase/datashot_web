/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.ejb.MessageBean;
import edu.harvard.mcz.imagecapture.jsfclasses.WebsocketChat;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.faces.application.FacesMessage;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

//import org.primefaces.push.EventBus;
//import org.primefaces.push.EventBusFactory;

/**
 * Connect the JMS messaging system to push to websockets.
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
    
    //@Inject
    //@Push
	//private PushContext chat;
	
    //@Inject
    //@Push
    //private PushContext serverNotifications;
    
    //@Inject
    //private WebsocketChat wsChat;
    
    @Inject
    private ChatResource chatResource;
    
    @Inject
    private ServerNotificationsResource serverNotificationsResource;
    
	public void sendMessage(Object message) {
	
		logger.log(Level.INFO, message.toString());
		
		chatResource.sendToSessions(message.toString());
		
//		logger.log(Level.INFO,chat.toString());
//		logger.log(Level.INFO,chat.URI_PREFIX);
//		Set<Future<Void>>futures = chat.send(message);
//		if (futures!=null) { 
//			logger.log(Level.INFO, "futures.size()=" + futures.size());
//			if (futures.size()==0) { 
//			    logger.log(Level.SEVERE, "No open websocket connection for chat.");
//			}
//		try {
//			Iterator<Future<Void>> i = futures.iterator();
//			while (i.hasNext()) { 
//				Future<Void> future = i.next();
//				if (future.get()==null) { 
//					logger.log(Level.INFO, "Chat message sent, future returned null.");
//				}
//			}
//		} catch (ExecutionException e) { 
//			logger.log(Level.SEVERE, "Error sending chat message.");
//			logger.log(Level.SEVERE, e.getMessage(), e);
//		} catch (InterruptedException e) {
//			logger.log(Level.SEVERE, e.getMessage(), e);
//		}
//		} else { 
//			logger.log(Level.SEVERE, "No open websocket connection for chat.");
//		}
	}

    public ChatMessageBean() {
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
			try { 
			messageBean.setUserList(chatResource.getUserList());
			} catch (Exception e) { 
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
			//EventBus eventBus = EventBusFactory.getDefault().eventBus();
			//eventBus.publish("/chat", new FacesMessage(originator + "(cmb)", text.getText()));
			if (originator.equals("Server")) { 
				//eventBus.publish("/serverNotifications",new FacesMessage(FacesMessage.SEVERITY_ERROR, originator, text.getText()));
				// serverChannel.send(new FacesMessage(FacesMessage.SEVERITY_ERROR, originator, text.getText()));
				
				serverNotificationsResource.sendToSessions(text.getText());
				
			} else { 

				sendMessage(text.getText());
			}

		} catch (JMSException ex) {
			Logger.getLogger(ChatMessageBean.class.getName()).log(Level.SEVERE, null, ex);
		}		
		
	}
	
	
}
///**
// *
// * @author mole
// */
//@MessageDriven(mappedName = "jms/InsectChatTopic", activationConfig =  {
//        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
//        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
//    })
//public class ChatMessageBean implements MessageListener {
//	private final static Logger logger = Logger.getLogger(ChatMessageBean.class.getName());
//
//    @EJB
//	private MessageBean messageBean;
//
//    public ChatMessageBean() {
//    }
//
//    public void onMessage(Message message) {
//		try {
//			TextMessage text = (TextMessage) message;
//			String originator = text.getStringProperty("Originator");
//			logger.log(Level.INFO, message.getJMSMessageID());
//			logger.log(Level.INFO, text.getText());
//			logger.log(Level.INFO, originator);
//			messageBean.addMessage(originator, text.getText());
//			//EventBus eventBus = EventBusFactory.getDefault().eventBus();
//			//eventBus.publish("/chat", new FacesMessage(originator + "(cmb)", text.getText()));
//			if (originator.equals("Server")) { 
//				//eventBus.publish("/serverNotifications",new FacesMessage(FacesMessage.SEVERITY_ERROR, originator, text.getText()));
//			}
//
//		} catch (JMSException ex) {
//			Logger.getLogger(ChatMessageBean.class.getName()).log(Level.SEVERE, null, ex);
//		}
//    }
//    
//}