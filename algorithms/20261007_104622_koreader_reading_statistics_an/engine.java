import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SyncEngine {

    // Merge two BookStats objects: take max pagesRead, sum timeSpentSeconds
    public static BookStats mergeStats(BookStats a, BookStats b) {
        int mergedPages = Math.max(a.pagesRead, b.pagesRead);
        long mergedTime = a.timeSpentSeconds + b.timeSpentSeconds;
        return new BookStats(mergedPages, mergedTime);
    }

    // Merge two highlight lists, deduplicating by id and keeping the newer entry
    public static List<Highlight> mergeHighlights(List<Highlight> a, List<Highlight> b) {
        Map<String, Highlight> map = new HashMap<>();
        for (Highlight h : a) {
            map.put(h.id, h);
        }
        for (Highlight h : b) {
            map.merge(h.id, h, Highlight::merge);
        }
        // Return sorted by timestamp for deterministic order
        return map.values().stream()
                .sorted(Comparator.comparing(h -> h.timestamp))
                .collect(Collectors.toList());
    }

    // SM-2 spaced repetition update for a highlight based on recall quality (0-5)
    public static void updateReview(Highlight highlight, int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5");
        }
        ReviewInfo info = highlight.reviewInfo;
        if (quality >= 3) {
            if (info.repetition == 0) {
                info.intervalDays = 1;
            } else if (info.repetition == 1) {
                info.intervalDays = 6;
            } else {
                info.intervalDays = Math.round(info.intervalDays * info.easiness);
            }
            // Update easiness factor
            double ef = info.easiness + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
            info.easiness = Math.max(1.3, ef);
            info.repetition += 1;
        } else {
            // Reset on failure
            info.repetition = 0;
            info.intervalDays = 1;
        }
        info.nextReviewDate = LocalDate.now().plusDays((long) Math.round(info.intervalDays));
    }

    // Compute next review date without mutating the highlight (pure function)
    public static LocalDate computeNextReviewDate(ReviewInfo info) {
        return LocalDate.now().plusDays((long) Math.round(info.intervalDays));
    }
}
