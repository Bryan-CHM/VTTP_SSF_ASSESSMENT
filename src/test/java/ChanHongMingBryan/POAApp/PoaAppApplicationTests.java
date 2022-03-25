package ChanHongMingBryan.POAApp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.web.client.HttpClientErrorException;

import ChanHongMingBryan.POAApp.services.QuotationService;

@SpringBootTest
class PoaAppApplicationTests {

	@Autowired
	QuotationService QuotationSvc;


	@Test
	void contextLoads() {
		List<String> fruits = new LinkedList<>();
		fruits.add("durian");
		fruits.add("plum");
		fruits.add("pear");

		try {
			QuotationSvc.getQuotations(fruits);
		} catch (HttpClientErrorException expectedException) {
			// Checks expeceted status code and message
			assertTrue(expectedException.getRawStatusCode() >= 400);
			assertEquals("400 Bad Request: \"{\"error\":\"Unknown items: plum\"}\"" ,expectedException.getLocalizedMessage());
		}

		
	}

}
