/**
 * ServerNotificationsResource.java
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


/**
 * Websocket endpoint to support server messaging.
 * @author mole
 *
 */
@ApplicationScoped
@ServerEndpoint("/serverNotifications")
public class ServerNotificationsResource {
	
	private final static Logger logger = Logger.getLogger(ServerNotificationsResource.class.getName());
	
	private static Map<Session,Session> sessions = null;
	
	public ServerNotificationsResource() { 
		initSessions();
		// Required no argument constructor
	}
	
	private void initSessions() { 
		if (sessions==null) { 
			sessions = new HashMap<Session,Session>();
			logger.log(Level.INFO, this.toString());
		}
	}
	
	@OnMessage
	public void onMessage(String message) {
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
}