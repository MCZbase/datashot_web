/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.mcz.imagecapture.jsfclasses;

import edu.harvard.mcz.imagecapture.ejb.MessageBean;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

/** JSF Managed bean for interaction with the Chat system.
 * 
 * Serves as an adapter between JMS and PrimeFaces messaging.
 * 
 * Example of use to push JMS messages to a growl component on
 * a web page. 
 * <pre>
       <p:socket onMessage="handleMessage" channel="/serverNotifications" />
       <script type="text/javascript">
           function handleMessage(msg) {
               servergrowl.show([msg]);
           }
      </script>
      <p:growl widgetVar="servergrowl" id="servergrowl" showDetail="true" sticky="true" autoUpdate="false" />
   </pre>
 *
 * @author mole
 */
@Named("serverWarningController")
@SessionScoped
@MessageDriven(mappedName = "jms/InsectChatTopic", activationConfig =  {
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
    })
public class ServerWarningController implements Serializable, MessageListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger logger = Logger.getLogger(ServerWarningController.class.getName());

	@Resource(name= "jms/InsectChatTopic", type=javax.jms.Topic.class, mappedName = "jms/InsectChatTopic")
	private Topic insectChatTopic;
	@Resource(mappedName = "jms/InsectChatTopicFactory")
	private ConnectionFactory insectChatTopicFactory;

    @Inject
    @Push(channel="serverNotifications")
    private PushContext serverChannel;	
	
	public ServerWarningController() {
		logger.log(Level.INFO, "Instatntiating new ServerWarningController");
	}

	public void onMessage(Message jmsMessage) {
		try {
			logger.log(Level.INFO, "ID:" + jmsMessage.getJMSMessageID());
			String originator = jmsMessage.getStringProperty("Originator");
			logger.log(Level.INFO,originator);
			if (originator.equals( MessageBean.SERVER_MESSAGE_SOURCE)) { 					
	    		TextMessage text = (TextMessage) jmsMessage;
	    	    FacesMessage facesMessage = new FacesMessage("Message From: " + originator, text.getText());
	    	    facesMessage.setSeverity(FacesMessage.SEVERITY_WARN);
	    	    
				Set<Future<Void>> sent = serverChannel.send(facesMessage);
				//serverChannel.send(text.getText());
				Iterator<Future<Void>> i = sent.iterator();
				while (i.hasNext()) { 
 				    logger.log(Level.INFO,i.next().get().toString());
				}
	    	    
				//EventBus eventBus = EventBusFactory.getDefault().eventBus();
				//eventBus.publish("/serverNotifications",facesMessage);	    	    
	    	    //PushContext pushContext = PushContextFactory.getDefault().getPushContext();
	    	    //pushContext.push("/serverNotifications", facesMessage);
	    	    logger.log(Level.INFO, "Sent:" + ((TextMessage)jmsMessage).getText());
			}
		} catch (JMSException e) {
			logger.log(Level.WARNING, e.getMessage());
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
		} catch (ExecutionException e) {
			logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}
