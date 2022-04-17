package ru.makhmudov.search_engine.service.impl;

import org.springframework.stereotype.Service;
import ru.makhmudov.search_engine.util.RecursiveLemmatizator;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@Service
public class LemmaService {


    public void extractLemmas(String text) {
//        String text = "Повторное появление леопарда в Осетии позволяет предположить, что\n" +
//                "леопард постоянно обитает в некоторых районах Северного Кавказа.";

        RecursiveLemmatizator recursiveLemmatizator = new RecursiveLemmatizator(text);
        ForkJoinPool fjp = new ForkJoinPool();
        Map<String, Integer> map = fjp.invoke(recursiveLemmatizator);
//        map.forEach((k, v) -> System.out.printf("%s - %d\n", k, v));
    }
}
