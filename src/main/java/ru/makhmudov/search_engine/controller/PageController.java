package ru.makhmudov.search_engine.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.makhmudov.search_engine.service.impl.PageServiceImpl;

@RestController
@RequestMapping("/api")
public class PageController {

    private final PageServiceImpl pageService;

    public PageController(PageServiceImpl pageService) {
        this.pageService = pageService;
    }


    @GetMapping("/parse")
    public void parsePage() {
        pageService.parseSiteByURL("https://www.skillbox.ru");
//        pageService.parseSiteByURL("https://www.askmrrobot.com");
//        pageService.parseSiteByURL("http://www.playback.ru/");
//        pageService.parseSiteByURL("https://volochek.life/");
//        pageService.parseSiteByURL("http://radiomv.ru/");
//        pageService.parseSiteByURL("https://ipfran.ru/");
    }
}
