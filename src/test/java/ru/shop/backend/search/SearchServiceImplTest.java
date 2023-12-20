package ru.shop.backend.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.shop.backend.search.repository.ItemDbRepository;
import ru.shop.backend.search.repository.ItemElasticRepository;
import ru.shop.backend.search.service.MessageConverter;
import ru.shop.backend.search.service.impl.SearchServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;

//@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class SearchServiceImplTest {

    @Mock
    ItemElasticRepository elasticRepository;
    @Mock
    ItemDbRepository itemDbRepository;
    @InjectMocks
    SearchServiceImpl service;

    @Test
    public void convert() {
        assertThat(MessageConverter.convert("for рщьу elastic")).isEqualTo("home");
    }
}
