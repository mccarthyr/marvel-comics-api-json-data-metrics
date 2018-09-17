package com.marvel.challenge.service;

import java.io.IOException;

import java.util.List;

import com.marvel.challenge.domain.CharacterDataWrapper;
import com.marvel.challenge.domain.Character;


public abstract class MarvelApiData {


	public abstract String retrieveApiData() throws IOException;

	public abstract List<Character> parseApiData( CharacterDataWrapper characterWrapper ) throws IOException;

	public abstract void processApiData( List<Character> apiRetrievedCharactersList );

	public abstract void generateApiResponseMetrics();


}