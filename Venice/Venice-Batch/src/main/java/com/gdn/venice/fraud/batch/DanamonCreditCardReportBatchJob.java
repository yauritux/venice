package com.gdn.venice.fraud.batch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.ejb.EJBException;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.fraud.dataexport.InstallmentGenerator;
import com.gdn.venice.fraud.dataexport.bean.Installment;
import com.gdn.venice.util.EmailSender;
import com.gdn.venice.util.VeniceConstants;

/**
 * Batch job for generating Danamon Credit Card report 
 * 
 * @author Daniel Hutama Putra
 */
public class DanamonCreditCardReportBatchJob {
	
	protected static Logger _log = null;
	private static String CONFIG_FILE = System.getenv("VENICE_HOME") + "/admin/config.properties";
	private String dbHost = "";
	private String dbPort = "";
	private String dbUsername = "";
	private String dbPassword = "";
	private String environment = "";
	private String dbName = "";
	private static Connection conn;
	
	private static final String CONVERT_INSTALLMENT_LIST_SQL = "select op.order_payment_id, op.wcs_payment_id, o.wcs_order_id, o.order_date, op.reference_id, op.amount, op.tenor, op.installment, op.interest, op.interest_installment, op.installment_sent_flag, op.installment_sent_date, c.customer_user_name, p.full_or_legal_name " +
																											"from ven_order o " +
																											"left join ven_order_payment_allocation opa on o.order_id=opa.order_id " +
																											"left join ven_order_payment op on opa.order_payment_id=op.order_payment_id " +
																											"left join ven_customer c on c.customer_id=o.customer_id " +
																											"left join ven_party p on p.party_id=c.party_id " +
																											"left join ven_bin_credit_limit_estimate b on b.bin_number=substr(op.masked_credit_card_number,0,7) " +
																											"where o.order_status_id= " + VeniceConstants.VEN_ORDER_STATUS_FP +
																											" and (op.wcs_payment_type_id= " +VeniceConstants.VEN_WCS_PAYMENT_TYPE_ID_DanamonCreditCard +
																											"or (op.wcs_payment_type_id= " +VeniceConstants.VEN_WCS_PAYMENT_TYPE_ID_MIGSCreditCard + "))"+
																											" and b.bank_name='"+VeniceConstants.VEN_BIN_CREDIT_LIMIT_ESTIMATE_BANK_NAME_DANAMON+
																											"' and op.installment_sent_flag=false and o.order_date>=?";
	
	private static final String UPDATE_INSTALLMENT_LIST_SQL = "update ven_order_payment set installment_sent_flag = true, installment_sent_date=? where wcs_payment_id=?";
	
	private static final String INSERT_INSTALLMENT_HISTORY_LIST_SQL = "insert into ven_order_payment_installment_history (order_payment_id, installment_timestamp, history_reason) values (?, ?, ?)";
	
    private DanamonCreditCardReportBatchJob() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.fraud.batch.DanamonCreditCardReportBatchJob");
        
    	Properties prop = new Properties();
		prop.load(new FileInputStream(CONFIG_FILE));
		environment = prop.getProperty("environment");
		dbHost = prop.getProperty(environment + ".dbHost");
		dbPort = prop.getProperty(environment + ".dbPort");
		dbUsername = prop.getProperty(environment + ".dbUsername");
		dbPassword = prop.getProperty(environment + ".dbPassword");
		dbName = prop.getProperty(environment + ".dbName");
		
		System.out.println("environment: "+environment);
		System.out.println("dbHost: "+dbHost);
		System.out.println("dbPort: "+dbPort);
		
		setupDBConnection();
    }
    
	private void setupDBConnection() throws ClassNotFoundException, SQLException{
		Class.forName("org.postgresql.Driver");		
		conn = DriverManager.getConnection("jdbc:postgresql://" + dbHost +":" + dbPort + "/" + dbName, dbUsername, dbPassword);
	}
	
	private  ArrayList<Installment> generateInstallmentReport(){
		PreparedStatement psInstallmentList = null;      
      	ResultSet rsInstallmentList = null;
      	ArrayList<Installment> InstallmentList = null;	
	    
	    try{	            
	    	_log.debug("Query installment data");
	    	
	    	InstallmentList = new ArrayList<Installment>();
            psInstallmentList = conn.prepareStatement(CONVERT_INSTALLMENT_LIST_SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date paramDate = DateUtils.addDays((Date) sdf.parse(sdf.format(calendar.getTime())), -30);
            
            psInstallmentList.setDate(1, new java.sql.Date(paramDate.getTime()));
    		rsInstallmentList = psInstallmentList.executeQuery();
    		
    		rsInstallmentList.last();
			int totalInstallmentList = rsInstallmentList.getRow();
			rsInstallmentList.beforeFirst();
			rsInstallmentList.next();
    		
			_log.info("Query returns: " + totalInstallmentList + " row(s)");
			
			if(totalInstallmentList==0){
				return null;
			}else{            	            
	            for (int i=0; i<totalInstallmentList;i++) {
	            	Installment installment= new Installment();
	            	installment.setOrderPaymentId(rsInstallmentList.getLong("order_payment_id"));
	            	installment.setWcsOrderId(rsInstallmentList.getString("wcs_order_id"));
	            	installment.setWcsPaymentId(rsInstallmentList.getString("wcs_payment_id"));
	            	installment.setOrderDate(rsInstallmentList.getDate("order_date"));
	            	installment.setReferenceId(rsInstallmentList.getString("reference_id"));
	            	installment.setAmount(rsInstallmentList.getBigDecimal("amount"));
	            	installment.setTenor(rsInstallmentList.getInt("tenor"));
	            	installment.setInstallment(rsInstallmentList.getBigDecimal("installment"));
	            	installment.setInterest(rsInstallmentList.getBigDecimal("interest"));
	            	installment.setInteresInstallment(rsInstallmentList.getBigDecimal("interest_installment"));
	            	installment.setInstallmentSentFlag(rsInstallmentList.getBoolean("installment_sent_flag"));
	            	installment.setInstallmentSentDate(rsInstallmentList.getDate("installment_sent_date"));
	            	installment.setCustomerUserName(rsInstallmentList.getString("customer_user_name"));
	            	installment.setCustomerName(rsInstallmentList.getString("full_or_legal_name"));
	            	InstallmentList.add(installment);
	            	rsInstallmentList.next();
	            }
			}
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
            	if(psInstallmentList!=null) psInstallmentList.close();
            	if(rsInstallmentList!=null) rsInstallmentList.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
		return InstallmentList;
	}

    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
    	DanamonCreditCardReportBatchJob danamonCreditCardJob = new DanamonCreditCardReportBatchJob();
    	_log.info("Start DanamonCreditCardReportBatchJob");
        
        Long startTime = System.currentTimeMillis();
                
        ArrayList<Installment> danamonCreditCardList = danamonCreditCardJob.generateInstallmentReport();
        
        if(danamonCreditCardList!=null){
        	String filePath = System.getenv("VENICE_HOME") + "/files/export/fraud/installment/";
    		String fileName = "ConvertDanamonCreditCard" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date())+ ".xls";
    		 
    		Date date = new Date();
    		PreparedStatement psUpdateInstallment = null;
    		PreparedStatement psInsertInstallmentHistory = null;
    		try{
    			_log.debug("update installment date and installment sent flag, insert history");				
				for(int i=0;i<danamonCreditCardList.size();i++){
					psUpdateInstallment = conn.prepareStatement(UPDATE_INSTALLMENT_LIST_SQL);
					psUpdateInstallment.setDate(1, new java.sql.Date(date.getTime()));
					psUpdateInstallment.setString(2, danamonCreditCardList.get(i).getWcsPaymentId());    			    	
					psUpdateInstallment.executeUpdate();    			    	
					
					danamonCreditCardList.get(i).setInstallmentSentDate(date);
					
					psInsertInstallmentHistory = conn.prepareStatement(INSERT_INSTALLMENT_HISTORY_LIST_SQL);
					psInsertInstallmentHistory.setLong(1, danamonCreditCardList.get(i).getOrderPaymentId());
					psInsertInstallmentHistory.setTimestamp(2, new Timestamp(System.currentTimeMillis()));  
					psInsertInstallmentHistory.setString(3, "Convert to installment, updated by system");    
					psInsertInstallmentHistory.executeUpdate();
				}
    			
				_log.info("generate excel file");
        		InstallmentGenerator installmentGenerator = new InstallmentGenerator();
        		installmentGenerator.exportInstallmentData(filePath + fileName, danamonCreditCardList);
        		
        		_log.info("send email");
    			EmailSender es = new EmailSender();
    			Boolean sendFiles = es.sendInstallmentFiles(VeniceConstants.FRAUD_INSTALLMENT_BANK_REPORT_BATCH_JOB_DANAMON);
    			if (!sendFiles) {
    				_log.error("send files failed");
    			}else{
    				_log.info("done send email");    				
    			}
    		}catch (Exception e) {
    			_log.error(e);    			
    			throw new EJBException(e);
    		}finally{
    			if(psUpdateInstallment!=null) psUpdateInstallment.close();
    			if(psInsertInstallmentHistory!=null) psInsertInstallmentHistory.close();
    			conn.close();
    		}
        }else{
        	_log.info("No installment data exported");
        }

        Long endTime = System.currentTimeMillis();
        _log.info("DanamonCreditCardReportBatchJob finished, with duration:" + (endTime - startTime) + "ms");
    }
}

