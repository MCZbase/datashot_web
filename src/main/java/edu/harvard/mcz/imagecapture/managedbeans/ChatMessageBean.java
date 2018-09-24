/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.ejb.MessageBean;

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

    @Inject
    @Push(channel="chat")
	private PushContext chatChannel;
	
    @Inject
    @Push(channel="serverNotifications")
    private PushContext serverChannel;
    
	public void sendMessage(Object message) { 
		chatChannel.send(message);
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
		//EventBus eventBus = EventBusFactory.getDefault().eventBus();
		//eventBus.publish("/chat", new FacesMessage(originator + "(cmb)", text.getText()));
		if (originator.equals("Server")) { 
			//eventBus.publish("/serverNotifications",new FacesMessage(FacesMessage.SEVERITY_ERROR, originator, text.getText()));
			// serverChannel.send(new FacesMessage(FacesMessage.SEVERITY_ERROR, originator, text.getText()));
			serverChannel.send(text.getText());
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