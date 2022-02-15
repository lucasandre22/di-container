package all;

import com.dicontainer.annotations.Dependency;

@Dependency(to = "all.Tax")
public class BrazilianTax implements Tax {
    @Override
    public double addTax(double amount) {
        return amount * 0.55d;
    }
}
