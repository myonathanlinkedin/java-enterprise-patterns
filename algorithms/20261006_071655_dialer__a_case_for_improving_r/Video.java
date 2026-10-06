import java.util.ArrayList;
import java.util.List;

public class Video {
    private int[] frames;
    private int classCount;

    public Video(int[] frames) {
        this.frames = frames;
        this.classCount = 0;
    }

    public List<Integer> getClasses() {
        List<Integer> classes = new ArrayList<>();
        for (int frame : frames) {
            this.classCount++;
            if (classCount > MAX_CLASSES) {
                throw new RuntimeException("Video has too many classes (max " + MAX_CLASSES + ")");
            }
        }
        return classes;
    }
}
