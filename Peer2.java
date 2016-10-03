import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sujiaxu on 16/8/14.
 * <p>
 * 0.2 will deal with timeout and other
 */
public class Peer2 {

    ArrayList<BankInfo> allinfo = new ArrayList<>();

    private ArrayList<MyChannel> all = new ArrayList<>();

    public static HashMap<String, BankInfo> map = new HashMap<String, BankInfo>();

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    public static int port;

    public static int [] portArr = {10007,10014,10020,10026};

    public static void main(String[] args) throws IOException, InterruptedException {


        new Peer2().start();
    }

    public int get_the_right_port(int firstchar) {
        if (firstchar >= 104 && firstchar <= 110) {
            return 10014;
        } else if (firstchar >= 111 && firstchar <= 116) {
            return 10020;
        } else if (firstchar >= 117 && firstchar <= 122) {
            return 10026;
        } else if (firstchar >= 97 && firstchar <= 103) {
            return 10007;
        }
        return 0;


    }

    public void start() throws IOException, InterruptedException {

        boolean belonghereornot = false;

        port = 10014;//

        ServerSocket peer = new ServerSocket(port);

        DataOutputStream dos2;


        while (true) {
            Socket client = peer.accept();
            //int clientport = client.getPort();


            DataInputStream dis = new DataInputStream(client.getInputStream());

            //DataOutputStream dos = new DataOutputStream(client.getOutputStream());


            String firstMessage = dis.readUTF();

            //System.out.println("message is " + firstMessage);

            if (firstMessage.startsWith("open")){
                String openAccountName = firstMessage.substring(firstMessage.indexOf(" ")+1,firstMessage.length());
                map.put(openAccountName,new BankInfo(openAccountName,0.0f,false));
                continue;


            }else if(firstMessage.startsWith("start")){
                String startAccountName = firstMessage.substring(firstMessage.indexOf(" ")+1,firstMessage.length());
                map.get(startAccountName).insession = true;
                continue;
            }else if (firstMessage.startsWith("finish")){
                String finishAccountName = firstMessage.substring(firstMessage.indexOf(" ")+1,firstMessage.indexOf(":"));
                String UpDateBalance = firstMessage.substring(firstMessage.indexOf(":")+1,firstMessage.length());
                map.get(finishAccountName).insession = false;
                map.get(finishAccountName).balance = Float.parseFloat(UpDateBalance);
                continue;
            }

            int firstchar = (int) (firstMessage.toLowerCase().charAt(0));
            //888 means a connection from the peer
            if (firstMessage.startsWith("888") == false) {//means a new connect we have not check if it match the port or not

                if (get_the_right_port(firstchar) == port) {
                    belonghereornot = true;
                    //System.out.println("belonghere");
                } else {
                    belonghereornot = false;
                }
            }

            if ((firstMessage.startsWith("888")) || (belonghereornot == true)) {


                //from a peer or this is the right client, create the thread for a specifil client
                if (belonghereornot == true) {
                    //System.out.println("create new thread");

                    dos2 = new DataOutputStream(client.getOutputStream());
                    dos2.writeUTF(Integer.toString(port));
                    MyChannel myChannel = new MyChannel(client, firstMessage);

                    all.add(myChannel);
                    //System.out.println("caller 1");

                    new Thread(myChannel).start();


                }
                if (firstMessage.startsWith("888")) {


                    Socket realclient = peer.accept();
                    //System.out.println("I amssfdcd");


                    /*DataInputStream dis1 = new DataInputStream(realclient.getInputStream());

                    //DataOutputStream dos = new DataOutputStream(client.getOutputStream());



                    String firstMessage1 = dis1.readUTF();




                    System.out.println("message is " +firstMessage1);*/


                    MyChannel myChannel = new MyChannel(realclient, firstMessage.substring(3));

                    all.add(myChannel);

                    //System.out.println("caller 2");

                    new Thread(myChannel).start();


                }


            } else {
//I will send message to  the peer this client belong to, also send client the right port

                String Message_to_peer = "888" + firstMessage;
                int peerport = get_the_right_port(firstchar);

              /*  Socket Topeer = new Socket("localhost",peerport);
                DataOutputStream dos1 = new DataOutputStream(Topeer.getOutputStream());
                dos1.writeUTF(Message_to_peer);
                dos1.flush();*/

                InetAddress inetAddress = InetAddress.getByName("localhost");
                SocketAddress address1 = new InetSocketAddress(inetAddress, peerport);

                Socket Topper;

                while (true) {
                    try {
                        Topper = new Socket();
                        Topper.connect(address1, 2000);
                        break;
                    } catch (SocketTimeoutException e) {
                        System.out.println("I will reconnect in 2 seconds E(to the peer)");
                    } catch (IOException ex) {
                        Thread.sleep(2000);
                        System.out.println("I will reconnect in 2 seconds EX(to the peer)");
                    }
                }
                DataOutputStream dos1 = new DataOutputStream(Topper.getOutputStream());
                dos1.writeUTF(Message_to_peer);
                dos1.flush();


                Thread.sleep(300);

                dos2 = new DataOutputStream(client.getOutputStream());
                dos2.writeUTF(Integer.toString(peerport));


            }


        }


    }

    class MyChannel implements Runnable {

        String clientname;
        float balance = 0.0f;

        private DataInputStream dis;
        private DataOutputStream dos;
        private boolean isRunning = true;
        String accountinSession ="";


        public MyChannel(Socket client, String clientname) throws IOException {
            //System.out.println("client name is :" + clientname);
            this.clientname = clientname;
            if (map.containsKey(clientname)) {
                balance = map.get(clientname).balance;
            } else {

                map.put(clientname, new BankInfo(clientname, 0.0f,false));

            }

            dis = new DataInputStream(client.getInputStream());

            dos = new DataOutputStream(client.getOutputStream());

        }

        private String receive() {
           // System.out.println("4------");
            String msg = "";
            try {
                msg = dis.readUTF();
                System.out.println(msg);


            } catch (IOException e) {
                CloseUtil.closeAll(dis);
                isRunning = false;
                all.remove(this);//
            }
            return msg;

        }

        //
        private void send(String msg) {
            System.out.println("5------");
            if (null == msg || msg.equals("")) {
                return;
            }
            try {
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                CloseUtil.closeAll(dos);
                isRunning = false;
                all.remove(this);//
            }
        }

        private void Check_Send_recevie(String message) throws IOException, InterruptedException {
            //everything goes here
//            this.balance=5.0f;
//            send(String.valueOf(this.balance));
            String firstword = null;
            float amount = 0;
            String amout = null;
            String newAccountname = "";

           /* char checkit;
            if ((message.contains(" "))) {
                 checkit = message.charAt(message.indexOf(" ") + 1);
            }*/


            if ((message.contains(" "))) {
                firstword = message.substring(0, message.indexOf(" "));
                amout = message.substring(message.indexOf(" ") + 1, message.length());
                if (Character.isDigit(amout.charAt(0))) {

                    amount = Float.parseFloat(amout);
                }else {
                    newAccountname = amout;
                }


            } else {
                firstword = message;
            }
            firstword = firstword.toLowerCase();
            switch (firstword) {
                case "debit":
                    if (this.balance - amount < 0) {
                        String NEB = "you dont have enough balance to debit" + amout;
                        send(NEB);
                    } else {
                        this.balance = this.balance - amount;
                        map.put(newAccountname, new BankInfo(newAccountname, this.balance,true));
                        String DebitSuccess = "debit successful, and your balance is " + Float.toString(this.balance);
                        send(DebitSuccess);
                    }
                    break;

                case "credit":
                    this.balance = this.balance + amount;
                    map.put(newAccountname, new BankInfo(newAccountname, this.balance,true));
                    String CreditSuccess = "Credit Succcess, and your balance is " + Float.toString(this.balance);
                    send(CreditSuccess);

                    break;

                case "balance":

                    String ReturnBalance = "you have : " + Float.toString(this.balance);
                    send(ReturnBalance);
                    break;

                case "open":
                    //

                    if (map.containsKey(newAccountname)){
                        send("you can not use other people's account name");
                        return;
                    }else{

                        for (int i = 0;i<4;i++){
                            if (portArr[i]==port){
                                map.put(newAccountname,new BankInfo(newAccountname,0.0f,false));
                                continue;
                            }
                            InetAddress inetAddress2 = InetAddress.getByName("localhost");
                            SocketAddress address2 = new InetSocketAddress(inetAddress2,portArr[i]);
                            Socket other;
                            other = new Socket();
                            other.connect(address2);

                            DataOutputStream dos1 = new DataOutputStream(other.getOutputStream());
                            String sendmsg = "open "+newAccountname;
                            dos1.writeUTF(sendmsg);
                            dos1.flush();
                            //
                        }
                        send("open account successful\n");
                    }




                    break;

                case "start":
                    //
                    if (map.containsKey(newAccountname)==false){
                        send("we dont have that account in our database");
                    }
                    else if(map.get(newAccountname).insession==true){
                        send(ANSI_GREEN+"sorry! currently in session, retry in 3 seconds"+ANSI_RESET);
                        Thread.sleep(2000);

                        for (int i =0;i<6;i++){
                            Thread.sleep(2000);
                            if (map.get(newAccountname).insession==true){
                                send(ANSI_GREEN+"sorry! currently in session, retry in 3 seconds"+ANSI_RESET);
                        }else {
                                send("He just left Cusmtomer session, you can start it now");
                                break;
                            }
                        }
                    }

                    else {
                        accountinSession = newAccountname;
                        map.get(accountinSession).insession = true;
                        this.balance = map.get(accountinSession).balance;

                        for (int i = 0;i<4;i++){

                            if (portArr[i]==port){
                                // map.put(newAccountname,new BankInfo(newAccountname,0.0f,false));
                                continue;
                            }
                            InetAddress inetAddress2 = InetAddress.getByName("localhost");
                            SocketAddress address2 = new InetSocketAddress(inetAddress2,portArr[i]);
                            Socket other;
                            other = new Socket();
                            other.connect(address2);

                            DataOutputStream dos1 = new DataOutputStream(other.getOutputStream());
                            String sendmsg = "start "+newAccountname;
                            dos1.writeUTF(sendmsg);
                            dos1.flush();
                            //
                        }

                        send(ANSI_PURPLE+"start account successful,you are in customer session now\nYOU can TYPE:\ndebit\ncredit\nbalance\nfinish"+ANSI_RESET);

                    }


                    break;

                case "finish":

                    map.put(accountinSession,new BankInfo(accountinSession,this.balance,false));
                    //
                    String BLC = Float.toString(this.balance);
                    for (int i = 0;i<4;i++){

                        if (portArr[i]==port){
                            // map.put(newAccountname,new BankInfo(newAccountname,0.0f,false));
                            continue;
                        }
                        InetAddress inetAddress2 = InetAddress.getByName("localhost");
                        SocketAddress address2 = new InetSocketAddress(inetAddress2,portArr[i]);
                        Socket other;
                        other = new Socket();
                        other.connect(address2);

                        DataOutputStream dos1 = new DataOutputStream(other.getOutputStream());
                        String sendmsg = "finish "+accountinSession+":"+Float.toString(this.balance);

                        dos1.writeUTF(sendmsg);
                        dos1.flush();

                    }

                    send(ANSI_RED + "You just Finished a Customer Session \ntype (open + accountname) or (start + accountname) or EXIT\n"+ANSI_RESET);




                    break;

                case "exit":
                    send("Goodbye My Friend!!!!");
                    isRunning = false;
                    break;

                default:
                    send("Invalid Input!!!!!!");
                    break;


            }

        }


        @Override
        public void run() {

            System.out.println("In the hread" + balance);
            receive();
            send(ANSI_RED + "welcome to Billionare's bank\n  let's be a billionare in one minute \n" + ANSI_BLUE+"type (open + accountname) or (start + accountname)\n"+ANSI_RESET);
            while (isRunning) {
                try {
                    Check_Send_recevie(receive());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

        }
    }


}
