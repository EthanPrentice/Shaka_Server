package adt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;


public class HttpGET extends HttpGet {
	
	public HttpGET (String url) {
		super(url);
	}
	
	public HttpGET (String url, NameValuePair... params) throws URISyntaxException {
		super(url);
		addParams(params);
	}
	
	public void addParams (NameValuePair... params) throws URISyntaxException {
		URIBuilder ub = new URIBuilder(getURI());
		ub.setParameters(Arrays.asList(params));
		setURI(ub.build());
	}
	
	public void removeParams () throws URISyntaxException {
		URIBuilder ub = new URIBuilder(getURI());
		ub.clearParameters();
		setURI(ub.build());
	}
	
	public void addHeaders (NameValuePair... headers) {
		for (NameValuePair header : headers) {
			// Overwrite old headers.
			if (containsHeader(header.getName())) {
				super.removeHeaders(header.getName());
			}
			super.addHeader(header.getName(), header.getValue());
		}
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
