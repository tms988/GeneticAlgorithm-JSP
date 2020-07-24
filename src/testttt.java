import java.text.SimpleDateFormat;
import java.util.*;

public class testttt {
    public static void main(String[] args) {
        Integer[] arr = {1,2,4,2,3};
        List<Integer> list = Arrays.asList(arr);
        for (int i :list) System.out.println(i);
        System.out.println("===========");
        Integer[] arr2 = list.toArray(new Integer[list.size()]);
        for (int i: arr2) System.out.println(i);
    }
}
