import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class JobRelationship {
    private HashMap<Integer, HashSet<Integer>> map;  // key: job index, value: 该index对应任务链的前置任务链index
    private int[] counter;

    public JobRelationship() {
        this.map = new HashMap<>();
    }

    public void add(int before, int after) {
        HashSet set = map.getOrDefault(after, new HashSet<>());
        set.add(before);
        map.put(after, set);
    }

    public boolean hasPredecessor(int a) {
        return (map.containsKey(a));
    }

    public int getSuccessor(int a) {
        for (int preIndex : map.keySet()) {
            for (int index : map.get(preIndex)) {
                if (a==index) return preIndex;
            }
        }
        return -1;
    }

    public HashSet<Integer> getPredecessor(int a) {
        return map.get(a);
    }

    public boolean canDo(int index, int[] c) { // 前置任务是否全部做完
        if (!hasPredecessor(index)) return true;
        for (int preIndex : map.get(index)) {
            if (c[preIndex]<counter[preIndex]) return false;
        }
        return true;
    }

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
