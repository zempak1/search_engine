package ru.makhmudov.search_engine.util;

import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ru.makhmudov.search_engine.entity.Page;
import ru.makhmudov.search_engine.repository.PageRepository;

import java.io.IOException;
import java.io.Serial;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecursiveSiteParser extends RecursiveTask<Set<String>> {

    @Serial
    private static final long serialVersionUID;
    private final PageRepository pageRepository;
    public final static AtomicInteger connectionCount;

    private static volatile String domain;//
    private static volatile String domainRegex;  //Example:  (?:www\.)?(?:lenta\.ru)?
    private static volatile String domain2;         //Example:  (?:www\.)?lenta\.ru
    private static volatile String protocolRegex;
    private static volatile String pathRegex;
    private static volatile StringBuffer invalidURLEndings;

    private final Set<String> urlSet;
    private String url;//
    private Document doc;


    static {
        connectionCount = new AtomicInteger();
        serialVersionUID = 1L;
    }

    public RecursiveSiteParser(URL url, PageRepository pageRepository) {
        this.urlSet = Collections.synchronizedSet(new HashSet<>());
        this.pageRepository = pageRepository;
        this.url = url.toString();
        RecursiveSiteParser.domain = url.getHost();
        initParams();
    }

    public RecursiveSiteParser(String url, Set<String> urlMap, PageRepository pageRepository) {
        this.url = url;
        this.pageRepository = pageRepository;
        this.urlSet = urlMap;
    }

    private void initParams() {
        invalidURLEndings = new StringBuffer(Util.loadInvalidUrlEndings());
        protocolRegex = "(?:http[s]?:\\/\\/)?";
        String[] arr = domain.split("\\.");
        domainRegex = arr[0].contains("www") ?
                String.format("(?:%s\\.)?(?:%s\\.%s)?", arr[0], arr[1], arr[2].replaceAll("/", "")) :
                String.format("(?:%s\\.%s)?", arr[0], arr[1]);
        pathRegex = "(\\/?\\w*[-]?\\w*\\/?)+.?\\w+";

        domain2 = "//" + domain.replaceAll("www\\.", "") + "/";
    }

    @Override
    protected Set<String> compute() {
        List<RecursiveSiteParser> tasks = new ArrayList<>();
        boolean shouldContinue = parseSite();

        if (!shouldContinue) return urlSet;

        urlSet.add(url);
        Elements els = doc.body()
                .getElementsByAttributeValueMatching("href", protocolRegex + domainRegex + pathRegex);

        AtomicInteger count = new AtomicInteger();
        els.stream()
            .filter(el -> el.absUrl("href").contains(domain2) || el.absUrl("href").contains("//" + domain + "/"))
            .filter(el -> !urlSet.contains(el.absUrl("href")))
            .forEach(el -> {
                String absUrl = el.absUrl("href");
                if (isValidURL(absUrl)) {
                    urlSet.add(absUrl);
                    RecursiveSiteParser task = new RecursiveSiteParser(absUrl, urlSet, pageRepository);
                    task.fork();
                    tasks.add(task);
                    count.getAndIncrement();
                }
            });

        if (count.get() == 0) return urlSet;

        tasks.forEach(ForkJoinTask::join);
//        Util.saveInvalidUrlEndings(invalidURLEndings.toString());
        return urlSet;
    }

    private boolean parseSite() {
        sleep(100);
        long start = System.nanoTime();
        Connection connection = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .referrer("http://www.google.com");
        Connection.Response response = null;
        String path = url.replaceAll("http[s?]:\\/\\/(?:www\\.)?\\w+\\.\\w+", "");
        path = path.equals("") ? "/" : path;
        try {
            response = connection.execute();
            connectionCount.getAndIncrement();
            doc = response.parse();
            String content = doc.html();
            int code = response.statusCode();
            if (code == 503) {
                urlSet.remove(url);
                long time = System.nanoTime() - start;
                System.out.printf("Time: %.3f sec.\n", time/1e9);
                return false;
            }
            savePage(content, code, path);
            long time = System.nanoTime() - start;
            System.out.printf("Time: %.3f sec.\n", time/1e9);
            return true;
        } catch (IOException e) {
            String mes = e.getLocalizedMessage();
            System.out.println(mes);
            if (mes.contains("Unhandled content type")) {
                Pattern pattern = Pattern.compile("\\.\\w+\\/?$");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    invalidURLEndings.append(matcher.group(0) + ";");
                }
                System.out.println(url);
                return false;
            } if (mes.contains("Read timed out")) {
                savePage(null, 408, path);
                System.out.println(url);
                return false;
            }
            long time = System.nanoTime() - start;
            System.out.printf("Time: %.3f sec.\n", time/1e9);
            return false;
        }
    }

    private void savePage(String content, int code, String path) {
        Page page = new Page();
        page.setContent(content);
        page.setCode(code);
        page.setPath(path);
        pageRepository.save(page);
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isValidURL(String url) {
        if (url.contains("?") || url.contains("#")) return false;
        Pattern pattern = Pattern.compile("\\.\\w+\\/?$");
        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) return true;
        String foundation = matcher.group(0);

        return !invalidURLEndings.toString().contains(foundation.toLowerCase());
    }


    @Override
    public void complete(Set<String> value) {
        super.complete(value);
        Util.saveInvalidUrlEndings(invalidURLEndings.toString());
    }
}
