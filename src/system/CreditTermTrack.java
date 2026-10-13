/*
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
        LocalDate matchedDate = null;
        BigDecimal matchedAmount = null;

        for(Map.Entry<LocalDate, BigDecimal> entry : accountReceivables.entrySet()){
            LocalDate listedDate = entry.getKey();
            BigDecimal listedAmount = entry.getValue();

            if(!listedDate.isAfter(dateToFind)){
                // keep the most recent one that is <= dateToFind
                if(matchedDate == null || listedDate.isAfter(matchedDate)){
                    matchedDate = listedDate;
                    matchedAmount = listedAmount;
                }
            }
        }

        if(matchedDate == null){
            return null;   // no AR exists on or before the given date
        }

        if(matchedAmount.compareTo(paidAmount) < 0){
            return null;   // paying more than what is owed
        }

        long daysAgo = ChronoUnit.DAYS.between(matchedDate, currentDate);

        BigDecimal discountedAmount;
        if(daysAgo >= 0 && daysAgo <= 10){
            discountedAmount = paidAmount.multiply(BigDecimal.valueOf(0.98));
        } else {
            discountedAmount = paidAmount;
        }

        return discountedAmount;
    }

    public BigDecimal getAccountPayableAmount(LocalDate dateToFind, LocalDate currentDate, BigDecimal paidAmount){
        // Find the AP recorded on or before dateToFind (the "as of" date)
        LocalDate matchedDate = null;
        BigDecimal matchedAmount = null;

        for(Map.Entry<LocalDate, BigDecimal> entry : accountPayables.entrySet()){
            LocalDate listedDate = entry.getKey();
            BigDecimal listedAmount = entry.getValue();

            if(!listedDate.isAfter(dateToFind)){
                // keep the most recent one that is <= dateToFind
                if(matchedDate == null || listedDate.isAfter(matchedDate)){
                    matchedDate = listedDate;
                    matchedAmount = listedAmount;
                }
            }
        }

        if(matchedDate == null){
            return null;   // no AP exists on or before the given date
        }

        if(matchedAmount.compareTo(paidAmount) < 0){
            return null;   // paying more than what is owed
        }

        long daysAgo = ChronoUnit.DAYS.between(matchedDate, currentDate);

        BigDecimal discountedAmount;
        if(daysAgo >= 0 && daysAgo <= 10){
            discountedAmount = paidAmount.multiply(BigDecimal.valueOf(0.98));
        } else {
            discountedAmount = paidAmount;
        }

        return discountedAmount;
    }
}