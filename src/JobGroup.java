import java.util.HashMap;
import java.util.Set;

/**
 * 把任务id和任务对象Job对应起来。key是任务编号，value是它对应的Job对象
 */
public class JobGroup {
    private HashMap<Integer, Job> map;
    private int size;

    public JobGroup() {
        this.map = new HashMap<>();
        this.size = 0;
    }

    public void add(int id, Job j) {
        map.put(id, j);
        size++;
    }

    public Job getByID(int id) {
        return map.get(id);
    }

    public int size() {
        return size;
    }

    public Set<Integer> keySet() {
        return map.keySet();
    }
}