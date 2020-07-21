import java.util.*;

public class MachineGroup {
    private HashMap<Integer, List<Machine>> map; // key为设备组编号；value为设备组里有的设备（Machine对象）

    public MachineGroup() {
        this.map = new HashMap<>();
    }

    public void add(int groupID, Machine machine) {
        List<Machine> list = map.getOrDefault(groupID, new ArrayList<>());
        list.add(machine);
        map.put(groupID, list);
    }

    public void resetUsedTime() {
        for (int groupID : map.keySet()) {
            for (Machine m : map.get(groupID))
                m.resetUsedTime();
        }
    }

    public HashMap<Integer, double[]> createTimer() {    // key: groupID, value: [每个设备上次运行的截止时间]
        HashMap<Integer, double[]> timer = new HashMap<>(map.size());
        for(int id : map.keySet()) {
//            System.out.println("id=" + id + ", size="+map.get(id).size());
//            Integer[] tmp = {0, map.get(id).size()};
            timer.put(id, new double[map.get(id).size()]);
        }
        return timer;
    }

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

    public boolean isVirtual(int groupID) { // 58，59两个设备组的设备，是虚拟设备，可以理解成不限设备数量，不限产能
        return (groupID==58 || groupID==59);
    }

    public List<Machine> getByGroupID (int groupID) {
        return map.get(groupID);
    }

    public int getGroupSize(int groupID) {
        if (map.get(groupID)==null) return 0;
        return map.get(groupID).size();
    }

    public Set<Integer> getGroupIDs () {
        return map.keySet();
    }

    public HashMap<Integer, List<Machine>> getMap() {
        return map;
    }
}

class Machine {
    private int groupID;
    private int machineID;
    private double usedTime;

    public Machine(int groupID, int machineID) {
        this.groupID = groupID;
        this.machineID = machineID;
        this.usedTime = 0d;
    }

    public void addUsedTime(double addition) {
        this.usedTime += addition;
    }

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
