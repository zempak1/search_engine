package ru.makhmudov.search_engine.util;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class RecursiveLemmatizator extends RecursiveTask<Map<String, Integer>> {

    private static LuceneMorphology rusMorph;
    private static LuceneMorphology engMorph;
    private List<String> wordList;

    static {
        try {
            rusMorph = new RussianLuceneMorphology();
            engMorph = new EnglishLuceneMorphology();
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    public RecursiveLemmatizator(String text) {
        initWordList(text);
    }

    private void initWordList(String text) {
        text = text.replaceAll("[^а-яА-Я|^a-zA-Z]", " ").toLowerCase();
        wordList = Arrays.asList(text.split("\\s+"));
    }

    public RecursiveLemmatizator(List<String> wordList) {
        this.wordList = wordList;
    }

    public static void main(String[] args) {


    }

    @Override
    protected Map<String, Integer> compute() {
        List<RecursiveLemmatizator> tasks = new ArrayList<>();

        if (wordList.size() <= 1) {
            Map<String, Integer> lemmaMap = new HashMap<>();
            LuceneMorphology morph;
            String word = wordList.get(0);
            boolean isRus = word.matches("[а-я]+");
            if (isRus && word.length() == 1 && !word.equals("я")) return lemmaMap;
            morph = isRus ? rusMorph : engMorph;
            List<String> info =  morph.getMorphInfo(word);

            boolean isValid;
            if (isRus) {
                isValid = info.stream().noneMatch(i -> i.contains("n СОЮЗ") ||
                        i.contains("o МЕЖД") ||
                        i.contains("p ЧАСТ") ||
                        i.contains("l ПРЕДЛ"));
            } else {
                isValid = info.stream().noneMatch(i -> i.contains("1 PART") ||
                        i.contains("1 PREP") ||
                        i.contains("1 INT") ||
                        i.contains("1 CONJ"));
            }
            if (!isValid) return lemmaMap;
            lemmaMap.put(morph.getNormalForms(word).get(0), 1);
            return lemmaMap;
        }

        List<String> firstHalfWordList = new ArrayList<>(wordList.subList(0, wordList.size()/2));
        List<String> secondHalfWordList = new ArrayList<>(wordList.subList(wordList.size()/2, wordList.size()));

        RecursiveLemmatizator task1 = new RecursiveLemmatizator(firstHalfWordList);
        RecursiveLemmatizator task2 = new RecursiveLemmatizator(secondHalfWordList);
        task1.fork();
        task2.fork();
        tasks.add(task1);
        tasks.add(task2);

        return appendLemmas(tasks);
    }

    private Map<String, Integer> appendLemmas(List<RecursiveLemmatizator> tasks) {
        Map<String, Integer> map1 = tasks.get(0).join();
        Map<String, Integer> map2 = tasks.get(1).join();
        map1.forEach((k, v) -> map2.compute(k, (k2, v2) -> v2 == null ? v : v2 + v));
        return map2;
    }
}