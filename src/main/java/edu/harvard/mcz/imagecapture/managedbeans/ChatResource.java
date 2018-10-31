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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
//import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.websocket.OnError;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

//import org.primefaces.push.EventBus;
//import org.primefaces.push.EventBusFactory;
//import org.primefaces.push.RemoteEndpoint;
//import org.primefaces.push.annotation.Singleton;
//import org.primefaces.push.impl.JSONEncoder;

import edu.harvard.mcz.imagecapture.ejb.MessageBean;

/**
 * PrimeFacesPush atmosphere endpoint to support chat.
 * 
 * @author mole
 *
 */
@ApplicationScoped
@ServerEndpoint("/chat")
public class ChatResource {
	
	private final static Logger logger = Logger.getLogger(ChatResource.class.getName());
	
	private static Map<Session,Session> sessions = null;
	
	public ChatResource( ) {
		initSessions();
		// Required no argument constructor
	}
	
	private void initSessions() { 
		if (sessions==null) { 
			sessions = new HashMap<Session,Session>();
			logger.log(Level.INFO, this.toString());
		}
	} 
	
//    @Inject
//    @Push
//	private PushContext chat;
//	
//    @Inject
//    @Push
//    private PushContext serverNotifications;
    
    @OnMessage
    public void processMessage(String message, Session session) {
        logger.log(Level.INFO, message);
    }	
	
    @OnOpen
    public void openConnection(Session session) {
		initSessions();
		sessions.put(session, session);
        logger.log(Level.INFO, "Connection opened." + session.toString());
    }
    
    @OnClose
    public void closeConnection(Session session, CloseReason reason) { 
    	initSessions();
    	sessions.remove(session);
    	logger.log(Level.INFO, reason.getReasonPhrase());
    }
    
    @OnError
    public void error(Session session, Throwable e) { 
    	logger.log(Level.INFO, e.getMessage(), e);
    }
    
    public void sendToSessions(String message) { 
    	if (sessions!=null && !sessions.isEmpty()) { 
    		Set<Session> openSessions = sessions.keySet().iterator().next().getOpenSessions();
    		if (!openSessions.isEmpty()) { 
    			Iterator<Session> i = openSessions.iterator();
    			while (i.hasNext()) { 
    				Session s = i.next();
    				if (s.isOpen()) { 
    					try {
							s.getBasicRemote().sendText(message);
						} catch (IOException e) {
    		                logger.log(Level.SEVERE, e.getMessage(), e);
						}
    				}
    			}
    		} else { 
    		    logger.log(Level.WARNING, "no open sessions");
    		}
    	} else { 
    		logger.log(Level.WARNING, "no sessions");
    	}
    }
    
    
    public List<String> getUserList() { 
    	List<String> result = new ArrayList<String>();
    	if (sessions!=null && !sessions.isEmpty()) { 
    		Set<Session> openSessions = sessions.keySet().iterator().next().getOpenSessions();
    		if (!openSessions.isEmpty()) { 
    			logger.log(Level.INFO, "OpenSession count = " + Integer.toString(openSessions.size()));
    			Iterator<Session> i = openSessions.iterator();
    			while(i.hasNext()) {
    				Session s = i.next();
    				if (s==null) { 
    					logger.log(Level.WARNING, "Session is null"); 
    				} else { 
                       if (!s.isOpen()) {
    					   logger.log(Level.WARNING, "Session from getOpenSessions is not open."); 
                       } else { 
                    	   if (s.getUserProperties() ==null || s.getUserProperties().isEmpty()) {
    					        logger.log(Level.WARNING, "Session has no user properties."); 
                    	   } else {  
                    		   Map<String,Object> userprops = s.getUserProperties();
                    		   Set<String> keys = userprops.keySet();
                    		   Iterator<String> it = keys.iterator();
                    		   while (it.hasNext()) { 
                    			   String key = it.next();
                    			   logger.log(Level.INFO, key);
                    			   logger.log(Level.INFO, userprops.get(key).toString());
                    		   }
                    		   Boolean active = (Boolean)userprops.get("active");
                    		   if (active!=null && active) {
                                   result.add(s.getUserProperties().get("name").toString());
                    		   } else { 
      					           logger.log(Level.INFO, "User with active false or null."); 
                    		   }
                    	   }
                       }
    				}
    			}
    		}
    	}
    	return result;
    }
	
//	@Resource(name= "jms/InsectChatTopic", type=javax.jms.Topic.class, mappedName = "jms/InsectChatTopic")
//	private Topic insectChatTopic;
//	@Resource(mappedName = "jms/InsectChatTopicFactory")
//	private ConnectionFactory insectChatTopicFactory;	

//    @EJB
//	private MessageBean messageBean;	
    
//    @OnOpen
//    public void onOpen(javax.websocket.Session r) { 
//		String address = r.getUserProperties().get("javax.websocket.endpoint.remoteAddress").toString();
//		logger.log(Level.INFO, "Connection open from: " + address);
//   }
	
//	@OnMessage
//	public String onMessage(FacesMessage message) {
//		logger.log(Level.INFO, message.getDetail());
//	    return message.getDetail();
//	}
//	
//	/* Testing jms/prime push interactions.  */
//	@OnClose
//	public void onClose(javax.websocket.Session r) { 
//		String address = r.getUserProperties().get("javax.websocket.endpoint.remoteAddress").toString();
//		logger.log(Level.INFO, "Connection close: " + address);
//	
//		String message = "Connection to " + address + " lost.";
//		try {
//			sendJMSMessageToInsectChatTopic(message,"System (cr)");
//		} catch (JMSException e) {
//			logger.log(Level.WARNING,e.getMessage());
//		    //EventBus eventBus = EventBusFactory.getDefault().eventBus();
//		    //eventBus.publish("/chat", new FacesMessage("Connection Close", message));		
//		}
//		
//		try {
//		   messageBean.decrementUserCount();
//		} catch (NullPointerException e) {}
//		
//		
//	}
//	
//	/* Testing jms/prime push interactions.  */
//	private Message createJMSMessageForjmsInsectChatTopic(Session session, Object messageData, String originator) throws JMSException {
//		TextMessage tm = session.createTextMessage();
//		tm.setStringProperty("Originator", originator);
//		tm.setText(messageData.toString());
//		return tm;
//	}	
//	
//	/* Testing jms/prime push interactions.  */
//	private void sendJMSMessageToInsectChatTopic(Object messageData, String originator) throws JMSException {
//		Connection connection = null;
//		Session session = null;
//		try {
//			connection = insectChatTopicFactory.createConnection();
//			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//			MessageProducer messageProducer = session.createProducer(insectChatTopic);
//			messageProducer.send(createJMSMessageForjmsInsectChatTopic(session, messageData, originator));
//		} finally {
//			if (session != null) {
//				try {
//					session.close();
//				} catch (JMSException e) {
//					logger.log(Level.WARNING, "Cannot close session", e);
//				}
//			}
//			if (connection != null) {
//				connection.close();
//			}
//		}
//	}	
	
}