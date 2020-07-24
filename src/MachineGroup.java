import java.util.*;

/**
 * key为设备组编号；value为设备组里所有设备对应的Machine对象组成的列表
 */
public class MachineGroup {
    private HashMap<Integer, List<Machine>> map;

    public MachineGroup() {
        this.map = new HashMap<>();
    }

    /**
     * 往map里添加设备组及它的一个设备
     * @param groupID 设备组编号
     * @param machine 设备在组里的编号
     */
    public void add(int groupID, Machine machine) {
        List<Machine> list = map.getOrDefault(groupID, new ArrayList<>());
        list.add(machine);
        map.put(groupID, list);
    }

    /**
     * 置零MachineGroup中所有设备的已使用时间
     */
    public void resetUsedTime() {
        for (int groupID : map.keySet()) {
            for (Machine m : map.get(groupID))
                m.resetUsedTime();
        }
    }

    /**
     * timer：key是groupID, value:是[每个设备上次运行的截止时间]
     * @return timer的HashMap
     */
    public HashMap<Integer, double[]> createTimer() {
        HashMap<Integer, double[]> timer = new HashMap<>(map.size());
        for(int id : map.keySet()) {
//            System.out.println("id=" + id + ", size="+map.get(id).size());
//            Integer[] tmp = {0, map.get(id).size()};
            timer.put(id, new double[map.get(id).size()]);
        }
        return timer;
    }

    /**
     * 找到最早可使用（未被占用）的设备在数组中的index。优先返回从未被使用过的设备
     * @param counts 设备使用时间对应的计数器
     * @return 设备在数组中的index
     */
    public static int findNearMachine(double[] counts) {
        double min = Double.MAX_VALUE;
        int result=0;
        for (int i=0; i<counts.length; i++) {
            if (counts[i] < min) {
                result = i;
                min = counts[i];
            }
            if (min==0) break;
        }
        return result;
    }

    /**
     * 58，59两个设备组的设备，是虚拟设备，可以理解成不限设备数量，不限产能
     * @param groupID 需要查询的设备组编号
     * @return 该设备组是否是虚拟设备
     */
    public boolean isVirtual(int groupID) {
        return (groupID==58 || groupID==59);
    }

    /**
     *
     * @param groupID 需要查询的设备组编号
     * @return 该设备组中所有设备编号组成的list
     */
    public List<Machine> getByGroupID (int groupID) {
        return map.get(groupID);
    }

    public int getGroupSize(int groupID) {
        if (map.get(groupID)==null) return 0;
        return map.get(groupID).size();
    }

    /**
     *
     * @return 所有的设备组编号组成的set
     */
    public Set<Integer> getGroupIDs () {
        return map.keySet();
    }

    public HashMap<Integer, List<Machine>> getMap() {
        return map;
    }
}

/**
 * 一个Machine对应一台设备，记录了设备的信息（它所属的设备组编号、设备编号），并存储排程后的结果（占用时间）
 */
class Machine {
    private int groupID;
    private int machineID;
    private double usedTime;

    public Machine(int groupID, int machineID) {
        this.groupID = groupID;
        this.machineID = machineID;
        this.usedTime = 0d;
    }

    /**
     * 增加设备的已占用时间
     * @param addition 增加量
     */
    public void addUsedTime(double addition) {
        this.usedTime += addition;
    }

    /**
     * 置零占用时间
     */
    public void resetUsedTime() {
        this.usedTime = 0d;
    }

    public double getUsedTime() {
        return usedTime;
    }

    public int getMachineID() {
        return machineID;
    }
}
