package it.govhub.govregistry.api.test.utils;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;

import org.hamcrest.Matcher;
import org.hamcrest.collection.IsMapContaining;

public class Matchers {


	public static <K> Matcher<?> hasNullOrEmpty(K key) {
		return anyOf(
				not(	IsMapContaining.hasKey(key)),
				IsMapContaining.hasEntry(key, new ArrayList<>()));
	}
	
	
	private Matchers() {	}

	
}
