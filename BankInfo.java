import java.io.Serializable;

/**
 * Created by sujiaxu on 16/8/14.
 */
public class BankInfo implements Serializable{
    String ClientName;
    Float balance;
    boolean insession;

    public BankInfo(String ClientName , Float balance ,boolean insession){
        this.ClientName = ClientName;
        this.balance = balance;
        this.insession = insession;
    }

}
