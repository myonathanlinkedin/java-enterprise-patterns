import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Video> videos = new ArrayList<>();
        List<Integer> classes = Dialer.getClasses(videos);

        for (Integer classId : classes) {
            Video video = new Video(classId);
            System.out.println(video.getClasses().size());
        }
    }
}
