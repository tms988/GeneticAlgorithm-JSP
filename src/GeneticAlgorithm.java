
import java.util.*;

class GeneticAlgorithm {
    private final int populationNumber = 60;    // 备选方案个数？
    private final double crossProbability = 0.95;
    private final double mutationProbability = 0.05;
    private int jobNumber;  // 任务链个数
    private int machineNumber;  // 设备个数
    private int processNumber; // 最长任务链的长度（个数）
    private int chromosomeSize; // 整个任务流程长度（个数）

    private final int MAX_MAT_LEN = 10;
    private int[][] machineMatrix = new int[MAX_MAT_LEN][MAX_MAT_LEN];  // 任务链[i]步骤[j]位置的设备是machineMatrix[i][j]
    private int[][] timeMatrix = new int[MAX_MAT_LEN][MAX_MAT_LEN];
    private int[][] processMatrix = new int[MAX_MAT_LEN][MAX_MAT_LEN];  // 设备[i][j]位置的会参与的任务链是processMatrix[i][j]


    private Set<Gene> geneSet = new HashSet<>();
    private Random random = new Random();
    public GeneticAlgorithm(int jobNumber, int machineNumber) {
        this.jobNumber = jobNumber;
        this.machineNumber = machineNumber;
        for (int[] matrix : this.machineMatrix) Arrays.fill(matrix, -1);
        for (int[] process : this.processMatrix) Arrays.fill(process, -1);
    }

    private List<Integer> makeList(int n) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < n; i++) result.add(i);
        return result;
    }

    private Integer[] filterArray(Integer[] arr, int filterVal) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != filterVal) {
                result.add(arr[i]);
            }
        }
        return result.toArray(new Integer[0]);
    }

    // 初始化种群
    public  void initialPopulation() {
        for (int i = 0; i < populationNumber; i ++) {
            Gene g = new Gene();
            int size = jobNumber * machineNumber;
            List<Integer> indexList = makeList(size);   // 从0到size的int组成的list
            Integer[] chromosome = new Integer[size];
            Arrays.fill(chromosome, -1);
            for (int j = 0; j < jobNumber; j++) {
                System.out.println("j:"+j);
                for (int k = 0; k < machineNumber; k ++) {
                    int index = random.nextInt(indexList.size());
                    int val = indexList.remove(index);  // 等效于从indexList里抽取一个没有用过的数字
                    if (processMatrix[j][k] != -1) {
                        chromosome[val] = j;
                    }
                }
            }

            System.out.println("chromosome:");
            for(int n : chromosome) System.out.print(n+" ");
            System.out.println();
//            System.out.println("processMatrix:");
//            for(int x=0; x<processMatrix.length; x++) {
//                for(int y=0; y<processMatrix[0].length; y++){
//                    System.out.print(processMatrix[x][y]+" ");
//                }
//                System.out.println();
//            }
//            System.out.println("machineMatrix:");
//            for(int x=0; x<machineMatrix.length; x++) {
//                for(int y=0; y<machineMatrix[0].length; y++){
//                    System.out.print(machineMatrix[x][y]+" ");
//                }
//                System.out.println();
//            }

            g.chromosome = filterArray(chromosome, -1); // 删除chromosome中的-1（因为每个任务链都有长短）
            g.fitness = calculateFitness(g).fulfillTime;
            geneSet.add(g);
        }
    }

    public List<Integer> subArray(Integer[] arr, int start, int end) {
        List<Integer> list = new ArrayList<>();
        for (int i = start; i < end; i++) list.add(arr[i]);
        return list;
    }

    // 计算适应度
    public Result calculateFitness(Gene g) {
        Result result = new Result();
        for (int i = 0; i < g.chromosome.length; i ++) {
            int jobId = g.chromosome[i];
            int processId = result.processIds[jobId];   // 这条任务链进行到第几个步骤了
            int machineId = machineMatrix[jobId][processId];    // 这个步骤使用的设备
            int time = timeMatrix[jobId][processId];    // 这个步骤需要的时间
            result.processIds[jobId] += 1;
            result.startTime[jobId][processId] = (processId==0) ? result.machineWorkTime[machineId] :
                    Math.max(result.endTime[jobId][processId - 1], result.machineWorkTime[machineId]);  // 开始时间是：上一步骤的结束时间、机器占用结束时间 的最大值（最晚的那个）
            result.machineWorkTime[machineId] = result.startTime[jobId][processId] + time;  // startTime一直在增加，所以machineWorkTime也一直在增加
            result.endTime[jobId][processId] = result.machineWorkTime[machineId];
            result.fulfillTime = Math.max(result.fulfillTime, result.machineWorkTime[machineId]);   // 更新为最终全部完成的时间
        }
        return result;
    }

    // 交叉算子
    private Gene crossGene(Gene g1, Gene g2) {
        List<Integer> indexList = makeList(chromosomeSize);
        int p1 = indexList.remove(random.nextInt(indexList.size()));
        int p2 = indexList.remove(random.nextInt(indexList.size()));

        int start = Math.min(p1, p2);
        int end = Math.max(p1, p2);

        List<Integer> proto = subArray(g1.chromosome, start, end + 1);
        List<Integer> t = new ArrayList<>();
        for (Integer c : g2.chromosome) t.add(c);
        for (Integer val : proto) {
            for (int i = 0; i < t.size(); i++) {
                if (val.equals(t.get(i))) {
                    t.remove(i);
                    break;
                }
            }
        }

        Gene child = new Gene();
        proto.addAll(t.subList(start, t.size()));
        List<Integer> temp = t.subList(0, start);
        temp.addAll(proto);
        child.chromosome = temp.toArray(new Integer[0]);
        child.fitness = (double) calculateFitness(child).fulfillTime;
        return child;
    }
    // 突变算子
    public Gene mutationGene(Gene gene, int n) {
        List<Integer> indexList = makeList(chromosomeSize);
        for (int i = 0; i < n; i++) {
            int a = indexList.remove(random.nextInt(indexList.size()));
            int b = indexList.remove(random.nextInt(indexList.size()));
            int t = gene.chromosome[a];
            gene.chromosome[a] = gene.chromosome[b];
            gene.chromosome[b] = t;
        }
        gene.fitness = calculateFitness(gene).fulfillTime;
        return gene;
    }

    // 选择个体
    public Gene selectGene(int n) {
        List<Integer> indexList = makeList(geneSet.size());
        Map<Integer, Boolean> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            map.put(indexList.remove(random.nextInt(indexList.size())), true);
        }
        Gene bestGene = new Gene(0xfffff);
        int i = 0;
        for (Gene gene : geneSet) {
            if (map.containsKey(i)) {
                if (bestGene.fitness > gene.fitness) {
                    bestGene = gene;
                }
            }
            i ++;
        }
        return bestGene;
    }

    public Result run(List<List<Integer[]>> job) {
        int jobSize = job.size();

        for (int i = 0; i < jobSize; i ++) {
            chromosomeSize += job.get(i).size();
            processNumber = Math.max(processNumber, job.get(i).size());
            for (int j = 0; j < job.get(i).size(); j ++) {
                machineMatrix[i][j] = job.get(i).get(j)[0];
                timeMatrix[i][j] = job.get(i).get(j)[1];
            }
        }

        for (int i = 0; i < jobSize; i++) {
            for (int j = 0;j < processNumber; j++){
                if (machineMatrix[i][j] != -1) {
                    processMatrix[i][machineMatrix[i][j]] = j;
                }
            }
        }

        initialPopulation();

        for (int i = 0; i < populationNumber; i++) {
            double p = (double) random.nextInt(100) / 100.0;
            if (p < mutationProbability) {
                int index = random.nextInt(geneSet.size());
                int k = 0;
                for (Gene gene : geneSet) {
                    if (k == index) {
                        mutationGene(gene);
                        break;
                    }
                    k ++;
                }
            } else {
                Gene g1 = selectGene(), g2 = selectGene();
                Gene child1 = crossGene(g1, g2), child2 = crossGene(g2, g1);
                geneSet.add(child1);
                geneSet.add(child2);
            }
        }
        Gene bestGene = new Gene(0xffffff);
        for (Gene gene : geneSet) {
            if (bestGene.fitness > gene.fitness) {
                bestGene = gene;
            }
        }
        return calculateFitness(bestGene);

//        return new Result();
    }

    public Gene selectGene() {
        return selectGene(3);
    }

    public Gene mutationGene(Gene gene) {
        return mutationGene(gene, 2);
    }

    static public void main(String[] args) {
        List<List<Integer[]>> job = Arrays.asList(
                Arrays.asList(new Integer[]{0, 3}, new Integer[]{1, 2}, new Integer[]{2, 2}),
                Arrays.asList(new Integer[]{0, 2}, new Integer[]{2, 1}, new Integer[]{1, 4}),
                Arrays.asList(new Integer[]{1, 4}, new Integer[]{2, 3})
        );

        int n = 3, m = 3;
        GeneticAlgorithm ga = new GeneticAlgorithm(n, m);
        Result result = ga.run(job);
        int processNumber = ga.processNumber;

        int[][] machineMatrix = ga.machineMatrix;
        System.out.println(result.fulfillTime);

        for (int i = 0; i < n; i++) {
            for (int j = 0 ; j < processNumber; j++) {
                if (machineMatrix[i][j] != -1) {
                    System.out.println(String.format("job: %d, process: %d, machine: %d, startTime: %d, endTime: %d",
                            i, j, machineMatrix[i][j], result.startTime[i][j], result.endTime[i][j]));
                }
            }
        }
    }
}



//class Gene {
//    public double fitness;
//    public Integer[] chromosome;
//    public Gene() {
//        fitness = 0;
//    }
//    public Gene(double fitness) {this.fitness = fitness;}
//}

//class Result {
//    public int fulfillTime = 0;
//    public int[] machineWorkTime = new int[1024];
//    public int[] processIds = new int[1024];
//    public int[][] endTime = new int[1024][1024];
//    public int[][] startTime = new int[1024][1024];
//}
