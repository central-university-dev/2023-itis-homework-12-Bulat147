package ru.shop.backend.search.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@ToString
@Getter
public class SearchResultElastic {
    public List<CatalogueElastic> result;
}
