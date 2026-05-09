package com.smartstudyplanner.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartstudyplanner.backend.dto.learning.LearningResourcesResponse;
import com.smartstudyplanner.backend.dto.learning.WikipediaArticleItem;
import com.smartstudyplanner.backend.dto.learning.YoutubeVideoItem;
import com.smartstudyplanner.backend.model.StudyTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class LearningResourcesService {

    private static final Logger log = LoggerFactory.getLogger(LearningResourcesService.class);
    private static final int MAX_QUERY_LEN = 200;
    private static final int YOUTUBE_MAX = 5;
    private static final int WIKI_MAX = 5;
    private static final Pattern WIKI_HTML_TAGS = Pattern.compile("<[^>]+>");

    private final RestClient learningRestClient;
    private final TaskService taskService;

    @Value("${app.youtube.api-key:}")
    private String youtubeApiKey;

    public LearningResourcesService(
            @Qualifier("learningRestClient") RestClient learningRestClient,
            TaskService taskService
    ) {
        this.learningRestClient = learningRestClient;
        this.taskService = taskService;
    }

    public LearningResourcesResponse getResourcesForTask(String userId, String taskId) {
        StudyTask task = taskService.getOwnedTask(userId, taskId);
        String primaryQuery = buildSearchQuery(task);
        boolean youtubeOk = youtubeApiKey != null && !youtubeApiKey.isBlank();
        List<YoutubeVideoItem> videos = new ArrayList<>();
        List<WikipediaArticleItem> articles = new ArrayList<>();

        if (!primaryQuery.isEmpty()) {
            if (youtubeOk) {
                try {
                    videos.addAll(fetchYoutube(primaryQuery));
                } catch (RestClientException e) {
                    log.warn("YouTube search failed: {}", e.getMessage());
                }
            }
            try {
                articles.addAll(fetchWikipediaWithFallback(task, primaryQuery));
            } catch (RestClientException e) {
                log.warn("Wikipedia lookup failed: {}", e.getMessage());
            }
        }

        return new LearningResourcesResponse(primaryQuery, youtubeOk, List.copyOf(videos), List.copyOf(articles));
    }

    private List<WikipediaArticleItem> fetchWikipediaWithFallback(StudyTask task, String primaryQuery) {
        List<WikipediaArticleItem> found = fetchWikipedia(primaryQuery);
        if (!found.isEmpty()) {
            return found;
        }
        String sub = task.getSubject() != null ? task.getSubject().trim() : "";
        String tit = task.getTitle() != null ? task.getTitle().trim() : "";
        if (!sub.isEmpty() && !sub.equalsIgnoreCase(primaryQuery)) {
            found = fetchWikipedia(truncate(sub));
            if (!found.isEmpty()) {
                return found;
            }
        }
        if (!tit.isEmpty() && !tit.equalsIgnoreCase(primaryQuery) && !tit.equalsIgnoreCase(sub)) {
            found = fetchWikipedia(truncate(tit));
        }
        return found;
    }

    private String buildSearchQuery(StudyTask task) {
        String subject = task.getSubject() != null ? task.getSubject().trim() : "";
        String title = task.getTitle() != null ? task.getTitle().trim() : "";
        if (subject.isEmpty() && title.isEmpty()) {
            return "";
        }
        if (subject.isEmpty()) {
            return truncate(title);
        }
        if (title.isEmpty()) {
            return truncate(subject);
        }
        if (title.toLowerCase().contains(subject.toLowerCase())) {
            return truncate(title);
        }
        return truncate(subject + " " + title);
    }

    private static String truncate(String s) {
        if (s.length() <= MAX_QUERY_LEN) {
            return s;
        }
        return s.substring(0, MAX_QUERY_LEN).trim();
    }

    private List<YoutubeVideoItem> fetchYoutube(String query) {
        String uri = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("maxResults", YOUTUBE_MAX)
                .queryParam("q", query)
                .queryParam("key", youtubeApiKey.trim())
                .build()
                .encode()
                .toUriString();

        JsonNode root = learningRestClient.get().uri(uri).retrieve().body(JsonNode.class);
        if (root == null || !root.has("items")) {
            return List.of();
        }
        List<YoutubeVideoItem> out = new ArrayList<>();
        for (JsonNode item : root.get("items")) {
            JsonNode idNode = item.path("id");
            String videoId = idNode.path("videoId").asText(null);
            if (videoId == null || videoId.isEmpty()) {
                continue;
            }
            JsonNode sn = item.path("snippet");
            String vTitle = sn.path("title").asText("");
            String channel = sn.path("channelTitle").asText("");
            String thumb = sn.path("thumbnails").path("medium").path("url").asText("");
            if (thumb.isEmpty()) {
                thumb = sn.path("thumbnails").path("default").path("url").asText("");
            }
            String watch = "https://www.youtube.com/watch?v=" + videoId;
            out.add(new YoutubeVideoItem(videoId, vTitle, channel, thumb, watch));
        }
        return out;
    }

    /**
     * Uses only {@code w/api.php}: {@code list=search} then a single {@code prop=extracts} batch call.
     * Avoids {@code /api/rest_v1/} which is sometimes blocked or stricter on clients.
     */
    private List<WikipediaArticleItem> fetchWikipedia(String query) {
        JsonNode searchRoot = wikiSearch(query);
        if (searchRoot == null) {
            return List.of();
        }
        if (searchRoot.has("error")) {
            log.warn("Wikipedia search API error: {}", searchRoot.path("error").path("info").asText(searchRoot.toString()));
            return List.of();
        }
        JsonNode queryNode = searchRoot.path("query");
        JsonNode searchArr = queryNode.path("search");
        if (searchArr.isMissingNode() || !searchArr.isArray() || searchArr.isEmpty()) {
            return List.of();
        }

        List<JsonNode> hits = new ArrayList<>();
        for (JsonNode hit : searchArr) {
            hits.add(hit);
            if (hits.size() >= WIKI_MAX) {
                break;
            }
        }

        List<String> titles = new ArrayList<>();
        for (JsonNode hit : hits) {
            String t = hit.path("title").asText("");
            if (!t.isEmpty()) {
                titles.add(t);
            }
        }
        if (titles.isEmpty()) {
            return List.of();
        }

        Map<String, String> extractByTitle = fetchWikiExtractsMap(titles);

        List<WikipediaArticleItem> out = new ArrayList<>();
        for (JsonNode hit : hits) {
            String title = hit.path("title").asText("");
            if (title.isEmpty()) {
                continue;
            }
            String extract = matchExtract(title, extractByTitle);
            if (extract.isBlank()) {
                extract = stripWikiMarkup(hit.path("snippet").asText(""));
            }
            if (extract.isBlank()) {
                extract = "Open the Wikipedia article for full text.";
            }
            out.add(new WikipediaArticleItem(title, extract, wikipediaArticleUrl(title)));
        }
        return out;
    }

    private JsonNode wikiSearch(String query) {
        String searchUri = UriComponentsBuilder.fromHttpUrl("https://en.wikipedia.org/w/api.php")
                .queryParam("action", "query")
                .queryParam("list", "search")
                .queryParam("srsearch", query)
                .queryParam("srnamespace", "0")
                .queryParam("format", "json")
                .queryParam("srlimit", WIKI_MAX)
                .build()
                .encode()
                .toUriString();

        return learningRestClient.get()
                .uri(searchUri)
                .header("Accept", "application/json")
                .retrieve()
                .body(JsonNode.class);
    }

    private Map<String, String> fetchWikiExtractsMap(List<String> titles) {
        String joined = String.join("|", titles);
        String uri = UriComponentsBuilder.fromHttpUrl("https://en.wikipedia.org/w/api.php")
                .queryParam("action", "query")
                .queryParam("prop", "extracts")
                .queryParam("exintro", "true")
                .queryParam("explaintext", "true")
                .queryParam("redirects", "1")
                .queryParam("titles", joined)
                .queryParam("format", "json")
                .build()
                .encode()
                .toUriString();

        try {
            JsonNode root = learningRestClient.get()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(JsonNode.class);
            if (root == null || !root.path("query").has("pages")) {
                return Map.of();
            }
            return buildExtractMap(root.path("query").path("pages"));
        } catch (RestClientException e) {
            log.warn("Wikipedia extracts request failed: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * MediaWiki returns {@code pages} as an object keyed by page id; values hold {@code title} and {@code extract}.
     */
    private static Map<String, String> buildExtractMap(JsonNode pagesNode) {
        Map<String, String> map = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = pagesNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> e = fields.next();
            JsonNode page = e.getValue();
            if (page.has("missing")) {
                continue;
            }
            int ns = page.path("ns").asInt(0);
            if (ns != 0) {
                continue;
            }
            String title = page.path("title").asText("");
            if (title.isEmpty()) {
                continue;
            }
            String extract = page.path("extract").asText("");
            map.put(title, extract);
        }
        return map;
    }

    private static String matchExtract(String searchTitle, Map<String, String> extracts) {
        if (extracts.containsKey(searchTitle)) {
            return extracts.get(searchTitle);
        }
        for (Map.Entry<String, String> e : extracts.entrySet()) {
            if (e.getKey().equalsIgnoreCase(searchTitle)) {
                return e.getValue();
            }
        }
        return "";
    }

    private static String stripWikiMarkup(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        String s = WIKI_HTML_TAGS.matcher(html).replaceAll("");
        return s.replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&#039;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim();
    }

    private static String wikipediaArticleUrl(String title) {
        String underscored = title.trim().replace(' ', '_');
        String encoded = UriUtils.encodePath(underscored, StandardCharsets.UTF_8);
        return "https://en.wikipedia.org/wiki/" + encoded;
    }
}
