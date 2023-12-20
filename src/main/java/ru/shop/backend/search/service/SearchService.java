package ru.shop.backend.search.service;

import ru.shop.backend.search.dto.SearchResult;
import ru.shop.backend.search.dto.SearchResultElastic;

public interface SearchService {
    SearchResult getSearchResult(String text, Integer regionId);
    SearchResultElastic getSearchResultElastic(String text, int regionId);

}
