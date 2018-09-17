package com.marvel.challenge.service;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface MarvelService {

	public default ObjectNode getMarvelResults() throws IOException {
		return new ObjectMapper().createObjectNode();
	}

}

