package ru.shop.backend.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class  TypeHelpText {
    private TypeOfQuery type;
    private String text;
}
