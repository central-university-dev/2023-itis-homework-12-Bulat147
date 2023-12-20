package ru.shop.backend.search.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.shop.backend.search.dto.SearchResult;
import ru.shop.backend.search.dto.SearchResultElastic;
import ru.shop.backend.search.model.CatalogueElastic;
import ru.shop.backend.search.model.Category;
import ru.shop.backend.search.model.TypeHelpText;
import ru.shop.backend.search.model.TypeOfQuery;
import ru.shop.backend.search.model.documents.ItemElastic;
import ru.shop.backend.search.model.entities.Item;
import ru.shop.backend.search.repository.ItemDbRepository;
import ru.shop.backend.search.service.CatalogueElasticService;
import ru.shop.backend.search.service.SearchService;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {


    private final ItemDbRepository dbRepository;
    private final CatalogueElasticService catalogueElasticService;
    private static Pattern pattern = Pattern.compile("\\d+");

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    public synchronized SearchResult getSearchResult(String text, Integer regionId) {
        List<CatalogueElastic> result = null;
        if (isNumeric(text)) {
            Integer itemId = dbRepository.findBySku(text).stream().findFirst().orElse(null);
            if (itemId == null) {
                var catalogue = catalogueElasticService.getByName(text);
                if (!catalogue.isEmpty()) {
                    result = catalogue;
                }
            }
            try {
                result = catalogueElasticService.getByItemId(itemId.toString());
            } catch (Exception e) {
            }
        }
        if (result == null) {
            result = catalogueElasticService.getAll(text);
        }
        List<Item> items = dbRepository.findByIds(regionId,
                        result.stream()
                                .flatMap(category -> category.getItems().stream())
                                .map(ItemElastic::getItemId)
                                .collect(Collectors.toList())
                ).stream()
                .map(arr -> new Item(((BigInteger) arr[2]).intValue(),
                        arr[1].toString(),
                        arr[3].toString(),
                        arr[4].toString(),
                        ((BigInteger) arr[0]).intValue(),
                        arr[5].toString()))
                .collect(Collectors.toList());
        Set<String> catUrls = new HashSet<>();
        String brand = null;
        if (!result.isEmpty())
            brand = result.get(0).getBrand();
        if (brand == null) {
            brand = "";
        }
        brand = brand.toLowerCase(Locale.ROOT);
        String finalBrand = brand;
        List<Category> categories = dbRepository.findCatsByIds(
                        items.stream()
                                .map(Item::getItemId)
                                .collect(Collectors.toList())).stream()
                .map(arr ->
                {
                    if (catUrls.contains(arr[2].toString()))
                        return null;
                    catUrls.add(arr[2].toString());
                    return
                            new Category(arr[0].toString(),
                                    arr[1].toString(),
                                    "/cat/" + arr[2].toString() + (finalBrand.isEmpty() ? "" : "/brands/" + finalBrand),
                                    "/cat/" + arr[3].toString(), arr[4] == null ? null : arr[4].toString());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new SearchResult(
                items,
                categories,
                !result.isEmpty() ? (List.of(new TypeHelpText(TypeOfQuery.SEE_ALSO,
                        ((result.get(0).getItems().get(0).getType() != null ?
                                result.get(0).getItems().get(0).getType() : "") + " "
                                + (result.get(0).getBrand() != null ? result.get(0).getBrand() : "")).trim())))
                        : new ArrayList<>()
        );
    }

    public synchronized SearchResultElastic getSearchResultElastic(String text, int regionId) {
        if (isNumeric(text)) {
            Integer itemId = dbRepository.findBySku(text).stream().findFirst().orElse(null);
            if (itemId == null) {
                var catalogue = catalogueElasticService.getByName(text);
                if (!catalogue.isEmpty()) {
                    return new SearchResultElastic(catalogue);
                }
                return new SearchResultElastic(catalogueElasticService.getAllFull(text));
            }
            try {
                return new SearchResultElastic(catalogueElasticService.getByItemId(itemId.toString()));
            } catch (Exception e) {

            }
        }
        return new SearchResultElastic(catalogueElasticService.getAllFull(text));
    }

}
