package ru.shop.backend.search.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.shop.backend.search.api.SearchApi;
import ru.shop.backend.search.dto.SearchResult;
import ru.shop.backend.search.dto.SearchResultElastic;
import ru.shop.backend.search.service.impl.SearchServiceImpl;

@RestController
@RequiredArgsConstructor
public class SearchController implements SearchApi {

    private final SearchServiceImpl service;

    public SearchResult find(String text, int regionId){
        return service.getSearchResult(text, regionId);
    }

    public SearchResultElastic finds(String text, int regionId) {
        return service.getSearchResultElastic(text, regionId);
    }

}
