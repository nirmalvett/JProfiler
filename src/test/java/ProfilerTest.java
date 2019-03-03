import com.vettiankal.ExecutionTree;
import com.vettiankal.Profiler;
import com.vettiankal.ThreadInfo;

import java.util.Map;

public class ProfilerTest {

    public static void main(String... args) {
        Profiler profiler = new Profiler(10, 20000);
        profiler.start((trees) -> {
            for(Map.Entry<ThreadInfo, ExecutionTree> tree : trees.entrySet()) {
                System.out.println(tree.getKey());
                System.out.println(tree.getValue());
            }
        });
        while(true) {
            a(3);
        }
    }

    public static int a(int a) {
        int j = 0;
        for(int i = 0; i < 10; i++) {
            j += a;
        }

        return b(j);
    }

    public static int b(int b) {
        int j = 0;
        for(int i = 0; i < 10000; i++) {
            j += b;
        }

        return c(j);
    }

    public static int c(int c) {
        int j = 0;
        for(int i = 0; i < 1000000; i++) {
            j += c;
        }

        return j;
    }

}
