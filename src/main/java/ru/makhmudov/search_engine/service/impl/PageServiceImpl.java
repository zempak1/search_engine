package ru.makhmudov.search_engine.service.impl;

import org.springframework.stereotype.Service;
import ru.makhmudov.search_engine.repository.PageRepository;
import ru.makhmudov.search_engine.util.RecursiveSiteParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

@Service
public class PageServiceImpl {

    private final PageRepository pageRepository;

    public PageServiceImpl(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public void parseSiteByURL(String url) {
        long start = System.nanoTime();
        URL urlObject = createURL(url);
        RecursiveSiteParser siteParser = new RecursiveSiteParser(urlObject, pageRepository);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Set<String> urlSet = forkJoinPool.invoke(siteParser);
        long time = System.nanoTime() - start;

        System.err.printf(
                """
                *******************
                *      DONE       *
                *******************
                
                Time: %.3f sec.
                Conn count: %d
                Set size: %d
                
                """, time/1e9, RecursiveSiteParser.connectionCount.get(), urlSet.size());
    }

    private URL createURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Your URL path is broken");
        }
    }

}
