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

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.bean.ManagedProperty;
import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageProducer;
//import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import jakarta.websocket.OnError;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

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