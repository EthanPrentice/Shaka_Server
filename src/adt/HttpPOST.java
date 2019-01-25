/**
 * @author Ethan Prentice
 * 
 */


package adt;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;

public class HttpPOST extends HttpPost {
	
	public HttpPOST(String url) {
		super(url);
	}
	
	public HttpPOST(String url, String body) throws UnsupportedEncodingException {
		super(url);
		setBody(body);
	}
	
	public HttpPOST(String url, NameValuePair... params) throws UnsupportedEncodingException {
		super(url);
		setParams(params);
	}
	
	public HttpPOST setParams (NameValuePair... params) throws UnsupportedEncodingException {
		setEntity(new UrlEncodedFormEntity(Arrays.asList(params)));
		return this;
	}
	
	public HttpPOST setBody (String body) throws UnsupportedEncodingException {
		setEntity(new ByteArrayEntity(body.getBytes()));
		return this;
	}
	
	public HttpPOST addHeaders (NameValuePair... headers) {
		for (NameValuePair header : headers) {
			// Overwrite old headers.
			if (containsHeader(header.getName())) {
				super.removeHeaders(header.getName());
			}
			super.addHeader(header.getName(), header.getValue());
		}
		return this;
	}
	
	public HttpPOST removeHeaders(String... headers) {
		for (String s : headers) {
			if (containsHeader(s)) {
				super.removeHeaders(s);
			}
		}
		return this;
	}
	
	public String execute() throws IOException {
		// Execute and get the response.
		HttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = httpClient.execute(this);
		HttpEntity entity = response.getEntity();

		BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
		String inputLine;
		
		StringBuffer sBuffer = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			sBuffer.append(inputLine);
		}
		in.close();
		
		return sBuffer.toString();
	}

}
