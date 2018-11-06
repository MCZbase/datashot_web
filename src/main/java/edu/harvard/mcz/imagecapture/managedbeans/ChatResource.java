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
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.faces.bean.ManagedProperty;
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
import edu.harvard.mcz.imagecapture.jsfclasses.ChatController;

/**
 * Websocket endpoint to support chat.
 * 
 * @author mole
 *
 */
@ApplicationScoped
@ServerEndpoint("/chat")
public class ChatResource {
	
	private final static Logger logger = Logger.getLogger(ChatResource.class.getName());
	
	private static Map<Session,Principal> sessions = Collections.synchronizedMap(new HashMap<Session,Principal>());
	
	public ChatResource( ) {
		// Required no argument constructor
	}
	
	
    @OnMessage
    public void processMessage(String message, Session session) {
        logger.log(Level.INFO, message);
    }	
	
    @OnOpen
    public void openConnection(Session session) {
		sessions.put(session, session.getUserPrincipal());
		logger.log(Level.INFO, "openConnection() User:" + session.getUserPrincipal().getName());
        logger.log(Level.INFO, "Connection opened: " + session.toString());
		logger.log(Level.INFO, "Number of Connnections: " + Integer.toString(sessions.size()));
    }
    
    @OnClose
    public void closeConnection(Session session, CloseReason reason) { 
    	sessions.remove(session);
    	logger.log(Level.INFO, reason.getReasonPhrase());
    }
    
    @OnError
    public void error(Session session, Throwable e) { 
    	logger.log(Level.INFO, e.getMessage(), e);
    }
    
    /**
     * Send a message to all open websocket sessions.
     * 
     * @param message the string message to send.
     */
    public void sendToSessions(String message) { 
    	if (sessions!=null && !sessions.isEmpty()) {
    		Set<Session> ses = sessions.keySet();
    		Iterator<Session> i = ses.iterator();
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
    		logger.log(Level.WARNING, "no sessions");
    	}
    }
 
   
    /**
     * Obtain the websocket session's list of current active users. 
     * 
     * @return set of principals that have sessions.
     */
    public Set<Principal> getUsers() { 
    	Set<Principal> result = new HashSet<Principal>();
    	Iterator<Principal> i = sessions.values().iterator();
    	while (i.hasNext()) { 
    		Principal p = i.next();
    		if (p!=null) { 
    		   result.add(p);
    		}
    	}
    	return (result);
    }
	
}