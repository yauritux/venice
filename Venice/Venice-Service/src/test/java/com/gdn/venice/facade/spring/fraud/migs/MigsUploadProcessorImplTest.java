package com.gdn.venice.facade.spring.fraud.migs;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.instanceOf;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.gdn.venice.constants.VeniceEnvironment;
import com.gdn.venice.dao.VenMigsUploadMasterDAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.exception.MIGSFileParserException;
import com.gdn.venice.facade.spring.fraud.migs.strategy.FileParserStrategy;
import com.gdn.venice.fraud.dataimport.MigsReport;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.persistence.VenMigsUploadTemporary;
import com.gdn.venice.util.CommonUtil;

@RunWith(MockitoJUnitRunner.class)
public class MigsUploadProcessorImplTest {
	
	MigsUploadProcessorServiceImpl sut;
	
	@Mock
	FileParserStrategy fileParserStrategyMock;
	
	@Mock
	VenMigsUploadMasterDAO venMigsUploadMasterDAOMock;
	
	@Mock
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAOMock;
	
	@Before
	public void setup(){
		sut = new MigsUploadProcessorServiceImpl();
		sut.fileParserStrategy = fileParserStrategyMock;
		sut.venMigsUploadMasterDAO = venMigsUploadMasterDAOMock;
		sut.venOrderPaymentAllocationDAO = venOrderPaymentAllocationDAOMock;
		
		CommonUtil.veniceEnv = VeniceEnvironment.TESTING;
	}
	
	@Test
	public void parse_noErrorDuringFileParsing_returnsListOfMigsObjects() throws MIGSFileParserException{
		
		MigsReport migsReport = new MigsReport();
		ArrayList<PojoInterface> resultListMock = new ArrayList<PojoInterface>(1);
		resultListMock.add(migsReport);
		
		doNothing().when(fileParserStrategyMock).parse(anyString());
		when(fileParserStrategyMock.isError()).thenReturn(false);
		when(fileParserStrategyMock.getParseResult()).thenReturn(resultListMock);
		
		ArrayList<PojoInterface> resultList = sut.parse("file name");
		assertThat(resultList.get(0), instanceOf(MigsReport.class));
	}
	
	@Test(expected=MIGSFileParserException.class)
	public void parse_errorDuringFileParsing_throwsException() throws MIGSFileParserException{
		
		MigsReport migsReport = new MigsReport();
		ArrayList<PojoInterface> resultListMock = new ArrayList<PojoInterface>(1);
		resultListMock.add(migsReport);
		
		doNothing().when(fileParserStrategyMock).parse(anyString());
		when(fileParserStrategyMock.isError()).thenReturn(true);
		
		sut.parse("file name");
	}
	
	@Test
	public void formatAuthCode_authCodeLengthLessThan6Char_addPrefix(){
		String result = sut.formatAuthCode("1234");
		assertEquals(6, result.length());
	}
	
	@Test
	public void formatAuthCode_authCodeLengthIsMoreThan6_authCodeLengthRemains(){
		String testData = "12345678";
		
		String result = sut.formatAuthCode(testData);
		
		assertEquals(testData.length(), result.length());
	}
	
	@Test
	public void formatAuthCode_authCodeNullString_returnsNull(){
		String testData = "null";
		
		String result = sut.formatAuthCode(testData);
		
		assertEquals(null, result);
	}
	
	@Test
	public void formatAuthCode_authCodeNull_returnsNull(){
		String testData = null;
		
		String result = sut.formatAuthCode(testData);
		
		assertEquals(null, result);
	}

	@Test
	public void isMigsAlreadyUploaded_migsAlreadyUploaded_returnsTrue(){
		
		VenMigsUploadMaster migsUploadMaster = new VenMigsUploadMaster();
		
		List<VenMigsUploadMaster> singleResultMock = new ArrayList<VenMigsUploadMaster>();
		singleResultMock.add(migsUploadMaster);
		
		when(venMigsUploadMasterDAOMock.findByTransactionIdAuthCodeAndActionNotRemoved(anyString(), anyString())).thenReturn(singleResultMock);
		
		boolean result = sut.isMigsAlreadyUploaded("123", "123");
		
		assertTrue(result);
	}
	
	@Test
	public void isMigsAlreadyUploaded_migsNotYetUploaded_returnsFalse(){
		
		List<VenMigsUploadMaster> emptyResultMock = new ArrayList<VenMigsUploadMaster>();
		
		when(venMigsUploadMasterDAOMock.findByTransactionIdAuthCodeAndActionNotRemoved(anyString(), anyString())).thenReturn(emptyResultMock);
		
		boolean result = sut.isMigsAlreadyUploaded("123", "123");
		
		assertFalse(result);
	}
	
	@Test
	public void isMigsResponseCodeApproved_responceCodeApproved_returnsTrue(){
		String approveCode = "0 - Approved";
		
		boolean result = sut.isMigsResponseCodeApproved(approveCode);
		
		assertTrue(result);
	}

	@Test
	public void isMigsResponseCodeApproved_responceCodeNotApproved_returnsFalse(){
		String approveCode = "";
		
		boolean result = sut.isMigsResponseCodeApproved(approveCode);
		
		assertFalse(result);
	}
	
	@Test
	public void isMerchantTransactionRefEmpty_empty_returnsTrue(){
		VenMigsUploadTemporary merchantTransactionRefEmpty = new VenMigsUploadTemporary();
		
		boolean result = sut.isMerchantTransactionRefEmpty(merchantTransactionRefEmpty);
		
		assertTrue(result);
	}
	
	@Test
	public void isMerchantTransactionRefEmpty_notEmpty_returnsFalse(){
		VenMigsUploadTemporary merchantTransactionRefNotEmpty = new VenMigsUploadTemporary();
		merchantTransactionRefNotEmpty.setMerchantTransactionReference("123");
		
		boolean result = sut.isMerchantTransactionRefEmpty(merchantTransactionRefNotEmpty);
		
		assertFalse(result);
	}

	@Test
	public void isAuthCodeEmpty_empty_returnsTrue(){
		VenMigsUploadTemporary authCodeEmpty = new VenMigsUploadTemporary();
		
		boolean result = sut.isAuthCodeEmpty(authCodeEmpty);
		
		assertTrue(result);
	}

	@Test
	public void isAuthCodeEmpty_notEmpty_returnsFalse(){
		VenMigsUploadTemporary authCodeNotEmpty = new VenMigsUploadTemporary();
		
		boolean result = sut.isAuthCodeEmpty(authCodeNotEmpty);
		
		assertTrue(result);
	}
	
	@Test
	public void isCardNumberEmpty_empty_returnsTrue(){
		VenMigsUploadTemporary cardNumberEmpty = new VenMigsUploadTemporary();
		
		boolean result = sut.isCardNumberEmpty(cardNumberEmpty);
		
		assertTrue(result);
	}
	
	@Test
	public void isCardNumberEmpty_notEmpty_returnsFalse(){
		VenMigsUploadTemporary cardNumberNotEmpty = new VenMigsUploadTemporary();
		cardNumberNotEmpty.setCardNumber("123456875687");
		
		boolean result = sut.isCardNumberEmpty(cardNumberNotEmpty);
		
		assertFalse(result);
	}
	
	@Test
	public void isMigsDataIncomplete_merchantTransactionReferenceIsEmptyButAuthCodeAndCardNumberAreNotEmpty_returnsTrue(){
		VenMigsUploadTemporary merchantTransRefEmpty = new VenMigsUploadTemporary();
		merchantTransRefEmpty.setAuthorisationCode("1234");
		merchantTransRefEmpty.setCardNumber("123456877890");
		
		boolean result = sut.isMigsDataIncomplete(merchantTransRefEmpty);
		assertTrue(result);
	}
	
	@Test
	public void isMigsDataIncomplete_authCodeIsEmptyButMerchantTransactionReferenceAndCardNumberAreNotEmpty_returnsTrue(){
		VenMigsUploadTemporary authCodeEmpty = new VenMigsUploadTemporary();
		authCodeEmpty.setMerchantTransactionReference("1234");
		authCodeEmpty.setCardNumber("123456877890");
		
		boolean result = sut.isMigsDataIncomplete(authCodeEmpty);
		assertTrue(result);
	}
	
	@Test
	public void getIncompleteDataNote_merchantTransactionReferenceIsEmptyButAuthCodeAndCardNumberAreNotEmpty_returns1Note(){
		VenMigsUploadTemporary merchantTransRefEmpty = new VenMigsUploadTemporary();
		merchantTransRefEmpty.setAuthorisationCode("1234");
		merchantTransRefEmpty.setCardNumber("123456877890");
		
		List<String> result = sut.getIncompleteDataNote(merchantTransRefEmpty);
		
		assertEquals(1, result.size());
	}
	
	@Test
	public void getIncompleteDataNote_merchantTransactionReferenceAndAuthCodeAreEmptyButCardNumberIsNotEmpty_returns2Notes(){
		VenMigsUploadTemporary merchantTransRefAndAuthCodeEmpty = new VenMigsUploadTemporary();
		merchantTransRefAndAuthCodeEmpty.setCardNumber("123456877890");
		
		List<String> result = sut.getIncompleteDataNote(merchantTransRefAndAuthCodeEmpty);
		
		assertEquals(2, result.size());
	}
	
	@Test
	public void getIncompleteDataNote_merchantTransactionReferenceAndAuthCodeAndCardNumberAreEmpty_returns3Notes(){
		VenMigsUploadTemporary merchantTransRefEmptyAndAuthCodeAndCardNumberEmpty = new VenMigsUploadTemporary();
		
		List<String> result = sut.getIncompleteDataNote(merchantTransRefEmptyAndAuthCodeAndCardNumberEmpty);
		
		assertEquals(3, result.size());
	}
	
	@Test
	public void getWcsOrderIdFromMerchantTransactionReference_merchantTransactionRefWithSuffixSeparatedWithDash_returnsWcsOrderId(){
		String merchantTransactionRefWithSuffixSeparatedWithDash = "12345-2";
		
		String result = sut.getWcsOrderIdFromMerchantTransactionReference(merchantTransactionRefWithSuffixSeparatedWithDash);
		
		assertEquals("12345", result);
	}
	
	@Test
	public void getWcsOrderIdFromMerchantTransactionReference_merchantTransactionEmpty_returnsEmptyString(){
		String merchantTransactionRefWithSuffixSeparatedWithDash = "";
		
		String result = sut.getWcsOrderIdFromMerchantTransactionReference(merchantTransactionRefWithSuffixSeparatedWithDash);
		
		assertEquals("", result);
	}
	
	@Test
	public void isOrderRelatedToPaymentExist_orderWithSpecifiedMerchantTransactionRefAndAuthCodeIsFound_returnsTrue(){
		when(venOrderPaymentAllocationDAOMock.countByWcsOrderIdAndPaymentRef(anyString(), anyString())).thenReturn(1);
		
		boolean result = sut.isOrderRelatedToPaymentExist("12345", "2345");
		
		assertTrue(result);
	}

	@Test
	public void isOrderRelatedToPaymentExist_orderWithSpecifiedMerchantTransactionRefAndAuthCodeIsNotFound_returnsFalse(){
		when(venOrderPaymentAllocationDAOMock.countByWcsOrderIdAndPaymentRef(anyString(), anyString())).thenReturn(0);
		
		boolean result = sut.isOrderRelatedToPaymentExist("12345", "2345");
		
		assertFalse(result);
	}
	
	@After
	public void shutdown(){
		fileParserStrategyMock = null;
		venMigsUploadMasterDAOMock = null;
		venOrderPaymentAllocationDAOMock = null;
		sut = null;
		
		CommonUtil.veniceEnv = VeniceEnvironment.PRODUCTION;
	}

}
