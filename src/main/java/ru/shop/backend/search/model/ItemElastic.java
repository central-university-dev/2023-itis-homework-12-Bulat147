package ru.shop.backend.search.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "item", createIndex = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemElastic {
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Text)
    @JsonIgnore
    private String fulltext;
    @Id
    @Field(name = "item_id", type = FieldType.Integer)
    private Long itemId;
    @Field(name = "catalogue_id", type = FieldType.Integer)
    @JsonIgnore
    private Long catalogueId;
    @Field(type = FieldType.Text)
    @JsonIgnore
    private String catalogue;
    @Field(type = FieldType.Text)
    private String brand;
    @Field(type = FieldType.Text)
    private String type;

    @Field(type = FieldType.Text)
    private String description;

    public ItemElastic(ItemEntity entity) {
        this.description = buildDescription(entity.getDescription());
        this.fulltext = entity.getCatalogue()+ " " + entity.getType() + " " + entity.getName() + " " + description;
        this.name = entity.getName().replace(entity.getBrand(),"").trim();
        this.itemId = entity.getItemId();
        this.catalogueId = entity.getCatalogueId();
        this.catalogue = entity.getCatalogue();
        this.brand = entity.getBrand();
        this.type = entity.getType();
    }
    public String buildDescription(String description){
        return Arrays.stream(description.split(";")).map(
                d -> {
                    d = d.toLowerCase(Locale.ROOT);
                    if(d.contains(": нет"))
                        return null;
                    if(d.contains(": -"))
                        return null;
                    if(d.contains(": есть"))
                        return d.replace(": есть", "");
                    if(d.contains(": 0 "))
                        return null;
                    return d.replace(":","");
                }
        ).filter( d -> d != null).collect(Collectors.joining());
    }
}