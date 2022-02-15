package all;

import com.dicontainer.annotations.ToInject;

public class Account {
    private double total;
    private Tax tax;

    @ToInject
    public Account(Tax tax) {
        this.tax = tax;
    }

    public double getAnnualTax() {
        return tax.addTax(total);
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
