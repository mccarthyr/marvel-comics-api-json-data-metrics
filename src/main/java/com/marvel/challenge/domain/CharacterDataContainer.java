package com.marvel.challenge.domain;

import java.util.*;


public class CharacterDataContainer {


	private int limit;

	private int offset;

	private int total;

	private int count;

	private List<Character> results;



	// Getters & Setters

	public int getLimit() {
		return this.limit;
	}

	public int getOffset() {
		return this.offset;
	}

	public int getTotal() {
		return this.total;
	}

	public int getCount() {
		return this.count;
	}

	public List<Character> getResults() {
		return this.results;
	}


	public void setLimit( int limit ) {
		this.limit = limit;
	}

	public void setOffset( int offset ) {
		this.offset = offset;
	}

	public void setTotal( int total ) {
		this.total = total;
	}

	public void setCount( int count ) {
		this.count = count;
	}

	public void setResults( List<Character> results ) {
		this.results = results;
	}


	@Override
	public String toString() {
		return "THIS IS THE STRING REPRESENTATION OF THE CHARACTER-DATA-CONTAINER!";
	}


}

