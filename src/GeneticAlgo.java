import com.csvreader.*;
import java.nio.charset.Charset;
import java.awt.Color;
import java.io.IOException;
import java.util.*;
import java.text.DecimalFormat;

import com.yuxingwang.gantt.GanttChart;
import com.yuxingwang.gantt.model.GanttModel;
import com.yuxingwang.gantt.model.Task;
import com.yuxingwang.gantt.ui.TimeUnit;

public class GeneticAlgo {
    private final int planedTime = 26880;
    private int strategy, order;
//    strategy：排程策略。1超过天数越小越好；2超期任务数量越少越好；3设备利用率越高越好
//    order：排程规则，1正排；2倒排

    private final int hourPerDay = 16;  // 一天工作16小时
    private final int finalMissionLen = 2*hourPerDay*60; // 六位数任务的时间，单位：分钟。两天

    private final int populationNumber = 10;    // 备选方案个数？
    private final double crossProbability = 0.95;
    private final double mutationProbability = 0.05;
    private int mutateTime=2;  // mutate次数
    private int selectNumber=3;

    private int jobNumber;  // 任务链个数
    private int chromosomeSize; // 整个任务流程长度（个数）

    private JobGroup jg= new JobGroup();
    List<List<Integer>> jobList = new ArrayList<>();
    private JobRelationship jr = new JobRelationship();
    private MachineGroup mg = new MachineGroup();

    private Set<Gene> geneSet = new HashSet<>();
    private Random random = new Random();
    GanttChart gantt = new GanttChart();
    // 测试用
    DecimalFormat df = new DecimalFormat( "0.00 ");

    public GeneticAlgo(int strategy, int order) {
        this.strategy = strategy;
        this.order = order;
    }

    private void missionGantt(Result result, String filename) {
        //设置甘特图的时间刻度的单位，如以星期为单位，则时间轴上的每一刻度代表一个星期：
        gantt.setTimeUnit(TimeUnit.Hour);

        //设置甘特图各个元素的颜色，宽度等，详细配置项请参考API文档中的Config类的说明。
        //Config config = gantt.getConfig();
        //config.setWorkingTimeBackColor(Color.red);

        GanttModel model = new GanttModel();
        // 项目开始时间和结束时间：
        GregorianCalendar totalStart = new GregorianCalendar(2020, 7, 3,0,0);  // 2020年8月3日。1月是0
        model.setKickoffTime(totalStart);
        GregorianCalendar totalEnd = (GregorianCalendar) totalStart.clone();
        totalEnd.add(Calendar.DATE, planedTime/hourPerDay/60);
        model.setDeadline(totalEnd);

        // 一个Task对象在甘特图中表现为一条横线。
        //每个Task对象都可以包含任意多的子Task对象，形成树状的任务模型。如果一个Task对象包含子Task,
        //则自动成为对象组，对象组仍然是Task对象，但是在甘特图中显示为不同的形状。
        //如下例，taskGroup就是任务组，包含了两个子任务，task1和task2：
        Task taskGroup = new Task("taskGA", totalStart, totalEnd);
        Task[] tasks = new Task[jobNumber];
        for (int i=0; i<jobNumber; i++) {
            double endTime = findEndTime(result.endTime[i]);
            GregorianCalendar start = (GregorianCalendar) totalStart.clone();
            start.add(Calendar.HOUR, (int)(result.startTime[i][0]/60/hourPerDay*24));
            GregorianCalendar endgc = (GregorianCalendar) totalStart.clone();
            endgc.add(Calendar.HOUR, (int)(endTime/60/hourPerDay*24+1));
//            为确保日期准确，换算成一天工作24个小时
            int startID = jobList.get(i).get(0);
            int endID = jobList.get(i).get(jobList.get(i).size()-1);
            String title;
            if (startID==endID) title = startID+" ";
            else title = startID+"~"+endID;
            tasks[i] = new Task(title, start, endgc);
            tasks[i].setBackcolor(Color.CYAN);
//            System.out.println("task["+i+"]: start="+new SimpleDateFormat("yyyyMMdd").format(start.getTime())
//                                +", end="+new SimpleDateFormat("yyyyMMdd").format(endgc.getTime()));
            System.out.println("task["+i+"]: start="+df.format(result.startTime[i][0])+", end="+df.format(endTime));
        }
        taskGroup.add(tasks);

        // 指定任务之间的依赖关系。如果一项任务需要在另一项任务完成之后才能开始，那么需要将另一项任务设为此任务的前置任务。
//        task2.addPredecessor(task1);

        model.addTask(taskGroup);
        gantt.setModel(model);
        System.out.println(gantt);
        try {
            gantt.generateImageFile(filename);
            System.out.println("Gantt chart created. Path: "+filename);
        }
        catch (IOException e) {
            System.out.println("gantt error: "+e);
        }
    }

    private List<Integer> cutArray(List<Integer> list, int key) {
        List<Integer> result = new ArrayList<>();
        int index=0;
        while (list.get(index)!=key) index++;
        while (index<list.size()) {
            int tmp = list.remove(index);
            result.add(tmp);
        }
        return result;
    }

    private int getIndex(List<Integer> list) {
        int index = 0;
        int len = jobList.size();
        while (index<len && !jobList.get(index).equals(list)) index++;
        return index;
    }

    private Job getJobByPosition(int index, int c) {
        int id = jobList.get(index).get(c);
        return jg.getByID(id);
    }

    private List<Integer> createIndexList() {
        List<Integer> indexList = new ArrayList<>(chromosomeSize);  // {0 1 2 2 2 2 2 2 2 2 2 3 4 ...}
        for (int index=0; index<jobList.size(); index++) {
            for (int j=0; j<jobList.get(index).size(); j++) indexList.add(index);
        }
        return indexList;
    }

    private double findEndTime(double[] arr) {  // 输入result.endTime[i]，输出该任务链的最后完成时间
        int endTime;
        for (endTime=0; endTime<1024 && arr[endTime+1]!=0; endTime++){}
        return arr[endTime];
    }

    private List<Integer> makeList(int n) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < n; i++) result.add(i);
        return result;
    }

    private int[][] addCounter(int[] arr) {
//            result:！！下面这个要转置一下
//            0 1 1 2 0 1 8 1 8 （输入的arr）
//            0 0 1 0 1 2 0 3 1 （counter是从0开始的）
        int[] counter = new int[arr.length];
        int[][] result = new int[arr.length][2];
        for (int i=0; i<arr.length; i++) {
            result[i][0] = arr[i];
            result[i][1] = counter[arr[i]]++;
        }
        return result;
    }

    private int crossHelper(int[] arr, int index, int end) {   // end是implant的end
        if (!jr.hasPredecessor(arr[index])) return -1;
        HashSet<Integer> predecessor = jr.getPredecessor(arr[index]);
        int result = -1;
        for (int j=end+1; j<arr.length; j++) {
            if (arr[index]==arr[j]) return result;
            if (predecessor.contains(arr[j])) {
                result = j;
            }
        }
        return result;
    }

    public void readMission(String filename) {
        HashMap<Integer, List<Integer>> map = new HashMap<>();
//        先达成{{308,309,310,311,...，316,226776} 一行是一个队列
//              {317,226776}}这样的list            总的用ArrayList串起来
//        顺便看看能不能加进设备组信息
        try {
            CsvReader csvReader = new CsvReader(filename, ',', Charset.forName("UTF-8"));
            csvReader.readHeaders();    // 略过表头
            int count = 0;
//            while (csvReader.readRecord() && count<77) {
            while (csvReader.readRecord()) {
                int id = Integer.parseInt(csvReader.get(0));
                double runTime = Double.parseDouble(csvReader.get(2));
                int machineid = (!csvReader.get(3).equals("")) ? Integer.parseInt(csvReader.get(3)) : -1;
                int number = Integer.parseInt(csvReader.get(4));
                int nextid = Integer.parseInt(csvReader.get(5));
                int prepareTime = Integer.parseInt(csvReader.get(6));

                Job jtmp = new Job(id, runTime, machineid, number, nextid, prepareTime);
                List<Integer> qtmp;
                if (!map.containsKey(id)) {
                    qtmp = new ArrayList<>();
                    qtmp.add(id);
                    map.put(id, qtmp);
                    jobList.add(qtmp);
                } else {
                    qtmp = map.get(id);
                }
                if (nextid != -1) {
                    if (map.containsKey(nextid)) {  // 有多个前驱任务步骤，截断之后的
                        // {21,22,23,33,56,57}
                        //       {32,33}        要把{33,56,57}截出，变成一个新的
                        if (map.get(nextid).size()!=1) {
                            // size!=1用来防止cutArray把只有一个元素的list也截断了（=删除旧的复制新的）导致jobList中出现空的list
                            List<Integer> newList = cutArray(map.get(nextid), nextid);
                            int beforeIndex1 = getIndex(map.get(nextid));
                            int beforeIndex2 = getIndex(map.get(id));
                            map.put(nextid, newList);
                            jobList.add(newList);
                            int afterIndex = jobList.size() - 1;
                            jr.add(beforeIndex1, afterIndex);
                            jr.add(beforeIndex2, afterIndex);
//                            System.out.print("id=" + id + ", nextid=" + nextid + ", ");
//                            System.out.print("before1=" + beforeIndex1 + ", before2=" + beforeIndex2 + ", ");
//                            System.out.println("after=" + afterIndex);
                        }
                        else {
                            int beforeIndex = getIndex(map.get(id));
                            int afterIndex = getIndex(map.get(nextid));
                            jr.add(beforeIndex, afterIndex);
//                            System.out.print("!id=" + id + ", nextid=" + nextid + ", ");
//                            System.out.print("before=" + beforeIndex + ", ");
//                            System.out.println("after=" + afterIndex);
                        }
                    }
                    else {
                        map.put(nextid, qtmp);
                        qtmp.add(nextid);
                    }
                }
                jg.add(id, jtmp);
                count++;
            }

//            删除空列表
//            List<List<Integer>> ltmp = new ArrayList<>();
//            ltmp.add(new ArrayList<Integer>());
//            jobList.removeAll(ltmp);
            this.chromosomeSize = count;
            this.jobNumber = jobList.size();
            jr.createCounter(jobList, jobNumber);

//            打印relationship
            System.out.println("relationship: ");
            for (int k : jr.getMap().keySet()) {
                System.out.print(k + " - set: ");
                HashSet<Integer> set = jr.getMap().get(k);
                for (int i : set) {
                    System.out.print(i+" ");
                }
                System.out.println();
            }

            // 打印读取到的所有任务链
            System.out.println("from csv:");
            for (int x=0; x<jobList.size(); x++) {
                List<Integer> q = jobList.get(x);
                System.out.print("index="+x+". ");
                for (Integer i : q) {
                    System.out.print(i+" "+jg.getByID(i).getmGroupID()+", ");
                }
                System.out.println();
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    public void readMachine(String filename) {
        try {
            CsvReader csvReader = new CsvReader(filename, ',', Charset.forName("UTF-8"));
            csvReader.readHeaders();    // 略过表头
            while (csvReader.readRecord()) {
                 if (Integer.parseInt(csvReader.get(2))==0 || csvReader.get(1).equals("")) { // 略过不可用和没有设备组项的
//                     System.out.println("忽略: groupID="+csvReader.get(1)+"machineID="+csvReader.get(0)+"可用="+csvReader.get(2));
                     continue;
                }
                int machineID = Integer.parseInt(csvReader.get(0));
                int groupID = Integer.parseInt(csvReader.get(1));
                mg.add(groupID, new Machine(groupID, machineID));
//                System.out.println("add: groupID="+groupID+" machineID="+machineID+" 可用="+csvReader.get(2));
            }

//            System.out.println("machine:");
//            for(int id : mg.getMap().keySet()) {
//                System.out.print("id="+id+". ");
//                for (Machine x : mg.getByGroupID(id)) {
//                    System.out.print(x.getMachineID()+", ");
//                }
//                System.out.println();
//            }
        }
        catch (IOException e) {
            System.out.println(e);
            return;
        }
    }

    public void csvMissionWriter(String filename) {
        try {
            CsvWriter writer = new CsvWriter(filename, ',', Charset.forName("UTF-8"));
            String[] headers = {"任务", "预完成时间", "运行时间", "设备组", "任务数量", "后继任务", "准备时间",
                                "设备", "计划开始时间", "计划结束时间", "排程时间"};  // 表头
            writer.writeRecord(headers);

            for (int index=0; index<jobNumber; index++) {
                List<Integer> list = jobList.get(index);
                for (int id : list) {
                    Job job = jg.getByID(id);
                    String[] content = new String[11];
                    content[0] = job.getId()+"";
                    if (job.getId()>100000) content[1] = planedTime+"";
                    content[2] = job.getRunTime()+"";
                    content[3] = job.getmGroupID()+"";
                    content[4] = job.getNumber()+"";
                    content[5] = job.getNextid()+"";
                    content[6] = job.getPrepareTime()+"";
                    content[7] = job.getMachineID()+"";
                    content[8] = df.format(job.getStart())+"";
                    content[9] = df.format(job.getEnd())+"";
                    content[10] = "2020.8.2";
                    writer.writeRecord(content);
                }
            }
            System.out.println("Mission result csv is created. Path: "+filename);
            writer.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    public void csvMachineWriter(String filename) {
        try {
            CsvWriter writer = new CsvWriter(filename, ',', Charset.forName("UTF-8"));
            String[] headers = {"设备", "设备组", "占用时间"};  // 表头
            writer.writeRecord(headers);

            for (int groupID : mg.getGroupIDs()) {   // 设备组
                for (Machine m : mg.getByGroupID(groupID)) {    // 单个设备
                    String[] content = new String[3];
                    content[0] = m.getMachineID()+"";
                    content[1] = groupID+"";
                    content[2] = df.format(m.getUsedTime())+"";
                    writer.writeRecord(content);
                }
            }
            System.out.println("Machine result csv is created. Path: "+filename);
            writer.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

//    随机出初始解
    public void initialPopulation() {
        List<Integer> indexList = createIndexList();  // {0 1 2 2 2 2 2 2 2 2 2 3 4 ...}
        for (int i = 0; i < populationNumber; i ++) {
            Gene g = new Gene();
            ArrayList<Integer> indexCopy = new ArrayList<>(indexList);
            g.chromosome = new int[chromosomeSize];
            int[] count = new int[jobNumber];

            int j=0;
            while (j<chromosomeSize) {
                int tmp = indexCopy.remove(random.nextInt(indexCopy.size()));
                if (jr.hasPredecessor(tmp) && !jr.canDo(tmp, count)) {
                    indexCopy.add(tmp);
                    continue;
                }
                g.chromosome[j] = tmp;
                count[tmp]++;
                j++;
            }

            g.fitness = calculateFitness(g).fitness;
            geneSet.add(g);

            System.out.println("chromosome:");
            for(int n : g.chromosome) System.out.print(n+" ");
            System.out.println();
            System.out.println("fitness="+df.format(g.fitness));
            System.out.println("==========================");
        }
    }

    // 计算适应度
    public Result calculateFitness(Gene g) {
        Result result;
        if (order==1) {
            result = positiveFitness(g);
        }
        else {
            result = reverseFitness(g);
        }
        if (strategy==1) {
            result.fitness = result.fulfillTime;
        }
        else if (strategy==2) { // 计算超期任务的数量。加权。权重按1-3天 0.3，3-7天 0.6，7-14天 1，15-30天 2，30之后 3
            double overdue = 0;
            for (int i=0; i<jobNumber; i++) {
                double endTime = findEndTime(result.endTime[i]);
                if (endTime>planedTime && jobList.get(i).get(0)>100000) {   // 六位数任务超期了
                    if (endTime - planedTime < 3) overdue+=0.3;
                    else if (endTime - planedTime < 7) overdue+=0.6;
                    else if (endTime - planedTime < 14) overdue+=1;
                    else if (endTime - planedTime < 30) overdue+=2;
                    else overdue+=3;
                }
            }
            result.fitness = overdue;
        }
        else if (strategy==3){  // 计算设备空闲时间越少越好=空闲比率
            double spareTIme = 0d;
            format(result);
            for (int groupID : mg.getGroupIDs()) {
                for (Machine m : mg.getByGroupID(groupID)) {
                    spareTIme = spareTIme + (1 - m.getUsedTime() / planedTime);
                }
            }
            result.fitness = spareTIme;
            System.out.println("3: "+result.fitness);
        }

        return result;
    }

    public Result positiveFitness(Gene g) {
        Result result = new Result(jobNumber);
        int[] count = new int[jobNumber];
        HashMap<Integer, double[]> machineTimer = mg.createTimer();
        for (int i=0; i<chromosomeSize; i ++) {
            int index = g.chromosome[i];
            int c = count[index]++;   // 第index条任务链的第c个任务步骤，预置进行到下一步
            Job job = getJobByPosition(index, c);
            int mid = job.getmGroupID();    // 这个步骤使用的设备=machineGroupID
            if (mid<0) { // 设备组为-1的，即六位数任务
                double latest = Double.MIN_VALUE;
                if (jr.hasPredecessor(index)) {
                    for (int predecessor : jr.getPredecessor(index)) {
                        double endTime = findEndTime(result.endTime[predecessor]);
                        latest = Math.max(latest, endTime);
                    }
                }
                else {  // 没有前驱任务链，从头开始
                    latest = 0d;
                }
                result.startTime[index][c] = latest;
                result.endTime[index][c] = latest + finalMissionLen;
                result.fulfillTime = Math.max(result.fulfillTime, result.endTime[index][c]);
                continue;
            }

//            System.out.println("index="+index+", id="+job.getId()+", mid="+mid);
//            for (double d : machineTimer.get(mid)) System.out.print(df.format(d)+" ");
//            System.out.println();

            int nearMachine = MachineGroup.findNearMachine(machineTimer.get(mid));
//            开始时间是：上一步骤的结束时间、机器占用结束时间 的最大值（最晚的那个）
            if (c==0) { // 是任务链的最开始
                if (!jr.hasPredecessor(index)) { // 没有前驱任务链时
                    result.startTime[index][c] = (mg.isVirtual(mid)) ? 0 : machineTimer.get(mid)[nearMachine];
                    // 是虚拟设备的话，无前驱任务链时可以直接开始。不然，从有可用设备时开始
                }
                else {// 有前驱任务链，要找出所有前驱任务链中最晚完成的
                    double latest = Double.MIN_VALUE;
                    for (int predecessor : jr.getPredecessor(index)) {
                        double endTime = findEndTime(result.endTime[predecessor]);
                        latest = Math.max(latest, endTime);
                    }
                    result.startTime[index][c] = (latest<machineTimer.get(mid)[nearMachine]) ? machineTimer.get(mid)[nearMachine] :
                            Math.max(latest-job.getPrepareTime(), machineTimer.get(mid)[nearMachine]);
                }
            }
            else if (mg.isVirtual(mid)) { // 是虚拟设备组，有无限个可用设备。且不是任务链最开始
                result.startTime[index][c] = Math.max(result.endTime[index][c-1] - job.getPrepareTime(), 0); // 可以在上个任务开始前提前准备
            }
            else if (result.endTime[index][c-1] < machineTimer.get(mid)[nearMachine]) {    // 前置任务完成时没有设备
                result.startTime[index][c] = machineTimer.get(mid)[nearMachine];
            }
            else {  // 设备有空余时，前置任务还没完成，设备可以提前准备
                result.startTime[index][c] = Math.max(result.endTime[index][c-1]-job.getPrepareTime(), machineTimer.get(mid)[nearMachine]);
                // 在设备准备完毕后正好前置任务做完
            }

            machineTimer.get(mid)[nearMachine] = result.startTime[index][c] + job.getTotalTime();
            result.useMachine[index][c] = nearMachine;
            result.endTime[index][c] = machineTimer.get(mid)[nearMachine];
            result.fulfillTime = Math.max(result.fulfillTime, machineTimer.get(mid)[nearMachine]); // 更新为最终全部完成的时间
        }

        return result;
    }

    // 计算适应度
    public Result reverseFitness(Gene g) {
        Result result = new Result(jobNumber);
        int[] count = new int[jobNumber];
        HashMap<Integer, double[]> machineTimer = mg.createTimer(); // 从endTime开始数，距离endTime有多久
        for (int i=chromosomeSize; i>=0; i--) {
            int index = g.chromosome[i];
            int c = jobList.get(index).size() - count[index] - 1; // 第index条任务链的倒数第c个任务步骤
            Job job = getJobByPosition(index, jobList.get(index).size() - c - 1);
            int mid = job.getmGroupID();    // 这个步骤使用的设备=machineGroupID
            if (mid<0) { // 设备组为-1的，即六位数任务
                result.startTime[index][c] = (finalMissionLen<planedTime) ? planedTime-finalMissionLen : 0;
                result.endTime[index][c] = (finalMissionLen<planedTime) ? planedTime : finalMissionLen;
                result.fulfillTime = Math.max(result.fulfillTime, result.endTime[index][c]);
                continue;
            }

//            System.out.println("index="+index+", id="+job.getId()+", mid="+mid);
//            for (double d : machineTimer.get(mid)) System.out.print(df.format(d)+" ");
//            System.out.println();

            int nearMachine = MachineGroup.findNearMachine(machineTimer.get(mid));
            // TODO: 没算准备时间
            if (count[index]==0) { // 是任务链的末尾
                if (jr.getSuccessor(index)<0) { // 没有后继任务链时
                    result.endTime[index][c] = (mg.isVirtual(mid)) ? planedTime : machineTimer.get(mid)[nearMachine];
                }
                else {// 有一个后继任务链
                    int successor = jr.getSuccessor(index);
                    double successStartTime = result.startTime[successor][0];
                    result.endTime[index][c] = Math.min(successStartTime, planedTime - machineTimer.get(mid)[nearMachine]);
                }
            }
            else if (mg.isVirtual(mid)) { // 是虚拟设备组，有无限个可用设备。且不是任务链最末
                result.endTime[index][c] = result.startTime[index][c+1];
            }
            else {
                result.endTime[index][c] = Math.min(result.startTime[index][c+1], planedTime - machineTimer.get(mid)[nearMachine]);
            }

            machineTimer.get(mid)[nearMachine] = result.endTime[index][c] - job.getTotalTime();
            result.useMachine[index][c] = nearMachine;
//          如果完成所需时间比预订的结束时间更长，预订开始时间就会变成负数
            if (result.endTime[index][c] < job.getTotalTime()) {
                double diff = job.getTotalTime() - result.endTime[index][c];
                for (int j=c; j<result.endTime.length; j++) {
                    result.endTime[index][j] += diff;
                    result.startTime[index][j] += diff;
                    result.useMachine[index][j] -= diff;
                }
            }
            result.startTime[index][c] = result.endTime[index][c] - job.getTotalTime();
            result.fulfillTime = Math.max(result.fulfillTime, machineTimer.get(mid)[nearMachine]); // 更新为最终全部完成的时间

            count[index]++;
        }

        return result;
    }

    public Gene mutate(Gene gene) {
        List<Integer> indexList = createIndexList();
        int i = 0;
        while (i<mutateTime) {
            int a = indexList.remove(random.nextInt(indexList.size()));
            int b = indexList.remove(random.nextInt(indexList.size()));
            if (!jr.canExchange(a, b)) { // 选中的两个任务链有先后关系，放回去重新选
                indexList.add(a);
                indexList.add(b);
                continue;
            }
            // 交换下标为a和b的两个
            int tmp = gene.chromosome[a];
            gene.chromosome[a] = gene.chromosome[b];
            gene.chromosome[b] = tmp;
            i++;

//            System.out.println("mutate:");
//            System.out.println("a="+a+" ("+gene.chromosome[b]+"), b="+b+" ("+gene.chromosome[a]+")");
        }
        gene.fitness = calculateFitness(gene).fitness;
        return gene;
    }

    // 选择
    public Gene select() {
        List<Integer> indexList = makeList(geneSet.size());
        HashSet<Integer> set = new HashSet<>();
        for (int i=0; i<selectNumber; i++) {
            set.add(indexList.remove(random.nextInt(indexList.size())));
        }
        Gene bestGene = new Gene(Double.MAX_VALUE);
        int i = 0;
        for (Gene gene : geneSet) {
            if (set.contains(i) && bestGene.fitness > gene.fitness) {
                bestGene = gene;
            }
            i++;
        }
        return bestGene;
    }

//     交叉
    private Gene crossover(Gene g1, Gene g2) {
        int[][] wzCounter1 = addCounter(g1.chromosome);
        int[][] wzCounter2 = addCounter(g2.chromosome);

        int a, b;
        do {
            a = random.nextInt(chromosomeSize);
            b = random.nextInt(chromosomeSize);
        } while (a==b);
        int start = Math.min(a, b);
        int end = Math.max(a, b);
//        System.out.println("crossover: start="+start+", end="+end);

        int[][] implant = Arrays.copyOfRange(wzCounter1, start, end+1);
        List<int[]> child0 = new ArrayList<>(chromosomeSize);
        for (int[] arr : wzCounter2) child0.add(arr);   // 先把g2的全放进去
        for (int[] arr : implant) {
            for (int i=0; i<child0.size(); i++) {   // 把implant里有的从child里删掉
                if (Arrays.equals(arr, child0.get(i))) {
                    child0.remove(i);
                    break;
                }
            }
        }
        // |0~start | start~end |end+1~size|
        // |    g2  |g1(implant)|    g2    |
        List<int[]> child1 = new ArrayList<>(chromosomeSize);
        for (int i=0; i<start; i++) child1.add(child0.get(i));
        for (int[] arr : implant) child1.add(arr);
        int sizeTmp = child1.size();
        for (int i=start; i<child0.size(); i++) child1.add(child0.get(i));

        int[][] tmp = child1.toArray(new int[child1.size()][2]);
        int[] childArr = new int[tmp.length];
        for (int i=0; i<tmp.length; i++) {
            childArr[i] = tmp[i][0];
        }
        // 解决前驱任务链冲突
        for (int i=start; i<=end; i++) {
            int indexTmp = crossHelper(childArr, i, end);
            if (indexTmp < 0) continue;
            // 交换两个值
            int t = childArr[indexTmp];
            childArr[indexTmp] = childArr[i];
            childArr[i] = t;
        }

        Gene child = new Gene();
        child.chromosome = childArr;
        child.fitness = calculateFitness(child).fitness;
        return child;
    }

    public Result run() {
        for (int i = 0; i < populationNumber; i++) {
            double p = (double) random.nextInt(100) / 100.0;
            if (p < mutationProbability) {
                int index = random.nextInt(geneSet.size());
                int k = 0;
                for (Gene gene : geneSet) {
                    if (k==index) {
                        mutate(gene); // 随机挑一个gene
                        break;
                    }
                    k++;
                }
            }
            else {
                Gene g1 = select(), g2 = select();
                Gene child1 = crossover(g1, g2), child2 = crossover(g2, g1);
                geneSet.add(child1);
                geneSet.add(child2);
            }
        }

        Gene bestGene = new Gene(0xffffff);
        for (Gene gene : geneSet) {
//            System.out.println("fitness: "+bestGene.fitness+"   &   "+gene.fitness);
            if (bestGene.fitness > gene.fitness) {
                bestGene = gene;
            }
//            System.out.println("choose: "+ bestGene.fitness);
//            System.out.println("=================================");
        }
        return calculateFitness(bestGene);
//        System.out.println("calculateFitness: child");
//        return calculateFitness(child1);
//        return new Result(1);
    }

    public void format(Result result) {
        mg.resetUsedTime();
        for (int index=0; index<jobNumber; index++) {
            List<Integer> jobs = jobList.get(index);
            for (int c=0; c<jobs.size(); c++) {
                int id = jobs.get(c);
                Job job = jg.getByID(id);
                job.setStart(result.startTime[index][c]);
                job.setEnd(result.endTime[index][c]);
                int pos = result.useMachine[index][c];
                if (job.getmGroupID()>0) {  // 跳过设备组为-1的
                    Machine machine = mg.getByGroupID(job.getmGroupID()).get(pos);
                    job.setMachineID(machine.getMachineID());
                    machine.addUsedTime(result.endTime[index][c] - result.startTime[index][c]);
                }
            }
        }
    }


    public static void main(String[] args) {
        String missionFilename = "F:/任务信息.csv";
        String machineFilename = "F:/设备信息small.csv";
        GeneticAlgo ga = new GeneticAlgo(1,1);
        ga.readMission(missionFilename);
        ga.readMachine(machineFilename);
        ga.initialPopulation();
        Result result = ga.run();
        ga.format(result);
        String ganttFilename = "F:/ganttTest3.jpg";
        ga.missionGantt(result, ganttFilename);
//        String output1 = "output.csv";
//        String output2 = "outputmachine.csv";
//        ga.csvMissionWriter(output1);
//        ga.csvMachineWriter(output2);

    }
}

class Gene {
    public double fitness;
    public int[] chromosome;
    public Gene() {
        fitness = 0;
    }
    public Gene(double fitness) {this.fitness = fitness;}
}

class Result {
    public double fulfillTime = 0;
    public double[][] endTime;
    public double[][] startTime;
    public int[][] useMachine;  // 在数组里的位置
    public double fitness = 0;

    public Result(int height) {
        this.startTime = new double[height][1024];
        this.endTime = new double[height][1024];
        this.useMachine = new int[height][1024];
    }
}
