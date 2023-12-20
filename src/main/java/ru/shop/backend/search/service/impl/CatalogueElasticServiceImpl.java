package ru.shop.backend.search.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.shop.backend.search.model.CatalogueElastic;
import ru.shop.backend.search.model.documents.ItemElastic;
import ru.shop.backend.search.repository.ItemElasticRepository;
import ru.shop.backend.search.service.CatalogueElasticService;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ru.shop.backend.search.service.MessageConverter.*;

@Service
@RequiredArgsConstructor
public class CatalogueElasticServiceImpl implements CatalogueElasticService {

    private final ItemElasticRepository elasticRepository;
    private Pageable pageable = PageRequest.of(0, 150);
    private Pageable pageableSmall = PageRequest.of(0, 10);

    public synchronized List<CatalogueElastic> getAll(String text) {
        return getAll(text, pageableSmall);
    }

    public List<CatalogueElastic> getAll(String text, Pageable pageable) {
        String type = "";
        List<ItemElastic> list;
        String brand = "";
        String text2 = text;
        Long catalogueId = null;
        boolean needConvert = true;
        // Проверка на наличие левых символов - [ ] | и тд
        // Если имеются - то конверт выполняется только здесь, а дальше нет,
        // иначе - будем конвертировать, чтобы проверить оба варианта
        if (isContainErrorChar(text)) {
            text = convert(text);
            needConvert = false;
        }
        // Если после конверта будет содержать лишние символы - то не нужно вообще в дальнейшем конвертировать
        if (needConvert && isContainErrorChar(convert(text))) {
            needConvert = false;
        }

        // ищем бренд
        if (text.contains(" "))
            // бежим по отдельным словам и ищем список вещей по их бренду, считая нынешнее слово - названием бренда
            // если есть такие вещи, то берем первый и достаем у него настоящее название бренда
            // при этом откидываем из text слово соответствующее бренду (будем разбирать по оставшейся части остальное)
            for (String queryWord : text.split("\\s")) {
                list = elasticRepository.findAllByBrand(queryWord, pageable);
                if (list.isEmpty() && needConvert) {
                    list = elasticRepository.findAllByBrand(convert(text), pageable);
                }
                if (!list.isEmpty()) {
                    text = text.replace(queryWord, "").trim().replace("  ", " ");
                    brand = list.get(0).getBrand();
                    break;
                }
            }

        // ищем тип по всем оставшимся словам в text
        list = elasticRepository.findAllByType(text, pageable);
        if (list.isEmpty() && needConvert) {
            list = elasticRepository.findAllByType(convert(text), pageable);
        }
        if (!list.isEmpty()) { // находим тип с минимальным соответсвующим названием
            // а мб здесь нужно убрать все слова, если найдено
            type = list.stream()
                    .map(ItemElastic::getType)
                    .min(Comparator.comparingInt(String::length))
                    .get();
            // а если не нашлось - ищем по каждому отдельному слову в text
        } else {
            for (String queryWord : text.split("\\s")) {
                list = elasticRepository.findAllByType(queryWord, pageable);
                if (list.isEmpty() && needConvert) {
                    list = elasticRepository.findAllByType(convert(text), pageable);
                }
                if (!list.isEmpty()) {
                    text = text.replace(queryWord, "");
                    type = list.stream()
                            .map(ItemElastic::getType)
                            .min(Comparator.comparingInt(String::length))
                            .get();
                    // Хм, без брейка - смапит последний совподающий под слово тип
                    // и удалит все подходящие под тип слова из text
                }
            }
        }
        // ищем каталог по всему оставшемуся в text, если бренд не был найден
        // у каталога есть бренд - соответсвенно можно подцепить бренд, получив каталог
        if (brand.isEmpty()) {
            list = elasticRepository.findByCatalogue(text, pageable);
            if (list.isEmpty() && needConvert) {
                list = elasticRepository.findByCatalogue(convert(text), pageable);
            }
            if (!list.isEmpty()) {
                catalogueId = list.get(0).getCatalogueId();
            }
        }

        text = text.trim();
        if (text.isEmpty() && !brand.isEmpty())
            // Но нужно же достать вещи данного каталога и бренда и выдать - почему null
            return Collections.singletonList(new CatalogueElastic(list.get(0).getCatalogue(),
                    list.get(0).getCatalogueId(),
                    null,
                    brand));

        // логика по поиску на основании наличия или отсутствия типа, бренда, категории
        text += "?";
        if (brand.isEmpty()) {
            if (catalogueId == null)
                if (type.isEmpty()) {
                    list = elasticRepository.find(text, pageable);
                    if (list.isEmpty()) {
                        list = elasticRepository.find(convert(text), pageable);
                    }
                } else {
                    type += "?";
                    list = elasticRepository.findAllByType(text, type, pageable);
                    if (list.isEmpty()) {
                        list = elasticRepository.findAllByType(convert(text), type, pageable);
                    }
                }
            else if (!type.isEmpty()) {
                type += "?";
                list = elasticRepository.find(text, catalogueId, type, pageable);
                if (list.isEmpty()) {
                    list = elasticRepository.find(convert(text), catalogueId, type, pageable);
                }
            } else {
                list = elasticRepository.find(text, catalogueId, pageable);
                if (list.isEmpty()) {
                    list = elasticRepository.find(convert(text), catalogueId, pageable);
                }
            }
        } else {
            if (type.isEmpty()) {
                list = elasticRepository.findAllByBrand(text, brand, pageable);
                if (list.isEmpty()) {
                    list = elasticRepository.findAllByBrand(convert(text), brand, pageable);
                }
            } else {
                type += "?";
                list = elasticRepository.findAllByTypeAndBrand(text, brand, type, pageable);
                if (list.isEmpty()) {
                    list = elasticRepository.findAllByTypeAndBrand(convert(text), brand, type, pageable);
                }
            }
        }

        if (list.isEmpty()) {
            text2 += "?";
            list = elasticRepository.findAllNotStrong(text2, pageable);
            if (list.isEmpty() && needConvert) {
                list = elasticRepository.findAllNotStrong(convert(text2), pageable);
            }
        }
        return get(list, text, brand);
    }

    private List<CatalogueElastic> get(List<ItemElastic> list, String name, String brand) {
        Map<String, List<ItemElastic>> map = new HashMap<>();
        AtomicReference<ItemElastic> searchedItem = new AtomicReference<>();
        list.forEach(
                i ->
                {
                    if (name.replace("?", "").equals(i.getName())) {
                        searchedItem.set(i);
                    }
                    if (name.replace("?", "")
                            .endsWith(i.getName()) && name.replace("?", "")
                            .startsWith(i.getType())) {
                        searchedItem.set(i);
                    }
                    if (!map.containsKey(i.getCatalogue())) {
                        map.put(i.getCatalogue(), new ArrayList<>());
                    }
                    map.get(i.getCatalogue()).add(i);
                }
        );
        if (brand.isEmpty())
            brand = null;
        if (searchedItem.get() != null) {
            ItemElastic i = searchedItem.get();
            return Collections.singletonList(new CatalogueElastic(i.getCatalogue(),
                    i.getCatalogueId(),
                    Collections.singletonList(i),
                    brand));
        }
        String finalBrand = brand;
        return map.keySet().stream()
                .map(c ->
                        new CatalogueElastic(c, map.get(c).get(0).getCatalogueId(), map.get(c), finalBrand))
                .collect(Collectors.toList());
    }

    public List<CatalogueElastic> getByName(String num) {
        List<ItemElastic> list;
        list = elasticRepository.findAllByName(".*" + num + ".*", pageable);
        return get(list, num, "");
    }

    public List<CatalogueElastic> getByItemId(String itemId) {
        var list = elasticRepository.findByItemId(itemId, PageRequest.of(0, 1));
        return Collections.singletonList(new CatalogueElastic(list.get(0).getCatalogue(),
                list.get(0).getCatalogueId(),
                list,
                list.get(0).getBrand()));
    }

    private Boolean isContainErrorChar(String text) {
        return text.contains("[") || text.contains("]") || text.contains("\"")
                || text.contains("/") || text.contains(";");
    }

    public List<CatalogueElastic> getAllFull(String text) {
        return getAll(text, pageable);
    }

}
