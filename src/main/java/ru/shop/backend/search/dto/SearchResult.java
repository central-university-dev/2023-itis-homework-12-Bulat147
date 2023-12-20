package ru.shop.backend.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.shop.backend.search.model.Category;
import ru.shop.backend.search.model.entities.Item;
import ru.shop.backend.search.model.TypeHelpText;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {
    private List<Item> items;
    private List<Category> categories;
    private List<TypeHelpText> typeQueries;
}
