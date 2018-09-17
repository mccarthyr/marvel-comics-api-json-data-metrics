package com.marvel.challenge.service;

import java.security.*;

import org.apache.commons.codec.binary.Hex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import com.marvel.challenge.domain.*;
import com.marvel.challenge.domain.Character;

import java.io.*;
import java.net.*;

import java.util.Properties;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.LongSummaryStatistics;
import java.util.stream.LongStream;
import java.util.IntSummaryStatistics;
import java.util.stream.*;

import java.time.Instant;
import java.time.Duration;


public class MarvelCharactersService extends MarvelApiData implements MarvelService  {


	private static final String CONFIG_STORE = "/config.properties";

	private static final int API_RESULTS_LIMIT = 100;

	private static final String NO_CHARACTER_POWERS = "No Powers are available for this character";

	private int apiResultsOffset = 0;

	private int numberOfAdditionalRequests = -1;	// USED TO INDICATE THE INITAL REQUEST

	private int total = 0;

	private List<Character> characters = new ArrayList<Character>();

	private int requestCounter = 0;

	private int characterAppearsInComic = 0;

	private int characterAppearsInStory = 0;

	private long totalResponsesByteSize = 0;

	private List<Long> apiResponseTimes = new ArrayList<>();

	private long totalResponseTime;

	private float averageResponseTime;

	private List<Integer> apiResponseByteSizes = new ArrayList<>();

	private float averageResponseByteSize;

	private List<Integer> processedTopTenCharacterIds = new ArrayList<>();

	private String characterId = "";

	private boolean extractCharacterPowers = false;

	private Map<String, String> powerRatings = new HashMap<>();

	private HttpClient httpClient; 



	public MarvelCharactersService( HttpClient httpClient ) {
		this.httpClient = httpClient;
	}

	// Overloaded Constructor
	public MarvelCharactersService( HttpClient httpClient, String characterId ) {

		this.httpClient = httpClient;
		this.characterId = characterId;
	}


	// Overloaded Constructor
	public MarvelCharactersService( HttpClient httpClient, String characterId, boolean extractCharacterPowers ) {

		this.httpClient = httpClient;
		this.characterId = characterId;
		this.extractCharacterPowers = extractCharacterPowers;
	}


	/** 
	 * Each Marvel API request requires a 
	 * ts - a timestamp (or other long string which can change on a request-by-request basis)
	 * public key
	 * hash - a md5 digest of the ts parameter, your private key and your public key 
	 *        (e.g. md5(ts+privateKey+publicKey)
	 *
	 */
	private String generateApiRequestWithAuthenticationCode( int limit, int offset ) {

		// http://gateway.marvel.com/v1/public/characters?limit=100&offset=100&ts=1&apikey=86846b2057af63dbad0bd34c9b77e4cf&hash=c52a0c0bd177f89d350d76d4e1aa1633
		String apiRequest = "";

		try {

			final Properties properties = new Properties();
			properties.load(this.getClass().getResourceAsStream( CONFIG_STORE ));

			String publicKey = properties.getProperty( "marvel.api.key.public" );
			String privateKey = properties.getProperty( "marvel.api.key.private" );
			String ts = properties.getProperty( "marvel.api.ts" );

			String apiEndpoint = properties.getProperty( "marvel.api.base.endpoint" );
			//String apiResource = properties.getProperty( "marvel.api.endpoint.resources." + apiEndpointResource );

			String apiResource = properties.getProperty( "marvel.api.endpoint.resources.characters"  );
			if ( !this.characterId.isEmpty() ) {
				apiResource = apiResource + "/" + this.characterId;
			}

			// Generating the hascode
			String message = ts + privateKey + publicKey;

			byte[] messageBytes = message.getBytes();

			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest( messageBytes );

			final String hashCode = new String(Hex.encodeHex(digest));

			apiRequest = apiEndpoint + apiResource + "?limit=" + limit + "&offset=" + offset + "&ts=" + ts + "&apikey=" + publicKey +"&hash=" + hashCode;

		} catch ( IOException | NoSuchAlgorithmException ioe ) { ioe.printStackTrace(); }

		return apiRequest;

	}	// End of method generateApiRequestAuthenticationCode()...



	private int calculateResponseByteSize( String marvelApiResponse ) throws UnsupportedEncodingException {
		
		final byte[] apiResponseBytes = marvelApiResponse.getBytes( "UTF-16" );
		return apiResponseBytes.length;

	}	// End of method calculateResponseByteSize()...



	@Override
	public  String retrieveApiData() throws IOException {

		// Initial call sends with OFFSET of 0
		String apiRequest = generateApiRequestWithAuthenticationCode( API_RESULTS_LIMIT, this.apiResultsOffset  );

		//System.out.println( "===> THE REQUESTED URL IS: " + apiRequest );

		Instant start = Instant.now();

		String marvelApiResponse = this.httpClient.httpGet( apiRequest );

		Instant finish = Instant.now();	
		long timeElapsed = Duration.between( start, finish ).toMillis();

		this.apiResponseTimes.add( timeElapsed );
		this.apiResponseByteSizes.add( this.calculateResponseByteSize( marvelApiResponse ) );

		return marvelApiResponse;

	}	// End of method retrieveApiData()...



	public CharacterDataWrapper parseApiWrapperData( String apiData ) throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

		CharacterDataWrapper characterWrapper = new CharacterDataWrapper();
		try {

			characterWrapper = objectMapper.readValue( apiData, CharacterDataWrapper.class );
			this.total = characterWrapper.getData().getTotal();
			
			// The -1 near the end is because 1 original request was already executed to get this data!
			this.numberOfAdditionalRequests = ( total > API_RESULTS_LIMIT ) ? total / API_RESULTS_LIMIT + ( ( total % API_RESULTS_LIMIT == 0 ) ? 0 : 1 ) -1 : 0;

		} catch (  MalformedURLException e ) { e.printStackTrace(); }

		return characterWrapper;

	} // End of method parseApiWrapperData()...



	public CharacterDataWrapper mapApiDataToObjects( String apiData ) throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

		CharacterDataWrapper characterDataWrapper = new CharacterDataWrapper();
		try {

			characterDataWrapper = objectMapper.readValue( apiData, CharacterDataWrapper.class );

		} catch (  MalformedURLException e ) { e.printStackTrace(); }

		return characterDataWrapper;

	}	// End of method mapApiDataToObjects()...



	@Override
	public  List<Character> parseApiData( CharacterDataWrapper characterWrapper ) throws IOException {

		// Counting from zero so add 1 for natural display count
		System.out.println( "Completed API Request Number: " + ( this.requestCounter + 1 ) );	
	
		this.requestCounter++;

		if ( this.numberOfAdditionalRequests > 0 ) {

			this.characters.addAll( characterWrapper.getData().getResults() );
			this.apiResultsOffset += 100; // It started at zero and add 100 to it after each request
			
			while ( this.requestCounter <= this.numberOfAdditionalRequests ) {

				String apiData = this.retrieveApiData();
				CharacterDataWrapper characterDataWrapper = this.mapApiDataToObjects( apiData );
				this.parseApiData( characterDataWrapper );				
			}

		} else {
			this.characters = characterWrapper.getData().getResults();	// WHEN THERE IS ONLY 1 REQUEST...
		}

		return this.characters;

	}	// End of metho parseApiData()...



	public void processApiData( List<Character> apiRetrievedCharactersList ) {

		this.processedTopTenCharacterIds = apiRetrievedCharactersList.stream()
			.map( c -> {
				c.setCharacterPopularity( ( c.getComics().getAvailable() + c.getStories().getAvailable() ) );
				if ( c.getComics().getAvailable() > 0 ) this.characterAppearsInComic++;
				if ( c.getStories().getAvailable() > 0 ) this.characterAppearsInStory++;
				return c.getId();
			}  )
			.sorted()
			.limit( 10 )
			.collect( Collectors.toList() );

	}	// End of method processApiData


	public void generateApiResponseMetrics() {

		 LongSummaryStatistics stats = this.apiResponseTimes.stream().collect(
		            Collectors.summarizingLong(Long::longValue));

		 this.totalResponseTime = stats.getSum();
		 this.averageResponseTime = (float) stats.getAverage();

		IntSummaryStatistics responseSizeStats = this.apiResponseByteSizes
				.stream()
				.mapToInt( Integer::intValue )
				.summaryStatistics();

		this.totalResponsesByteSize = responseSizeStats.getSum();
		this.averageResponseByteSize = (float) responseSizeStats.getAverage();

	}	// End of method generateApiResponseMetrics()...


	/**
	 * Creates a JSON Object that contains 2 Json Objects.
	 * 1 for "characters" and 1 for "requests".
	 * The "characters" Json Object also contains an Array Object.
	 */
	private ObjectNode createCharactersResponseJson() throws JsonProcessingException {

 		ObjectMapper mapper = new ObjectMapper();

		ObjectNode containerObject = mapper.createObjectNode();

		ArrayNode topTenCharactersArrray = mapper.createArrayNode();

	  for ( Integer i : this.processedTopTenCharacterIds ) {
	  	topTenCharactersArrray.add( i.intValue() );
	  }
	  // [1009144,1009146,1009148,1009149,1009150,1009151,1009152,1009153,1009154,1009156]

		ObjectNode charactersObject = mapper.createObjectNode();
		charactersObject.put( "ids", topTenCharactersArrray );
		charactersObject.put( "total", this.total );
		charactersObject.put( "total_in_comics", this.characterAppearsInComic );
		charactersObject.put( "total_in_stories", this.characterAppearsInStory );

		containerObject.put( "characters", charactersObject );

		containerObject.put( "requests", this.generateRequestResponseMetricsJson() );

		return containerObject;

	}	// End of method createCharactersResponseJson()...


	private ObjectNode createSpecifiedCharacterResponseJson() throws JsonProcessingException {

 		ObjectMapper mapper = new ObjectMapper();	

		ObjectNode containerObject = mapper.createObjectNode();
		
		// There is only 1 chacter in the stored instance List<Character>
		ObjectNode charactersObject = mapper.createObjectNode();
		charactersObject.put( "id", this.characters.get(0).getId() );
		charactersObject.put( "name", this.characters.get(0).getName() );
		charactersObject.put( "description", this.characters.get(0).getDescription() );
		charactersObject.put( "thumbnail", this.characters.get(0).getThumbnail().getPath() );

		// If Character Powers were requested
		if ( this.extractCharacterPowers ) {
				
			charactersObject.put( "durability", this.powerRatings.get( "durability" ) );
			charactersObject.put( "energy", this.powerRatings.get( "energy" ) );
			charactersObject.put( "fighting_skills", this.powerRatings.get( "fighting skills" ) );
			charactersObject.put( "intelligence", this.powerRatings.get( "intelligence" ) );
			charactersObject.put( "speed", this.powerRatings.get( "speed" ) );
			charactersObject.put( "strength", this.powerRatings.get( "strength" ) );	
		}

		containerObject.put( "characters", charactersObject );
		containerObject.put( "requests", this.generateRequestResponseMetricsJson() );


		return containerObject;		

	}	// End of method createSpecifiedCharacterResponseJson()...



	private Optional<String> extractCharacterPowersFromWiki() throws IOException {

		// Get the wiki for the requested character
		String url = "";
		
		List<Url> requestedCharacterUrls = this.characters.get(0).getUrls();

		for ( Url urlData : requestedCharacterUrls ) {
			if ( ( urlData.getType().equals("detail") ) || ( urlData.getType().equals("wiki") ) ) {
				url = urlData.getUrl();
			}
		}

		if ( url.isEmpty() ) {
			return Optional.of( NO_CHARACTER_POWERS );
		}


		// Extract the character powers from the url
		Document doc = Jsoup.connect( url ).get();
		Elements e = doc.getElementsByClass("power-circle");

		if ( null != e.first() ) {

			for ( Element element : e ) {
				// Placing in HashMap, Key on power and Value on rating
				this.powerRatings.put( element.siblingElements().get(0).text(), element.text() );
			}

		} else{
			// Some Wiki pages do not contain a power rating section
			return Optional.of( NO_CHARACTER_POWERS );
		}

		// Printing the power ratings map entries
		//powerRatings.forEach( (k,v) -> System.out.println("KEY is: " + k + " VALUE is: " + v) );

		return Optional.empty();

	}	// End of method extractCharacterPowersFromWiki()...



	private ObjectNode generateRequestResponseMetricsJson() throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();

		// Put together the response metrics...
		ObjectNode requestsMetrics = mapper.createObjectNode();
		requestsMetrics.put( "total", ( this.numberOfAdditionalRequests + 1 ) );
		requestsMetrics.put( "total_response_time", this.totalResponseTime );
		requestsMetrics.put( "average_response_time", this.averageResponseTime );
		requestsMetrics.put( "total_response_size", this.totalResponsesByteSize );
		requestsMetrics.put( "average_response_size", this.averageResponseByteSize );

		return requestsMetrics;

	}	// End of method generateRequestResponseMetricsJson()...



	@Override
	public ObjectNode getMarvelResults() throws IOException {

		String apiData = "";
		CharacterDataWrapper characterDataWrapper = new CharacterDataWrapper();

		try {

			apiData = this.retrieveApiData();
			characterDataWrapper =  parseApiWrapperData( apiData );

		} catch ( IOException ioe ) { ioe.printStackTrace(); }

		List<Character> apiRetrievedCharactersList = parseApiData( characterDataWrapper );

		this.processApiData( apiRetrievedCharactersList );
		this.generateApiResponseMetrics();

		if ( this.extractCharacterPowers ) {

			Optional<String> optional = this.extractCharacterPowersFromWiki();
			// If true then there are no power available as returned String message
			if ( optional.isPresent() ) {
				// Display the message that no powers are available
				System.out.println( optional.get() );
			}

		}

		return ( this.characterId.isEmpty() ) ? this.createCharactersResponseJson() : this.createSpecifiedCharacterResponseJson();


	}	// End of method getMarvelResults()...



}	// End of class MarvelCharacterService...