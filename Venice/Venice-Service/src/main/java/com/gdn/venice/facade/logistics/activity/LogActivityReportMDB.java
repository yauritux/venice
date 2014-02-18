package com.gdn.venice.facade.logistics.activity;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.facade.logistics.activity.processor.ActivityReportProcessor;
import com.gdn.venice.persistence.LogFileUploadLog;

/**
 * Message-Driven Bean implementation class for: LogActivityReportSessionEJBBEan
 *
 */
@Interceptors(SpringBeanAutowiringInterceptor.class)
@MessageDriven(name = "LogActivityReportMDB",
activationConfig = {
    @ActivationConfigProperty(propertyName="destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName="destination", propertyValue = "SendReceiveQueue"),
    @ActivationConfigProperty(propertyName="acknowledgeMode", propertyValue="Auto-acknowledge"), 
    @ActivationConfigProperty(propertyName="maxMessagesPerSessions", propertyValue="20")
//    @ActivationConfigProperty(propertyName="maxSessions", propertyValue="20")
    })
public class LogActivityReportMDB implements MessageListener {

    protected static Logger _log = null;
    
    @Autowired
    @Qualifier("JNEActivityReportProcessor")
    ActivityReportProcessor activityReportProcessorJNE;
    
    @Autowired
    @Qualifier("NCSActivityReportProcessor")
    ActivityReportProcessor activityReportProcessorNCS;
    
    @Autowired
    @Qualifier("MSGActivityReportProcessor")
    ActivityReportProcessor activityReportProcessorMSG;
    
    @Autowired
    @Qualifier("RPXActivityReportProcessor")
    ActivityReportProcessor activityReportProcessorRPX;
    
    /**
     * Default constructor.
     */
    public LogActivityReportMDB() {
        super();
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.facade.logistics.activity.LogActivityReportMDB");
    }

    /**
     * @see MessageListener#onMessage(Message)
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message message) {
        try {
            ObjectMessage msg = (ObjectMessage) message;
            LogFileUploadLog fileUploadLog = (LogFileUploadLog) msg.getObject();

            if (fileUploadLog.getFileUploadFormat().equals("JNE")) {
            	activityReportProcessorJNE.process(fileUploadLog);
            } else if (fileUploadLog.getFileUploadFormat().equals("NCS")) {
            	activityReportProcessorNCS.process(fileUploadLog);
            } else if (fileUploadLog.getFileUploadFormat().equals("RPX")) {
            	activityReportProcessorRPX.process(fileUploadLog);
            } else if (fileUploadLog.getFileUploadFormat().equals("MSG")) {
                activityReportProcessorMSG.process(fileUploadLog);
            }
        } catch (JMSException e) {
            _log.error("Error on reading message", e);
            e.printStackTrace();
        }
    }

}
