package com.marvel.challenge;

import java.io.IOException;

import com.marvel.challenge.service.*;


public class MarvelChallengeFactory {


	public static MarvelService getInstance( String[] args ) throws IOException {

		if ( args.length == 0 ) {
			System.out.println( "Please provide a Marvel command" );
		}

		if ( args.length == 1 && "characters".equals( args[0] ) ) {
			return new MarvelCharactersService( new ApacheHttpClient() );
		}

		if ( args.length == 2 && "characters".equals( args[0] ) ) {
			return new MarvelCharactersService( new ApacheHttpClient(), args[1] );
		}

		if ( args.length == 2 && "powers".equals( args[0] ) ) {
			return new MarvelCharactersService( new ApacheHttpClient(), args[1], true );
		}

		// Send back anonymouns class which is instance of Interface to satisfy return type. Interface Default method will be called
		return new MarvelService() {  };

	}	// End of getInstance() method...


}

