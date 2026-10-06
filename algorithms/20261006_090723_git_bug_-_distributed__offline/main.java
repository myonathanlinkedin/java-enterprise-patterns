import java.util.*;

public class main {
    private static int passed = 0;
    private static int failed = 0;

    private static void assertEqual(Object expected, Object actual, String testName) {
        if (Objects.equals(expected, actual)) {
            passed++;
            System.out.println("PASS: " + testName);
        } else {
            failed++;
            System.out.println("FAIL: " + testName + " | Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + testName);
        } else {
            failed++;
            System.out.println("FAIL: " + testName);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Git Bug Tracker Unit Tests ===\n");

        core.BugTracker tracker = new core.BugTracker();

        core.Bug bug1 = new core.Bug("BUG-001", "Null pointer in parser", "Parser crashes on null input",
                core.Severity.HIGH, core.Status.OPEN, System.currentTimeMillis(), System.currentTimeMillis(),
                "abc123", Arrays.asList("parser", "critical"));

        core.Bug bug2 = new core.Bug("BUG-002", "Memory leak in cache", "Cache grows unbounded",
                core.Severity.CRITICAL, core.Status.IN_PROGRESS, System.currentTimeMillis(), System.currentTimeMillis(),
                "def456", Arrays.asList("memory", "performance"));

        core.Bug bug3 = new core.Bug("BUG-003", "UI flicker on resize", "Window flickers when resized",
                core.Severity.LOW, core.Status.OPEN, System.currentTimeMillis(), System.currentTimeMillis(),
                "abc123", Arrays.asList("ui", "visual"));

        tracker.addBug(bug1);
        tracker.addBug(bug2);
        tracker.addBug(bug3);

        assertEqual(3, tracker.getBugCount(), "Initial bug count");
        assertEqual(bug1, tracker.getBug("BUG-001"), "Get bug by ID");
        assertEqual(null, tracker.getBug("NONEXISTENT"), "Get non-existent bug");

        List<core.Bug> commitBugs = tracker.getBugsByCommit("abc123");
        assertEqual(2, commitBugs.size(), "Bugs by commit abc123");
        assertTrue(commitBugs.contains(bug1) && commitBugs.contains(bug3), "Commit bugs contain correct bugs");

        List<core.Bug> tagBugs = tracker.getBugsByTag("parser");
        assertEqual(1, tagBugs.size(), "Bugs by tag parser");
        assertEqual(bug1, tagBugs.get(0), "Tag bug is correct");

        List<core.Bug> openBugs = tracker.getBugsByStatus(core.Status.OPEN);
        assertEqual(2, openBugs.size(), "Open bugs count");

        List<core.Bug> criticalBugs = tracker.getBugsBySeverity(core.Severity.CRITICAL);
        assertEqual(1, criticalBugs.size(), "Critical bugs count");
        assertEqual(bug2, criticalBugs.get(0), "Critical bug is correct");

        tracker.updateBugStatus("BUG-001", core.Status.RESOLVED);
        assertEqual(core.Status.RESOLVED, tracker.getBug("BUG-001").getStatus(), "Update bug status");

        tracker.addTagToBug("BUG-002", "urgent");
        List<core.Bug> urgentBugs = tracker.getBugsByTag("urgent");
        assertEqual(1, urgentBugs.size(), "Bugs by new tag urgent");
        assertEqual(bug2, urgentBugs.get(0), "Urgent bug is correct");

        Map<core.Status, Long> statusCounts = tracker.getStatusCounts();
        assertEqual(Long.valueOf(1), statusCounts.get(core.Status.RESOLVED), "Resolved count");
        assertEqual(Long.valueOf(1), statusCounts.get(core.Status.IN_PROGRESS), "In progress count");
        assertEqual(Long.valueOf(1), statusCounts.get(core.Status.OPEN), "Open count");

        Map<core.Severity, Long> severityCounts = tracker.getSeverityCounts();
        assertEqual(Long.valueOf(1), severityCounts.get(core.Severity.HIGH), "High severity count");
        assertEqual(Long.valueOf(1), severityCounts.get(core.Severity.CRITICAL), "Critical severity count");
        assertEqual(Long.valueOf(1), severityCounts.get(core.Severity.LOW), "Low severity count");

        System.out.println("\n=== Test Summary ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total: " + (passed + failed));

        if (failed > 0) {
            System.exit(1);
        }
    }
}
