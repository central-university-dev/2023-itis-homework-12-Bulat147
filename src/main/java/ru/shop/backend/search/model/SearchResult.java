package ru.shop.backend.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {
    private List<Item> items;
    private List<Category> categories;
    private List<TypeHelpText> typeQueries;
}
