/**
 * Created by sujiaxu on 16/7/11.
 */
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 */
public class Receive implements Runnable{

    private DataInputStream dis ;

    private boolean isRunning = true;

    public Receive()    {
    }
    public Receive(Socket client) {
        try {
            dis = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            isRunning = false;
            CloseUtil.closeAll(dis);
        }
    }


    public String receive()
    {
        String msg = "";
        try {
            msg = dis.readUTF();
            //Client.main(new String[]{});
            if (msg.startsWith("Goodby")){
                System.out.println("you got disconnected");
                System.exit(1);
            }

        } catch (IOException e) {
            isRunning = false;
            CloseUtil.closeAll(dis);
        }
        return msg;
    }

    @Override
    public void run() {

        while(isRunning){
            System.out.println(receive());
        }
    }
}