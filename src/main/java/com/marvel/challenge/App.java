package com.marvel.challenge;

import java.io.IOException;

public class App {

	// To run early prototype code: 
	// mvn exec:java -Dexec.args="characters"

	public static void main( String[] args ) throws IOException {
		//System.out.println( "Hello World!" );
		new ControlFlow().run( args );
	}



}
