package com.marvel.challenge.service;

import java.io.IOException;

public interface HttpClient {


	public abstract String httpGet( String apiRequest ) throws IOException;


}