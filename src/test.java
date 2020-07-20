import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import com.yuxingwang.gantt.Config;
import com.yuxingwang.gantt.GanttChart;
import com.yuxingwang.gantt.model.GanttModel;
import com.yuxingwang.gantt.model.Task;
import com.yuxingwang.gantt.ui.TimeUnit;

public class test {
    public static void main(String[] args) {
        GanttChart gantt = new GanttChart();
        gantt.setTimeUnit(TimeUnit.Day);

        GanttModel model = new GanttModel();

        // 项目开始时间和结束时间：
        GregorianCalendar startGC = new GregorianCalendar(2020, 7, 1);  // 2020年8月1日。1月是0
        model.setKickoffTime(startGC);
        model.setDeadline(new GregorianCalendar(2020, 8, 15));

        GregorianCalendar start = (GregorianCalendar) startGC.clone();
        start.add(5, (int)320.41/16/60);
        GregorianCalendar endgc = (GregorianCalendar) startGC.clone();
        endgc.add(5, (int)330.41/16/60+3);
        Task task = new Task("11100", start, endgc);
        task.setBackcolor(Color.CYAN);
        System.out.println("start="+new SimpleDateFormat("yyyyMMdd").format(start.getTime())
                            +", end="+new SimpleDateFormat("yyyyMMdd").format(endgc.getTime()));


        model.addTask(task);
        gantt.setModel(model);
        System.out.println(gantt);
        //指定路径，生成图片：
        String filePath = "F:/ganttt4.jpg";
        try {
            gantt.generateImageFile(filePath);
        }
        catch (IOException e) {
            System.out.println("gantt error: "+e);
        }
    }
}