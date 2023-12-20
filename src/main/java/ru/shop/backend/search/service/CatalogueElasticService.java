package ru.shop.backend.search.service;

import org.springframework.data.domain.Pageable;
import ru.shop.backend.search.model.CatalogueElastic;

import java.util.List;

public interface CatalogueElasticService {
    List<CatalogueElastic> getAll(String text);
    List<CatalogueElastic> getAll(String text, Pageable pageable);
    List<CatalogueElastic> getByName(String num);
    List<CatalogueElastic> getByItemId(String itemId);
    List<CatalogueElastic> getAllFull(String text);
}
