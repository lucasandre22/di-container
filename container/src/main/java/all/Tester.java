package all;

import com.dicontainer.Container;

public class Tester {

    public static void main(String[] args) throws Exception {
        Account myAccount = Container.getNewInstanceFor(Account.class);
        myAccount.setTotal(100.0d);
        System.out.println(myAccount.getAnnualTax());
    }
}
