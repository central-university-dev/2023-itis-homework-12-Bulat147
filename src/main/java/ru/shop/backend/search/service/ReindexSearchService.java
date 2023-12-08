package ru.shop.backend.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemDbRepository;
import ru.shop.backend.search.repository.ItemRepository;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReindexSearchService {
    private final ItemDbRepository dbRepository;
    private final ItemRepository searchRepository;
    @Scheduled(fixedDelay = 43200000)
    @Transactional
    public void reindex(){
        log.info("генерация индексов по товарам запущена");
        dbRepository.findAllInStream().parallel()
                .map(entity -> new ItemElastic(entity))
                .forEach(
                item ->
                searchRepository.save(item)
        );
        log.info("генерация индексов по товарам закончилась");

    }
}
