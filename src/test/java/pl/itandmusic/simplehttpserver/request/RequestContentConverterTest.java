package pl.itandmusic.simplehttpserver.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pl.itandmusic.simplehttpserver.model.HeaderNames;
import pl.itandmusic.simplehttpserver.model.HeaderValues;

public class RequestContentConverterTest {

	private static RequestContentConverter requestContentConverter;
	private List<String> content_1;
	private List<String> content_2;

	@BeforeClass
	public static void prepare() {
		requestContentConverter = new RequestContentConverter();
	}

	@Before
	public void prepareContent() {
		content_1 = new ArrayList<>();
		content_1.add("GET /test/test.do?test=test&test=wew HTTP/1.1");
		content_1.add("Host: wickedlysmart.com");
		content_1.add("Accept-Language: en-us, en;q=0.5");
		content_1.add("Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		
		
		
		content_2 = new ArrayList<>();
		content_2.add("POST /any/resource/on/server/test HTTP/2.0");
		// TODO the rest
	}

	@Test
	public void testProtocolExtracting() {
		String protocol = requestContentConverter.extractProtocol(content_1);
		assertEquals("HTTP/1.1", protocol);
		protocol = requestContentConverter.extractProtocol(content_2);
		assertEquals("HTTP/2.0", protocol);
	}

	@Test
	public void testURIExtracting() {
		URI uri = requestContentConverter.extractURI(content_1);
		assertNotNull(uri);
		assertEquals("/test/test.do", uri.toASCIIString());
		uri = requestContentConverter.extractURI(content_2);
		assertNotNull(uri);
		assertEquals("/any/resource/on/server/test", uri.toASCIIString());
	}
	
	@Test
	public void testURLExtracting() {
		//from /etc/hosts
		//127.0.0.1 localhost
		//127.0.1.1	damian-Lenovo-G780
		StringBuffer stringBuffer = requestContentConverter.extractURL(content_1);
		assertEquals("127.0.1.1/test/test.do", stringBuffer.toString());
		stringBuffer = requestContentConverter.extractURL(content_2);
		assertEquals("127.0.1.1/any/resource/on/server/test", stringBuffer.toString());
	}
	
	@Test
	public void testQueryStringExtracting() {
		String queryString = requestContentConverter.extractQueryString(content_1);
		assertEquals("/test/test.do?test=test&test=wew", queryString);
		queryString = requestContentConverter.extractQueryString(content_2);
		assertEquals("/any/resource/on/server/test", queryString);
	}
	
	@Test
	public void testHeadersExtracting() {
		Map<String, Enumeration<String>> headers = requestContentConverter.extractHeaders(content_1);
		assertTrue(headers.containsKey("Accept-Charset"));
		assertTrue(headers.containsKey("Accept-Language"));
		for(String key : headers.keySet()) {
			if(key.equals("Accept-Charset")) {
				HeaderValues headerValues = (HeaderValues)headers.get(key);
				int elementsCount = 0;
				while(headerValues.hasMoreElements()) {
					String value = headerValues.nextElement();
					assertTrue(value.equals("ISO-8859-1") || value.equals("utf-8;q=0.7") || value.equals("*;q=0.7"));
					elementsCount++;
				}
				assertEquals(3, elementsCount);
			}
			else if(key.equals("Accept-Language")) {
				HeaderValues headerValues = (HeaderValues)headers.get(key);
				int elementsCount = 0;
				while(headerValues.hasMoreElements()) {
					String value = headerValues.nextElement();
					assertTrue(value.equals("en-us") || value.equals("en;q=0.5"));
					elementsCount++; 
				}
				assertEquals(2, elementsCount);
			}
			
		}
	}
	
	public void testHeaderNamesExtracting() {
		Enumeration<String> headerNames = requestContentConverter.extractHeaderNames(content_1);
		
		List<String> headerNames_ = new ArrayList<>();
		
		while(headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headerNames_.add(headerName);
		}
		
		assertTrue(headerNames_.contains("Accept-Charset"));
		assertTrue(headerNames_.contains("Accept-Language"));
		assertTrue(headerNames_.contains("Host"));
		
	}
	
}
