package ru.shop.backend.search.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
public class Item {
    @Column
    private Integer price;
    @Column
    private String name;
    @Column
    private String url;
    @Column
    private String image;
    @Id
    @Column
    private Integer itemId;
    private String cat;
}
