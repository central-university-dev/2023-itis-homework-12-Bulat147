package ru.shop.backend.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.shop.backend.search.model.CatalogueElastic;

import java.util.List;

@AllArgsConstructor
@ToString
@Getter
public class SearchResultElastic {
    public List<CatalogueElastic> result;
}
