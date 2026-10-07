import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        runTests();
        System.out.println("All tests passed.");
    }

    private static void runTests() {
        testMergeStats();
        testMergeHighlights();
        testSpacedRepetition();
    }

    private static void testMergeStats() {
        BookStats local = new BookStats(120, 3600);
        BookStats remote = new BookStats(150, 5400);
        BookStats merged = SyncEngine.mergeStats(local, remote);
        if (merged.pagesRead != 150) {
            throw new AssertionError("Merged pagesRead expected 150, got " + merged.pagesRead);
        }
        if (merged.timeSpentSeconds != 9000) {
            throw new AssertionError("Merged timeSpentSeconds expected 9000, got " + merged.timeSpentSeconds);
        }
    }

    private static void testMergeHighlights() {
        Highlight h1 = new Highlight("id1", "First highlight", 10, LocalDateTime.of(2023, 1, 1, 10, 0));
        Highlight h2 = new Highlight("id2", "Second highlight", 20, LocalDateTime.of(2023, 1, 2, 11, 0));
        Highlight h2New = new Highlight("id2", "Second highlight updated", 20, LocalDateTime.of(2023, 1, 3, 12, 0));
        Highlight h3 = new Highlight("id3", "Third highlight", 30, LocalDateTime.of(2023, 1, 4, 13, 0));

        List<Highlight> local = Arrays.asList(h1, h2);
        List<Highlight> remote = Arrays.asList(h2New, h3);

        List<Highlight> merged = SyncEngine.mergeHighlights(local, remote);
        if (merged.size() != 3) {
            throw new AssertionError("Merged highlights expected size 3, got " + merged.size());
        }
        // Verify that id2 kept the newer timestamp
        Highlight mergedH2 = merged.stream().filter(h -> h.id.equals("id2")).findFirst()
                .orElseThrow(() -> new AssertionError("Merged highlight id2 missing"));
        if (!mergedH2.timestamp.equals(h2New.timestamp)) {
            throw new AssertionError("Highlight id2 did not keep newer timestamp");
        }
    }

    private static void testSpacedRepetition() {
        Highlight h = new Highlight("rev1", "Review me", 5, LocalDateTime.now());

        // First successful review (quality 5)
        SyncEngine.updateReview(h, 5);
        ReviewInfo info = h.reviewInfo;
        if (info.repetition != 1) {
            throw new AssertionError("After first review, repetition expected 1, got " + info.repetition);
        }
        if (Math.round(info.intervalDays) != 1) {
            throw new AssertionError("After first review, interval expected 1, got " + info.intervalDays);
        }
        LocalDate expectedDate1 = LocalDate.now().plusDays(1);
        if (!info.nextReviewDate.equals(expectedDate1)) {
            throw new AssertionError("After first review, nextReviewDate mismatch");
        }

        // Second successful review
        SyncEngine.updateReview(h, 5);
        if (info.repetition != 2) {
            throw new AssertionError("After second review, repetition expected 2, got " + info.repetition);
        }
        if (Math.round(info.intervalDays) != 6) {
            throw new AssertionError("After second review, interval expected 6, got " + info.intervalDays);
        }
        LocalDate expectedDate2 = LocalDate.now().plusDays(6);
        if (!info.nextReviewDate.equals(expectedDate2)) {
            throw new AssertionError("After second review, nextReviewDate mismatch");
        }

        // Third successful review
        SyncEngine.updateReview(h, 5);
        if (info.repetition != 3) {
            throw new AssertionError("After third review, repetition expected 3, got " + info.repetition);
        }
        long expectedInterval = Math.round(6 * info.easiness); // previous interval * easiness
        if (Math.round(info.intervalDays) != expectedInterval) {
            throw new AssertionError("After third review, interval expected " + expectedInterval + ", got " + info.intervalDays);
        }
        LocalDate expectedDate3 = LocalDate.now().plusDays(expectedInterval);
        if (!info.nextReviewDate.equals(expectedDate3)) {
            throw new AssertionError("After third review, nextReviewDate mismatch");
        }

        // Failure case (quality 2) resets repetition
        SyncEngine.updateReview(h, 2);
        if (info.repetition != 0) {
            throw new AssertionError("After failure review, repetition expected 0, got " + info.repetition);
        }
        if (Math.round(info.intervalDays) != 1) {
            throw new AssertionError("After failure review, interval expected 1, got " + info.intervalDays);
        }
    }
}
