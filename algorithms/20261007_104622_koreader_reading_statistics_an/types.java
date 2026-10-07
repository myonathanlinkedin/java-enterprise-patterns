import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class BookStats {
    public int pagesRead;
    public long timeSpentSeconds; // total reading time in seconds

    public BookStats(int pagesRead, long timeSpentSeconds) {
        this.pagesRead = pagesRead;
        this.timeSpentSeconds = timeSpentSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BookStats bookStats = (BookStats) o;
        return pagesRead == bookStats.pagesRead &&
                timeSpentSeconds == bookStats.timeSpentSeconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pagesRead, timeSpentSeconds);
    }
}

public class ReviewInfo {
    public int repetition;          // number of successful repetitions
    public double intervalDays;     // current interval in days
    public double easiness;         // easiness factor
    public LocalDate nextReviewDate;

    public ReviewInfo() {
        this.repetition = 0;
        this.intervalDays = 0;
        this.easiness = 2.5; // default SM-2 easiness
        this.nextReviewDate = LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReviewInfo that = (ReviewInfo) o;
        return repetition == that.repetition &&
                Double.compare(that.intervalDays, intervalDays) == 0 &&
                Double.compare(that.easiness, easiness) == 0 &&
                Objects.equals(nextReviewDate, that.nextReviewDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repetition, intervalDays, easiness, nextReviewDate);
    }
}

public class Highlight {
    public final String id;                 // unique identifier (e.g., UUID)
    public final String text;
    public final int page;
    public final LocalDateTime timestamp;   // when the highlight was created
    public ReviewInfo reviewInfo;

    public Highlight(String id, String text, int page, LocalDateTime timestamp) {
        this.id = id;
        this.text = text;
        this.page = page;
        this.timestamp = timestamp;
        this.reviewInfo = new ReviewInfo();
    }

    // Merge two highlights with the same id, preferring the later timestamp
    public static Highlight merge(Highlight a, Highlight b) {
        if (!a.id.equals(b.id)) {
            throw new IllegalArgumentException("Cannot merge highlights with different ids");
        }
        Highlight newer = a.timestamp.isAfter(b.timestamp) ? a : b;
        Highlight merged = new Highlight(newer.id, newer.text, newer.page, newer.timestamp);
        // Preserve the most advanced reviewInfo (higher repetition)
        merged.reviewInfo = (a.reviewInfo.repetition >= b.reviewInfo.repetition) ? a.reviewInfo : b.reviewInfo;
        return merged;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Highlight highlight = (Highlight) o;
        return page == highlight.page &&
                Objects.equals(id, highlight.id) &&
                Objects.equals(text, highlight.text) &&
                Objects.equals(timestamp, highlight.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, page, timestamp);
    }
}
