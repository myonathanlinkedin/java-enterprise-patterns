import java.util.*;
import java.util.stream.Collectors;

public class core {
    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status { OPEN, IN_PROGRESS, RESOLVED, CLOSED }

    public static class Bug {
        private final String id;
        private final String title;
        private final String description;
        private final Severity severity;
        private Status status;
        private final long createdAt;
        private long updatedAt;
        private final String gitCommitHash;
        private final List<String> tags;

        public Bug(String id, String title, String description, Severity severity, Status status,
                   long createdAt, long updatedAt, String gitCommitHash, List<String> tags) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.severity = severity;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.gitCommitHash = gitCommitHash;
            this.tags = new ArrayList<>(tags);
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public Severity getSeverity() { return severity; }
        public Status getStatus() { return status; }
        public long getCreatedAt() { return createdAt; }
        public long getUpdatedAt() { return updatedAt; }
        public String getGitCommitHash() { return gitCommitHash; }
        public List<String> getTags() { return Collections.unmodifiableList(tags); }

        public void updateStatus(Status newStatus) {
            this.status = newStatus;
            this.updatedAt = System.currentTimeMillis();
        }

        public void addTag(String tag) {
            if (!tags.contains(tag)) {
                tags.add(tag);
                this.updatedAt = System.currentTimeMillis();
            }
        }

        @Override
        public String toString() {
            return String.format("Bug{id='%s', title='%s', severity=%s, status=%s, commit='%s'}",
                    id, title, severity, status, gitCommitHash);
        }
    }

    public static class BugTracker {
        private final Map<String, Bug> bugs;
        private final Map<String, List<String>> commitToBugs;
        private final Map<String, List<String>> tagToBugs;

        public BugTracker() {
            this.bugs = new HashMap<>();
            this.commitToBugs = new HashMap<>();
            this.tagToBugs = new HashMap<>();
        }

        public void addBug(Bug bug) {
            bugs.putIfAbsent(bug.getId(), bug);
            commitToBugs.computeIfAbsent(bug.getGitCommitHash(), k -> new ArrayList<>()).add(bug.getId());
            for (String tag : bug.getTags()) {
                tagToBugs.computeIfAbsent(tag, k -> new ArrayList<>()).add(bug.getId());
            }
        }

        public Bug getBug(String id) {
            return bugs.get(id);
        }

        public List<Bug> getBugsByCommit(String commitHash) {
            List<String> bugIds = commitToBugs.getOrDefault(commitHash, Collections.emptyList());
            return bugIds.stream().map(bugs::get).filter(Objects::nonNull).collect(Collectors.toList());
        }

        public List<Bug> getBugsByTag(String tag) {
            List<String> bugIds = tagToBugs.getOrDefault(tag, Collections.emptyList());
            return bugIds.stream().map(bugs::get).filter(Objects::nonNull).collect(Collectors.toList());
        }

        public List<Bug> getBugsByStatus(Status status) {
            return bugs.values().stream()
                    .filter(b -> b.getStatus() == status)
                    .sorted(Comparator.comparingLong(Bug::getCreatedAt))
                    .collect(Collectors.toList());
        }

        public List<Bug> getBugsBySeverity(Severity severity) {
            return bugs.values().stream()
                    .filter(b -> b.getSeverity() == severity)
                    .sorted(Comparator.comparingLong(Bug::getCreatedAt))
                    .collect(Collectors.toList());
        }

        public void updateBugStatus(String bugId, Status newStatus) {
            Bug bug = bugs.get(bugId);
            if (bug != null) {
                bug.updateStatus(newStatus);
            }
        }

        public void addTagToBug(String bugId, String tag) {
            Bug bug = bugs.get(bugId);
            if (bug != null) {
                bug.addTag(tag);
                tagToBugs.computeIfAbsent(tag, k -> new ArrayList<>()).add(bugId);
            }
        }

        public int getBugCount() {
            return bugs.size();
        }

        public Map<Status, Long> getStatusCounts() {
            return bugs.values().stream()
                    .collect(Collectors.groupingBy(Bug::getStatus, Collectors.counting()));
        }

        public Map<Severity, Long> getSeverityCounts() {
            return bugs.values().stream()
                    .collect(Collectors.groupingBy(Bug::getSeverity, Collectors.counting()));
        }
    }
}
