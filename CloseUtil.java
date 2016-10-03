/**
 * Created by sujiaxu on 16/7/11.
 */
import java.io.Closeable;
import java.io.IOException;


public class CloseUtil {
    public static void closeAll(Closeable ... io)
    {
        for(Closeable temp:io)
        {
            if(null==temp)
            {
                try {
                    temp.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}