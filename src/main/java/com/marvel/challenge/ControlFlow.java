package com.marvel.challenge;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.marvel.challenge.service.MarvelService;


public class ControlFlow {

	public void run( String[] args ) throws IOException {

		MarvelService ms = MarvelChallengeFactory.getInstance( args );

		ObjectNode marvelResult = ms.getMarvelResults();
		ObjectMapper mapper = new ObjectMapper();

		System.out.println( mapper.writerWithDefaultPrettyPrinter().writeValueAsString( marvelResult ) );		

	}

}


