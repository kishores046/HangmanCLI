
package etl;

import util.HikariConnectionManager;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * One-time (or periodically re-run) batch job:
 * reads title.basics.tsv.gz + title.ratings.tsv.gz,
 * filters to popular, non-adult movies, and bulk-loads
 * candidates into words_staging for manual/scripted category assignment.
 */
public class IMDbStagingETL {

    private static final int MIN_VOTES = 50_000;   // tune this — higher = more mainstream-only
    private static final int BATCH_SIZE = 1000;

    private static final String BASICS_FILE = "C:\\Users\\CHINNAX\\imdb-data\\title.basics.tsv.gz";
    private static final String RATINGS_FILE = "C:\\Users\\CHINNAX\\imdb-data\\title.ratings.tsv.gz";
    private static final String UPSERT_SQL =
            "INSERT INTO words_staging (tconst, primary_title, genres, average_rating, num_votes) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "primary_title = VALUES(primary_title), genres = VALUES(genres), " +
                    "average_rating = VALUES(average_rating), num_votes = VALUES(num_votes)";

    public static void main(String[] args) throws Exception {
        System.out.println("Step 1/3: Loading ratings into memory...");
        Map<String, RatingRow> ratings = loadRatings();
        System.out.println("Loaded " + ratings.size() + " rated titles (numVotes >= " + MIN_VOTES + ")");

        System.out.println("Step 2/3: Streaming basics + filtering...");
        DataSource ds = HikariConnectionManager.getDataSource();

        int inserted = 0;
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPSERT_SQL);
             BufferedReader br = openTsv(BASICS_FILE)) {

            conn.setAutoCommit(false);

            String line = br.readLine(); // skip header
            int batchCount = 0;

            while ((line = br.readLine()) != null) {
                String[] cols = line.split("\t", -1);
                // tconst titleType primaryTitle originalTitle isAdult startYear endYear runtimeMinutes genres
                if (cols.length < 9) continue;

                String tconst = cols[0];
                String titleType = cols[1];
                String primaryTitle = cols[2];
                String isAdult = cols[4];
                String genres = cols[8];

                if (!"movie".equals(titleType)) continue;
                if (!"0".equals(isAdult)) continue;
                if ("\\N".equals(genres) || genres.isBlank()) continue;

                RatingRow rating = ratings.get(tconst);
                if (rating == null) continue; // not in high-vote set

                ps.setString(1, tconst);
                ps.setString(2, primaryTitle);
                ps.setString(3, genres);
                ps.setDouble(4, rating.avgRating);
                ps.setInt(5, rating.numVotes);
                ps.addBatch();

                batchCount++;
                inserted++;

                if (batchCount == BATCH_SIZE) {
                    ps.executeBatch();
                    conn.commit();
                    batchCount = 0;
                    System.out.print("\r  inserted so far: " + inserted);
                }
            }

            if (batchCount > 0) {
                ps.executeBatch();
                conn.commit();
            }
        }

        System.out.println("\nStep 3/3: Done. " + inserted + " candidate titles staged in words_staging.");
    }

    private static Map<String, RatingRow> loadRatings() throws IOException {
        Map<String, RatingRow> map = new HashMap<>();
        try (BufferedReader br = openTsv(RATINGS_FILE)) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] cols = line.split("\t", -1);
                // tconst averageRating numVotes
                if (cols.length < 3) continue;

                int numVotes;
                try {
                    numVotes = Integer.parseInt(cols[2]);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (numVotes < MIN_VOTES) continue;

                double avgRating;
                try {
                    avgRating = Double.parseDouble(cols[1]);
                } catch (NumberFormatException e) {
                    continue;
                }

                map.put(cols[0], new RatingRow(avgRating, numVotes));
            }
        }
        return map;
    }

    private static BufferedReader openTsv(String path) throws IOException {
        return new BufferedReader(
                new InputStreamReader(new GZIPInputStream(new FileInputStream(path))),
                1 << 16 // 64KB buffer, these files are large
        );
    }

    private record RatingRow(double avgRating, int numVotes) {}
}