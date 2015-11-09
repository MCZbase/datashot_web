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
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;

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
@ManagedBean(name = "serverWarningController")
@SessionScoped
@MessageDriven(mappedName = "jms/InsectChatTopic", activationConfig =  {
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
    })
public class ServerWarningController implements Serializable, MessageListener {

	private final static Logger logger = Logger.getLogger(ServerWarningController.class.getName());

	@Resource(name= "jms/InsectChatTopic", type=javax.jms.Topic.class, mappedName = "jms/InsectChatTopic")
	private Topic insectChatTopic;
	@Resource(mappedName = "jms/InsectChatTopicFactory")
	private ConnectionFactory insectChatTopicFactory;

	public ServerWarningController() {
		logger.log(Level.INFO, "Instatntiating new ServerWarningController");
	}

	public void onMessage(Message jmsMessage) {
		try {
			logger.log(Level.INFO, jmsMessage.getJMSMessageID());
			String originator = jmsMessage.getStringProperty("Originator");
			if (originator.equals( MessageBean.SERVER_MESSAGE_SOURCE)) { 					
	    	    FacesMessage facesMessage = new FacesMessage("Message From: " + originator, ((TextMessage)jmsMessage).getText());
	    	    facesMessage.setSeverity(FacesMessage.SEVERITY_WARN);
	    	    
				EventBus eventBus = EventBusFactory.getDefault().eventBus();
				eventBus.publish("/serverNotifications",facesMessage);	    	    
	    	    //PushContext pushContext = PushContextFactory.getDefault().getPushContext();
	    	    //pushContext.push("/serverNotifications", facesMessage);
	    	    logger.log(Level.INFO, ((TextMessage)jmsMessage).getText());
			}
		} catch (JMSException e) {
			logger.log(Level.WARNING, e.getMessage());
		}
		
	}
	
}
