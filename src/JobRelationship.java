import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * key: job index（任务链的index）, value: 该index对应任务链的前置任务链index 组成的set
 * index是指在jobList中的index
 */
public class JobRelationship {
    private HashMap<Integer, HashSet<Integer>> map;  //
    private int[] counter;

    public JobRelationship() {
        this.map = new HashMap<>();
    }

    /**
     *
     * @param before 前驱任务链的index
     * @param after 后继任务链的index
     */
    public void add(int before, int after) {
        HashSet set = map.getOrDefault(after, new HashSet<>());
        set.add(before);
        map.put(after, set);
    }

    /**
     * 该任务链是否有前驱任务链
     * @param index 需要查询的任务链的index
     * @return
     */
    public boolean hasPredecessor(int index) {
        return (map.containsKey(index));
    }

    /**
     *
     * @param index 需要查询的任务链的index
     * @return 该任务链的前驱任务链index组成的set
     */
    public HashSet<Integer> getPredecessor(int index) {
        return map.get(index);
    }

    /**
     * 该任务链是否有后继任务链，如有，返回它的index
     * @param a 需要查询的任务链的index
     * @return 如果有前驱，返回它的index（一个任务链只能有一个后继任务链），如果没有，返回-1（任务链的index都为正数，所以可以区分）
     */
    public int getSuccessor(int a) {
        for (int preIndex : map.keySet()) {
            for (int index : map.get(preIndex)) {
                if (a==index) return preIndex;
            }
        }
        return -1;
    }

    /**
     * 前置任务是否全部做完
     * @param index 需要查询的任务链的index
     * @param c 当前的计数器
     * @return
     */
    public boolean canDo(int index, int[] c) {
        if (!hasPredecessor(index)) return true;
        for (int preIndex : map.get(index)) {
            if (c[preIndex]<counter[preIndex]) return false;
        }
        return true;
    }

    /**
     * canExchange()的辅助函数
     * @param a
     * @param b
     * @return
     */
    public boolean canExchange0(int a, int b) {
        if (!this.hasPredecessor(a)) return true;
        HashSet<Integer> aSet = this.getPredecessor(a);
        boolean result = true;
        for (int predecessor : aSet) {
            if (predecessor==b) return false;
            if (this.hasPredecessor(predecessor)) result = result && canExchange0(predecessor, b);
        }
        return result;
    }

    /**
     * 两条任务链中各一个任务，它们是否可以交换位置（即是否有前后关系）
     * @param a 需要查询的任务链的index
     * @param b 需要查询的任务链的index
     * @return
     */
    public boolean canExchange(int a, int b) {
        return canExchange0(a,b) && canExchange0(b,a);
    }

    public void createCounter(List<List<Integer>> jobList, int len) {
        this.counter = new int[len];
        for (int i=0; i<jobList.size(); i++) {
            counter[i] = jobList.get(i).size();
        }
    }

    public HashMap<Integer, HashSet<Integer>> getMap() {
        return map;
    }
}
