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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;

import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;


/**
 * PrimeFacesPush atmosphere endpoint to support server messaging.
 * @author mole
 *
 */
@PushEndpoint("/serverNotifications")
public class ServerNotificationsResource {
	private final static Logger logger = Logger.getLogger(ServerNotificationsResource.class.getName());
	
	@OnMessage(encoders = {JSONEncoder.class})
	public FacesMessage onMessage(FacesMessage message) {
		logger.log(Level.INFO, message.getDetail());
	    return message;
	}
}