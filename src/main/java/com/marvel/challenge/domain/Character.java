package com.marvel.challenge.domain;

import java.lang.Comparable;

import java.util.List;


public class Character implements Comparable<Character> {


	private int id;

	private String name;

	private ComicList comics;

	private StoryList stories;

	private String description;

	private Image thumbnail;

	private List<Url> urls;

	// Based on number of appearance in both Comics & Stories
	private int characterPopularity;



	public Character() {}


	// Getters & Setters

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public ComicList getComics() {
		return this.comics;
	}

	public StoryList getStories() {
		return this.stories;
	}

	public String getDescription() {
		return this.description;
	}

	public Image getThumbnail() {
		return this.thumbnail;
	}

	public List<Url> getUrls() {
		return this.urls;
	}

	public int getCharacterPopularity() {
		return this.characterPopularity;
	}


	public void setId( int id ) {
		this.id = id;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public void setComics( ComicList comics ) {
		this.comics = comics;
	}

	public void setStories( StoryList stories ) {
		this.stories = stories;
	}

	public void setDescription( String description ) {
		this.description = description;
	}

	public void setThumbnail( Image thumbnail ) {
		this.thumbnail = thumbnail;
	}

	public void setUrls( List<Url> urls ) {
		this.urls = urls;
	}

	public void setCharacterPopularity( int characterPopularity ) {
		this.characterPopularity = characterPopularity;
	}


	@Override
	public String toString() {
		return "-->Character ID: " + this.id + " Character Name: " + this.name + " Popularity: " + this.characterPopularity;
	}

	@Override
	public int compareTo( Character character ) {
		//return this.getCharacterPopularity() - character.getCharacterPopularity();
		return character.getCharacterPopularity() - this.getCharacterPopularity();
	}


}

