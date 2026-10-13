/*EW37
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package system;

/**
 *
 * @author Ryan
 */
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;

public class CreditTermTrack {
    private Map<LocalDate, BigDecimal> accountReceivables = new HashMap<>();
    private Map<LocalDate, BigDecimal> accountPayables = new HashMap<>();
    
    public void addAccountReceivable(LocalDate date, BigDecimal amount){
        accountReceivables.put(date, amount);
    }
    public void addAccountPayable(LocalDate date, BigDecimal amount){
        accountPayables.put(date, amount);
    }
    
    public BigDecimal getAccountReceivableAmount(LocalDate dateToFind, LocalDate currentDate, BigDecimal paidAmount){
        boolean[] foundAndLessThan = {false, false};
        accountReceivables.forEach((dateListed, amount) -> {
                if(dateListed.equals(dateToFind)){
                    foundAndLessThan[0] = true;
                    if(amount.compareTo(paidAmount) >= 0){
                        foundAndLessThan[1] = true;
                    }
                }
                });
        
        if(!foundAndLessThan[0] || !foundAndLessThan[1]){ // check invalid conditions, might focus on later
            return null;
        }
        
        long daysAgo = ChronoUnit.DAYS.between(dateToFind, currentDate);
        
        BigDecimal discountedmount;
        
        if(daysAgo >= 0 && daysAgo <= 10){
            discountedmount = paidAmount.multiply(BigDecimal.valueOf(0.98));
        }else{
            discountedmount = paidAmount;
        }
        
        return discountedmount;
    }
    
    public BigDecimal getAccountPayableAmount(LocalDate dateToFind, LocalDate currentDate, BigDecimal paidAmount){
        boolean[] foundAndLessThan = {false, false};
        accountPayables.forEach((dateListed, amount) -> {
                if(dateListed.equals(dateToFind)){
                    foundAndLessThan[0] = true;
                    if(amount.compareTo(paidAmount) >= 0){
                        foundAndLessThan[1] = true;
                    }
                }
                });
        
        if(!foundAndLessThan[0] || !foundAndLessThan[1]){ // check invalid conditions, might focus on later
            return null;
        }
        
        long daysAgo = ChronoUnit.DAYS.between(dateToFind, currentDate);
        
        BigDecimal discountedmount;
        
        if(daysAgo >= 0 && daysAgo <= 10){
            discountedmount = paidAmount.multiply(BigDecimal.valueOf(0.98));
        }else{
            discountedmount = paidAmount;
        }
        
        return discountedmount;
    }
}
