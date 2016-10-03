import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

/**
 * Created by sujiaxu on 16/8/14.
 */
public class Client {


    public static void main(String[] args) throws IOException, InterruptedException {


        System.out.println("what is your name ");
        Scanner keyboard = new Scanner(System.in);
        String clientName = keyboard.nextLine();

        String host = "localhost";
        int port = 10007;

        InetAddress addr = InetAddress.getByName(host);
        SocketAddress socketAddr = new InetSocketAddress(addr, port);
        Socket client;



        while (true) {
            try {
                //System.out.println("tryed");
                 client = new Socket();
                client.connect(socketAddr, 3000);
                break;
            } catch (SocketTimeoutException e) {
                System.out.println("I will reconnect in 3 seconds E");
            }catch (IOException ex){
                Thread.sleep(3000);
                System.out.println("I will reconnect in 3 seconds EX");
            }
        }


        DataOutputStream dos = new DataOutputStream(client.getOutputStream());
        dos.writeUTF(clientName);


        //System.out.println("already sent");


        DataInputStream dis = new DataInputStream(client.getInputStream());
        String newportString = "";
        newportString = dis.readUTF();

        int newport = Integer.parseInt(newportString);
        //System.out.println(newport);

        if (newport == port) {
            new Thread(new Send(client, clientName)).start();
            new Thread(new Receive(client)).start();
        } else {


//            Socket client1 = new Socket("localhost", newport);
//            DataOutputStream dos1 = new DataOutputStream(client1.getOutputStream());
//            dos1.writeUTF(clientName);
//            dos1.flush();
//            System.out.println("just deted");

            InetAddress inetAddress = InetAddress.getByName("localhost");
            SocketAddress socketAddress1 = new InetSocketAddress(inetAddress,newport);
            Socket client1;

            while (true){
                try{
                    client1 = new Socket();
                    client1.connect(socketAddress1,3000);
                    break;
                } catch (SocketTimeoutException e) {
                    System.out.println("I will reconnect in 3 seconds E(the right client)");
                }catch (IOException ex){
                    Thread.sleep(3000);
                    System.out.println("I will reconnect in 3 seconds EX(the right client)");
                }
            }

            new Thread(new Send(client1, clientName)).start();
            new Thread(new Receive(client1)).start();

        }


    }
}
