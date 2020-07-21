public class Job {
    private int id;         // 任务编号
    private double runTime; // 运行时间
    private int mGroupID;   // 设备组
    private int number;     // 任务数量
    private int nextid;     // 后继任务
    private int prepareTime;// 准备时间

//    结果
    private int machineID;        // 设备
    private double start;      // 计划开始时间
    private double end;        // 计划结束时间

    public Job(int id, double runTime, int mGroupID, int number, int nextid, int prepareTime)  {
        this.id = id;
        this.runTime = runTime;
        this.mGroupID = mGroupID;
        this.number = number;
        this.nextid = nextid;
        this.prepareTime = prepareTime;
    }

    public double getTotalTime() {
        return runTime*number + prepareTime;
    }

    public double getSingleTime() {
        return runTime*number;
    }

    public void setMachineID(int mID) {
        this.machineID = mID;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public double getRunTime() {
        return runTime;
    }

    public int getmGroupID() {
        return mGroupID;
    }

    public int getNumber() {
        return number;
    }

    public int getNextid() {
        return nextid;
    }

    public int getPrepareTime() {
        return prepareTime;
    }

    public int getMachineID() {
        return machineID;
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }
}
