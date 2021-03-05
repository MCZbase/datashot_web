/**
 * WebsocketChat.java
 * edu.harvard.mcz.imagecapture.jsfclasses
 * Copyright © 2018 President and Fellows of Harvard College
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
package edu.harvard.mcz.imagecapture.jsfclasses;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * @author mole
 *
 */

public class WebsocketChat extends Endpoint {
	private final static Logger logger = Logger.getLogger(WebsocketChat.class.getName());
	public WebsocketChat() { 
	}
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        // https://java.net/jira/browse/WEBSOCKET_SPEC-240
	    logger.log(Level.INFO, "openConnection(" + session.getId() + ")");
	    logger.log(Level.INFO, session.getQueryString());
	    
        final RemoteEndpoint remote = session.getBasicRemote();
        session.addMessageHandler(String.class, new MessageHandler.Whole<String>() {
            public void onMessage(String text) {
            	logger.log(Level.INFO,"onMessage: " + text);
//                try {
//                	
//                    remote.sendString("Got your message (" + text + "). Thanks !");
//                } catch (IOException ioe) {
//                	logger.log(Level.SEVERE, ioe.getMessage(), ioe);
//                }
            }
        });	    
    }
}


/*@ServerEndpoint( "/chat" )
public class WebsocketChat {
	
	public WebsocketChat() { 
	}
	
	private final static Logger logger = Logger.getLogger(WebsocketChat.class.getName());
	
     @Resource(name="comp/DefaultManagedExecutorService")
     private ManagedExecutorService mes;
	
	 //@Inject @Push
	 //private PushContext chat;

	 @Inject
	 private WebsocketObserver wos;
	 
	 @OnOpen
	 public void openConnection(Session session) {
	        logger.log(Level.INFO, "openConnection(" + session.getId() + ")");
	        logger.log(Level.INFO, session.getQueryString());
	 }
	 
	 @OnMessage
	 public void message(final Session session, Object message) {
	        logger.log(Level.INFO, "message(" + session.getId() +"):" +  message.toString());
	        sendMessage(session, message);
	 }
	 
	 public void sendMessage(Session session, Object message ) { 
		 
	        try {
	            for (Session s : session.getOpenSessions()) {
	                if (s.isOpen()) {
	                    s.getBasicRemote().sendObject(message);
	                    logger.log(Level.INFO, "sendMessage("+ s.getId() +")", message.toString());
	                }
	            }
	        } catch (IOException e) { 
	            logger.log(Level.WARNING, e.getMessage(), e);
	        }   catch (EncodeException e) {
	            logger.log(Level.WARNING, e.getMessage(), e);
	        }

	 
	 // public void sendMessage(Object message) {
//			logger.log(Level.INFO,chat.toString());
//			logger.log(Level.INFO,chat.URI_PREFIX);
//			Set<Future<Void>>futures = chat.send(message);
//			if (futures!=null) { 
//				logger.log(Level.INFO, "futures.size()=" + futures.size());
//				if (futures.size()==0) { 
//				    logger.log(Level.SEVERE, "No open websocket connection for chat.");
//				}
//			try {
//				Iterator<Future<Void>> i = futures.iterator();
//				while (i.hasNext()) { 
//					Future<Void> future = i.next();
//					if (future.get()==null) { 
//						logger.log(Level.INFO, "Chat message sent, future returned null.");
//					}
//				}
//			} catch (ExecutionException e) { 
//				logger.log(Level.SEVERE, "Error sending chat message.");
//				logger.log(Level.SEVERE, e.getMessage(), e);
//			} catch (InterruptedException e) {
//				logger.log(Level.SEVERE, e.getMessage(), e);
//			}
//			} else { 
//				logger.log(Level.SEVERE, "No open websocket connection for chat.");
//			}
	 }
	
}*/
