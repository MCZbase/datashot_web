package edu.harvard.mcz.imagecapture.ejb;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Session Bean implementation class SessionTerminationMonitorBean
 */
@Singleton(mappedName = "sessionTerminationMonitorBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Local( { HttpSessionListener.class })
@LocalBean
public class SessionTerminationMonitorBean implements HttpSessionListener {
	
	private final static Logger logger = Logger.getLogger(SessionTerminationMonitorBean.class.getName());
	
	@Resource(name= "jms/InsectChatTopic", type=javax.jms.Topic.class, mappedName = "jms/InsectChatTopic")
	private Topic insectChatTopic;
	@Resource(mappedName = "jms/InsectChatTopicFactory")
	private ConnectionFactory insectChatTopicFactory;

	@EJB
	private MessageBean messageBean;
	@EJB
	private UsersFacadeLocal usersFacade;
    /**
     * Default constructor. 
     */
    public SessionTerminationMonitorBean() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent sessionEvent) {
		logger.log(Level.INFO, null, "A session started.");
        // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent sessionEvent) {
		logger.log(Level.INFO, null, "A session was destroyed.");
    	String logoutReason = (String) sessionEvent.getSession().getAttribute("logoutreason");

        if (logoutReason == null) {
            logoutReason = " Session Timeout";
        }

		try {
			sendJMSMessageToInsectChatTopic("Someone has gone offline " + logoutReason, "System");
		} catch (JMSException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
    }

	private Message createJMSMessageForjmsInsectChatTopic(Session session, Object messageData, String originator) throws JMSException {
		TextMessage tm = session.createTextMessage();
		tm.setStringProperty("Originator", originator);
		tm.setText(messageData.toString());
		return tm;
	}    
    
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
