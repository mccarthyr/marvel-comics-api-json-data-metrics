package com.marvel.challenge.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;


import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import org.apache.http.HttpResponse;


public class ApacheHttpClient implements HttpClient {


    // http://gateway.marvel.com/v1/public/characters?limit=100&offset=100&ts=1&apikey=86846b2057af63dbad0bd34c9b77e4cf&hash=c52a0c0bd177f89d350d76d4e1aa1633
	public String httpGet( String apiRequest ) throws IOException {

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet request = new HttpGet( apiRequest );

        CloseableHttpResponse response = httpclient.execute( request );

        System.out.println( "The Response Code is: " + response.getStatusLine().getStatusCode() );

        if ( response.getStatusLine().getStatusCode() != 200 ) {
            throw new IOException( "Invalid API Response Code" );
        }

        StringBuilder result = new StringBuilder();
        try {

            BufferedReader rd = new BufferedReader(
                new InputStreamReader( response.getEntity().getContent() ));

            String line = "";
            
            while ( ( line = rd.readLine() ) != null ) {
                result.append(line);
            }

            //System.out.println( "The returned response is: \n" + result );

        } finally {
            response.close();
        }

        return result.toString();

	}	// End of method httpGet()...


}	// End of class ApacheHttpClient...

