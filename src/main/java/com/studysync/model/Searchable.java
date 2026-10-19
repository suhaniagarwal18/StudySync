package com.studysync.model;

import java.util.List;

/**
 * Generic search contract implemented by searchable domain entities and services.
 * 
 * CONCEPT: Abstraction & Generic Interfaces
 * WHY: Decouples search query specifications from concrete retrieval implementations.
 * 
 * @param <T> the type of entity returned by the search
 */
public interface Searchable<T> {

    /**
     * Executes a search query based on a keyword or subject criteria.
     * 
     * @param criteria query string
     * @return list of matching items
     */
    List<T> search(String criteria);
}
