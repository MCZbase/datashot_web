/**
 * WebsocketObserver.java
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

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.event.WebsocketEvent;
import jakarta.faces.event.WebsocketEvent.Closed;
import jakarta.faces.event.WebsocketEvent.Opened;
import jakarta.websocket.CloseReason.CloseCode;


/**
 * @author mole
 *
 */
@ApplicationScoped
public class WebsocketObserver {
	private final static Logger logger = Logger.getLogger(WebsocketObserver.class.getName());
	
	public void onOpen(@Observes @Opened WebsocketEvent event) {
	         String channel = event.getChannel(); // Returns <f:websocket channel>.
	         Long userId = event.getUser(); // Returns <f:websocket user>, if any.
	         logger.log(Level.INFO, "Opened Channel:" +  channel);
	         logger.log(Level.INFO, "User:" +  userId.toString());
	}

	public void onClose(@Observes @Closed WebsocketEvent event) {
	         String channel = event.getChannel(); // Returns <f:websocket channel>.
	         Long userId = event.getUser(); // Returns <f:websocket user>, if any.
	         CloseCode code = event.getCloseCode(); // Returns close reason code.
	         logger.log(Level.INFO, "Closed Channel:" +  channel);
	         logger.log(Level.INFO, "User:" +  userId.toString());
	         logger.log(Level.INFO, "CloseCode:" +  Integer.toString(code.getCode()));
	}

}
