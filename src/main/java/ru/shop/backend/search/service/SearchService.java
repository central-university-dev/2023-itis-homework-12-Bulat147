package ru.shop.backend.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.shop.backend.search.model.*;
import ru.shop.backend.search.repository.ItemDbRepository;
import ru.shop.backend.search.repository.ItemRepository;

import javax.xml.catalog.Catalog;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final ItemRepository repo;
    private final ItemDbRepository repoDb;

    private Pageable pageable = PageRequest.of(0, 150);
    private Pageable pageableSmall = PageRequest.of(0, 10);

    private static Pattern pattern = Pattern.compile("\\d+");

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
    public synchronized SearchResult getSearchResult(Integer regionId, String text){
        List<CatalogueElastic> result = null;
        if (isNumeric(text)) {
            Integer itemId = repoDb.findBySku(text).stream().findFirst().orElse(null);
            if (itemId == null) {
                var catalogue = getByName(text);
                if (catalogue.size() > 0) {
                    result = catalogue;
                }
            }
            try {
                result = getByItemId(itemId.toString());
            } catch (Exception e) {
            }
        }
        if(result == null) {
            result = getAll(text);
        }
        List<Item> items = repoDb.findByIds(regionId,
                    result.stream()
                            .flatMap(category -> category.getItems().stream())
                            .map(item -> item.getItemId()) .collect(Collectors.toList())
                            ).stream()
                .map(arr -> new Item(((BigInteger) arr[2]).intValue(),arr[1].toString(),arr[3].toString(),arr[4].toString(),((BigInteger) arr[0]).intValue() , arr[5].toString()))
                            .collect(Collectors.toList());
        Set<String> catUrls = new HashSet();
        String brand = null;
        if(!result.isEmpty())
            brand = result.get(0).getBrand();
        if(brand == null){
            brand = "";
        }
        brand = brand.toLowerCase(Locale.ROOT);
        String finalBrand = brand;
        List<Category> categories = repoDb.findCatsByIds(items.stream().map(i-> i.getItemId()).collect(Collectors.toList())).stream()
                .map(arr ->
                {
                    if(catUrls.contains(arr[2].toString()))
                        return null;
                    catUrls.add(arr[2].toString());
                    return
                            new Category(arr[0].toString()
                                    , arr[1].toString()
                                    , "/cat/" + arr[2].toString() + (finalBrand.isEmpty()?"":"/brands/"+ finalBrand)
                                    , "/cat/" + arr[3].toString(), arr[4] == null ? null : arr[4].toString());
                })
                .filter(x -> x != null)
                .collect(Collectors.toList());
        return new SearchResult(
                items,
                categories,
                result.size()>0?(List.of(new TypeHelpText(TypeOfQuery.SEE_ALSO,
                        ((result.get(0).getItems().get(0).getType()!=null?result.get(0).getItems().get(0).getType():"") +
                        " " + (result.get(0).getBrand()!=null?result.get(0).getBrand():"")).trim()))):new ArrayList<>()
        );
    }
    public synchronized List<CatalogueElastic> getAll(String text){
        return getAll(text, pageableSmall);
    }

    public List<CatalogueElastic> getAll(String text, Pageable pageable){
        String type = "";
        List<ItemElastic> list = new ArrayList<>();
        String brand = "", text2 =text;
        Long catalogueId = null;
        boolean needConvert = true;
        if(isContainErrorChar(text)) {
            text = convert(text);
            needConvert = false;
        }
        if(needConvert && isContainErrorChar(convert(text))) {
            needConvert = false;
        }
        if(text.contains(" "))
            for(String queryWord: text.split("\\s")){
                list = repo.findAllByBrand(queryWord, pageable);
                if(list.isEmpty()&&needConvert){
                    list = repo.findAllByBrand(convert(text), pageable);
                }
                if(!list.isEmpty()) {
                        text = text.replace(queryWord, "").trim().replace("  ", " ");
                        brand = list.get(0).getBrand();
                        break;

                }

            }
        list = repo.findAllByType(text,pageable);
        if(list.isEmpty()&&needConvert){
            list = repo.findAllByType(convert(text), pageable);
        }
        if(!list.isEmpty()) {
            type=(list.stream().map( itemElastic ->
                    itemElastic.getType()).min(Comparator.comparingInt(x-> x.length())).get());
        } else {
            for (String queryWord : text.split("\\s")) {
                list = repo.findAllByType(queryWord,pageable);
                if(list.isEmpty()&&needConvert){
                    list = repo.findAllByType(convert(text), pageable);
                }
                if (!list.isEmpty()) {
                    text = text.replace(queryWord, "");
                    type=(list.stream().map( itemElastic ->
                            itemElastic.getType()).min(Comparator.comparingInt(x-> x.length())).get());
                }
            }
        }
        if(brand.isEmpty()){
            list = repo.findByCatalogue(text, pageable);
            if(list.isEmpty()&&needConvert){
                list = repo.findByCatalogue(convert(text), pageable);
            }
            if(!list.isEmpty()){
                catalogueId = list.get(0).getCatalogueId();
            }
        }
        text = text.trim();
        if(text.isEmpty() && !brand.isEmpty())
            return Collections.singletonList(new CatalogueElastic(list.get(0).getCatalogue(), list.get(0).getCatalogueId(), null, brand));
        text += "?";
        if(brand.isEmpty()) {
                type += "?";
                if(catalogueId == null)
                    if(type.isEmpty()) {
                        list = repo.find(text, pageable);
                        if (list.isEmpty()) {
                            list = repo.find(convert(text), pageable);
                        }
                    }
                    else {
                        list = repo.findAllByType(text, type, pageable);
                        if (list.isEmpty()) {
                            list = repo.findAllByType(convert(text), type, pageable);
                        }
                    }
                else
                    if(type.isEmpty()) {
                        list = repo.find(text, catalogueId, type, pageable);
                        if (list.isEmpty()) {
                            list = repo.find(convert(text), catalogueId, type, pageable);
                        }
                    }
                    else {
                        list = repo.find(text, catalogueId, pageable);
                        if (list.isEmpty()) {
                            list = repo.find(convert(text), catalogueId, pageable);
                        }
                    }

        }else {
            if(type.isEmpty()) {
                list = repo.findAllByBrand(text, brand, pageable);
                if (list.isEmpty()) {
                    list = repo.findAllByBrand(convert(text), brand, pageable);
                }
            }else {
                type += "?";
                list = repo.findAllByTypeAndBrand(text, brand, type, pageable);
                if (list.isEmpty()) {
                    list = repo.findAllByTypeAndBrand(convert(text), brand, type, pageable);
                }
            }
        }

        if(list.isEmpty()){
            if(text2.contains(" "))
                text = Arrays.stream(text.split("\\s")).collect(Collectors.joining(" "));
            text2 += "?";
            list  =repo.findAllNotStrong(text2, pageable);
            if (list.isEmpty()&&needConvert) {
                list = repo.findAllByTypeAndBrand(convert(text2), brand, type, pageable);
            }
        }
        return get(list, text, brand);
    }

    private List<CatalogueElastic> get(List<ItemElastic> list, String name, String brand){
        Map<String, List<ItemElastic>> map = new HashMap<>();
        AtomicReference<ItemElastic> searchedItem = new AtomicReference<>();
        list.stream().forEach(
                i ->
                {
                    if(name.replace("?","").equals(i.getName())) {
                        searchedItem.set(i);
                    }
                    if(name.replace("?","").endsWith(i.getName()) && name.replace("?","").startsWith(i.getType())) {
                        searchedItem.set(i);
                    }
                    if(!map.containsKey(i.getCatalogue())) {
                        map.put(i.getCatalogue(), new ArrayList<>());
                    }
                    map.get(i.getCatalogue()).add(i);
                }
        );
        if(brand.isEmpty())
            brand = null;
        if(searchedItem.get()!=null){
            ItemElastic i = searchedItem.get();
            return Collections.singletonList(new CatalogueElastic(i.getCatalogue(), i.getCatalogueId(), Collections.singletonList(i),brand));
        }
        List<CatalogueElastic> cats = new ArrayList<>();
        String finalBrand = brand;
        return map.keySet().stream().map(c ->
                new CatalogueElastic(c, map.get(c).get(0).getCatalogueId(), map.get(c), finalBrand)).collect(Collectors.toList());
    }
    public List<CatalogueElastic> getByName(String num){
        List<ItemElastic> list = new ArrayList<>();
        list = repo.findAllByName(".*" + num + ".*", pageable);
        return get(list, num, "");
    }
    public List<CatalogueElastic> getByItemId(String itemId) {
        var list = repo.findByItemId(itemId, PageRequest.of(0, 1));
        return Collections.singletonList(new CatalogueElastic(list.get(0).getCatalogue(), list.get(0).getCatalogueId(), list, list.get(0).getBrand()));
    }

    public static String convert(String message) {
        boolean result = message.matches(".*\\p{InCyrillic}.*");
        char[] ru = {'й','ц','у','к','е','н','г','ш','щ','з','х','ъ','ф','ы','в','а','п','р','о','л','д','ж','э', 'я','ч', 'с','м','и','т','ь','б', 'ю','.',
                ' ','0','1','2','3','4','5','6','7','8','9','-'};
        char[] en = {'q','w','e','r','t','y','u','i','o','p','[',']','a','s','d','f','g','h','j','k','l',';','"','z','x','c','v','b','n','m',',','.','/',
                ' ','0','1','2','3','4','5','6','7','8','9','-'};
        StringBuilder builder = new StringBuilder();

        if (result) {
            for (int i = 0; i < message.length(); i++) {
                for (int j = 0; j < ru.length; j++ ) {
                    if (message.charAt(i) == ru[j]) {
                        builder.append(en[j]);
                    }
                }
            }
        } else {
            for (int i = 0; i < message.length(); i++) {
                for (int j = 0; j < en.length; j++ ) {
                    if (message.charAt(i) == en[j]) {
                        builder.append(ru[j]);
                    }
                }
            }
        }
        return builder.toString();
    }
    private Boolean isContainErrorChar(String text){
        if(text.contains("[") || text.contains("]") || text.contains("\"") || text.contains("/") || text.contains(";"))
            return true;
        return false;
    }
    public List<CatalogueElastic> getAllFull(String text) {
        return getAll(text, pageable);
    }
}
