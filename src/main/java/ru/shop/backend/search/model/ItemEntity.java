package ru.shop.backend.search.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class ItemEntity {
    private String name;
    private String brand;
    private String catalogue;
    private String type;
    private String description;
    private long brandId;
    private long catalogueId;
    @Id
    private long itemId;
}
