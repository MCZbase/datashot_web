/**
 * ChatResource.java
 * edu.harvard.mcz.imagecapture.managedbeans
 * Copyright © 2014 President and Fellows of Harvard College
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of Version 2 of the GNU General Public License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Paul J. Morris
 */
package edu.harvard.mcz.imagecapture.managedbeans;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
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
import org.primefaces.push.RemoteEndpoint;
import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.OnOpen;
import org.primefaces.push.annotation.OnClose;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.annotation.Singleton;
import org.primefaces.push.impl.JSONEncoder;

import edu.harvard.mcz.imagecapture.ejb.MessageBean;

/**
 * PrimeFacesPush atmosphere endpoint to support chat.
 * 
 * @author mole
 *
 */
@PushEndpoint("/chat")
@Singleton
public class ChatResource {
	
	private final static Logger logger = Logger.getLogger(ChatResource.class.getName());
	
	@Resource(name= "jms/InsectChatTopic", type=javax.jms.Topic.class, mappedName = "jms/InsectChatTopic")
	private Topic insectChatTopic;
	@Resource(mappedName = "jms/InsectChatTopicFactory")
	private ConnectionFactory insectChatTopicFactory;	

    @EJB
	private MessageBean messageBean;	
    
    @OnOpen
    public void onOpen(RemoteEndpoint r) { 
		String address = r.address();
		logger.log(Level.INFO, "Connection open from: " + address);
    }
	
	@OnMessage(encoders = {JSONEncoder.class})
	public String onMessage(FacesMessage message) {
		logger.log(Level.INFO, message.getDetail());
	    return message.getDetail();
	}
	
	/* Testing jms/prime push interactions.  */
	@OnClose
	public void onClose(RemoteEndpoint r) { 
		String address = r.address();
		logger.log(Level.INFO, "Connection close: " + address);
	
		String message = "Connection to " + address + " lost.";
		try {
			sendJMSMessageToInsectChatTopic(message,"System (cr)");
		} catch (JMSException e) {
			logger.log(Level.WARNING,e.getMessage());
		    EventBus eventBus = EventBusFactory.getDefault().eventBus();
		    eventBus.publish("/chat", new FacesMessage("Connection Close", message));		
		}
		
		try {
		   messageBean.decrementUserCount();
		} catch (NullPointerException e) {}
		
		
	}
	
	/* Testing jms/prime push interactions.  */
	private Message createJMSMessageForjmsInsectChatTopic(Session session, Object messageData, String originator) throws JMSException {
		TextMessage tm = session.createTextMessage();
		tm.setStringProperty("Originator", originator);
		tm.setText(messageData.toString());
		return tm;
	}	
	
	/* Testing jms/prime push interactions.  */
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