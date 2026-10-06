import java.util.ArrayList;
import java.util.List;

public class Dialer {
    private static final int MAX_CLASSES = 10;
    private static final int MAX_VIDEO_LENGTH = 10000;

    private static class Video {
        private int[] frames;
        private int classCount;

        public Video(int[] frames) {
            this.frames = frames;
            this.classCount = 0;
        }

        public void addClass(int classId) {
            if (classId < 0 || classId >= MAX_CLASSES) {
                throw new IllegalArgumentException("Invalid class ID");
            }
            this.classCount++;
        }

        public int getClassCount() {
            return this.classCount;
        }

        public int[] getFrames() {
            return this.frames;
        }
    }

    private static class Classifier {
        private List<Integer> classes;

        public Classifier() {
            this.classes = new ArrayList<>();
        }

        public void classify(Video video) {
            int classCount = video.getClassCount();
            if (classCount > MAX_CLASSES) {
                throw new IllegalArgumentException("Video has too many classes (max " + MAX_CLASSES + ")");
            }

            for (int classId : video.getClasses()) {
                if (classes.contains(classId)) {
                    continue;
                }
                classes.add(classId);
            }
        }
    }

    public static List<Integer> getClasses(List<Video> videos) {
        List<Integer> classes = new ArrayList<>();
        for (Video video : videos) {
            Classifier classifier = new Classifier();
            video.classify(classifier);
            classes.addAll(classifier.getClasses());
        }
        return classes;
    }
}
