package util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dao.WordEntry;
import dao.WordsStatsDAO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OmdbClient {

    private static final String API_KEY = System.getenv("OMDB_API_KEY");
    private static final String BASE_URL = "https://www.omdbapi.com/";
    private static final Logger logger = Logger.getLogger("OmdbClient");

    private static final Cache<String, String> plotCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(6, TimeUnit.HOURS)
            .build();

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();


    public static String fetchPlotByImdbId(String imdbId, WordEntry entry, WordsStatsDAO wso) {

        String cached = plotCache.getIfPresent(imdbId);
        if (cached != null) return cached;


        if (entry.plotHint() != null) {
            plotCache.put(imdbId, entry.plotHint());
            return entry.plotHint();
        }

        String plot = callOmdbApi(imdbId);
        if (plot != null) {
            plotCache.put(imdbId, plot);
            wso.savePlotHint(entry.wordId(), plot);
        }
        return plot;
    }

    private static String callOmdbApi(String imdbId) {
        try {
            String url = BASE_URL + "?apikey=" + API_KEY + "&i=" + imdbId + "&plot=short";
            HttpRequest request = HttpRequest.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.log(Level.WARNING, "OMDb returned status {0} for {1}",
                        new Object[]{response.statusCode(), imdbId});
                return null;
            }

            return sanitizePlot(extractJsonField(response.body(), "Plot"));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "OMDb fetch failed for " + imdbId, e);
            return null;
        }
    }

    private static String extractJsonField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int start = json.indexOf(key);
        if (start == -1) return null;
        start += key.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? null : json.substring(start, end);
    }

    private static String sanitizePlot(String plot) {
        if (plot == null || plot.isBlank() || plot.equals("N/A")) return null;
        String[] words = plot.split("\\s+");
        int limit = Math.min(20, words.length);
        return String.join(" ", java.util.Arrays.asList(words).subList(0, limit))
                + (words.length > limit ? "..." : "");
    }
}