import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class testttt {
    public static void main(String[] args) {
        Calendar c=Calendar.getInstance();
        System.out.println(c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE));

    }
}
