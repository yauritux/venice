package com.gdn.venice.server.app.testrestdatasource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gdn.venice.facade.spring.fraud.migs.MigsUploadProcessorService;


@Controller
@RequestMapping("/SpringTestServlet")
public class SpringTestServlet {
	
	@Autowired
	MigsUploadProcessorService migsUploadProcessorService;
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String print(@RequestParam("transactionId") String transactionId, @RequestParam("authCode") String authCode){
		boolean result = migsUploadProcessorService.isMigsAlreadyUploaded(transactionId, authCode);
		
		if(result)
			return "exist";
		else
			return "not exist";
	}
	
}
