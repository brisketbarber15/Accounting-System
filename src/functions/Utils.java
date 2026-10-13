/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package functions;

/**
 *
 * @author Ryan
 */
import system.*;
import statics.*;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Utils {
    private static CreditTermTrack credits = new CreditTermTrack();
    
    public static Transaction initializeOpeningTrialBalance(LocalDate date){
        return new OpeningTrialBalance().createOpeningBalanceTransaction(date);
    }
    
    public static Account findAccount(String accountName){
        ChartofAccounts coa = new ChartofAccounts();
        
        Account account = coa.getAccounts()
                .stream()
                .filter(acc -> acc.name().equalsIgnoreCase(accountName))
                .findFirst()
                .orElse(null);
        
        if(account == null){
            JOptionPane.showMessageDialog(null, "Invalid account: " + accountName);
        }
        
        return account;
    }
    
    public static JournalEntryItem createEntry(Account acc, int amount, EntryType entry){
        return new JournalEntryItem(acc, BigDecimal.valueOf(amount), entry);
    }
    
    public static Transaction createTransaction(LocalDate date, String description, JournalEntryItem[] entries){
        List<JournalEntryItem> entry = new ArrayList();
        
        for(int i=0; i<entries.length; i++){
            entry.add(entries[i]);
        }
        
//        for(int i=0; i<entry.size(); i++){
//            System.out.println(entry.get(i).account().name() + ": " + entry.get(i).amount());
//        }
        
        if(!doubleEntryIsValid(entry)){ // check if double entry is valid
            return null;
        }
        
        List<JournalEntryItem> updatedEntries = checkForCreditsAccount(entry, date); // to track credit terms
        
        if(updatedEntries == null){
            return null;
        }
        
        if(!doubleEntryIsValid(updatedEntries)){   // safety net
            return null;
        }
//        boolean same = true;
//        for(int i =0; i<entry.size(); i++){
//            if(!entry.get(i).amount().equals(updatedEntries.get(i).amount())){
//                same = false;
//            }
//        }
//        
//        if(!same){
//            updatedEntries.add(new JournalEntryItem());
//        }
        
//        for(int j=0; j<updatedEntries.size(); j++){
//            System.out.println(updatedEntries.get(j).account().name() + ": " + updatedEntries.get(j).amount());
//        }
        
        return new Transaction(UUID.randomUUID(), date, description, updatedEntries);
    }
    
private static List<JournalEntryItem> checkForCreditsAccount(List<JournalEntryItem> entry, LocalDate date){
    List<JournalEntryItem> updatedEntries = new ArrayList<>();
    BigDecimal totalDiscount = BigDecimal.ZERO;

    for (JournalEntryItem acc : entry) {
        if(!acc.account().name().equalsIgnoreCase("accounts receivable")
                && !acc.account().name().equalsIgnoreCase("accounts payable")){
            updatedEntries.add(acc);
            continue;
        }

        // ================= ACCOUNTS RECEIVABLE =================
        if(acc.account().name().equalsIgnoreCase("accounts receivable")){

            if(acc.entryType() == EntryType.DEBIT){
                credits.addAccountReceivable(date, acc.amount());
                updatedEntries.add(acc);
                continue;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate dateToFind = null;
            boolean isValid = false;

            while(!isValid){
                String inputDate = JOptionPane.showInputDialog(
                    null,
                    "Enter the date the accounts receivable was originally recorded (YYYY-MM-DD):",
                    "Date Input",
                    JOptionPane.QUESTION_MESSAGE
                );

                if(inputDate == null){
                    JOptionPane.showMessageDialog(null, "Operation cancelled.", "Exit", JOptionPane.WARNING_MESSAGE);
                    return null;
                }

                try{
                    dateToFind = LocalDate.parse(inputDate, formatter);
                    isValid = true;
                }catch(DateTimeParseException e){
                    JOptionPane.showMessageDialog(
                            null,
                            "Invalid date or format! Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

            BigDecimal discountedAmount = credits.getAccountReceivableAmount(dateToFind, date, acc.amount());
            if(discountedAmount == null){
                JOptionPane.showMessageDialog(null, "Either accounts receivable at " + dateToFind + " is not found, or the amount paid is greater than the balance.");
                return null;
            }

            BigDecimal discountAmount = acc.amount().subtract(discountedAmount);

            JournalEntryItem updatedEntry = new JournalEntryItem(
                    acc.account(),
                    acc.amount(),
                    acc.entryType()
            );
            updatedEntries.add(updatedEntry);

            Account salesDiscountAcc = findAccount("sales discounts");
            if(salesDiscountAcc == null){
                return null;
            }
            JournalEntryItem addedEntry = new JournalEntryItem(
                    salesDiscountAcc,
                    discountAmount,
                    EntryType.DEBIT
            );
            updatedEntries.add(addedEntry);

            totalDiscount = totalDiscount.add(discountAmount);

        // ================= ACCOUNTS PAYABLE =================
        } else {

            if(acc.entryType() == EntryType.CREDIT){
                credits.addAccountPayable(date, acc.amount());
                updatedEntries.add(acc);
                continue;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate dateToFind = null;
            boolean isValid = false;

            while(!isValid){
                String inputDate = JOptionPane.showInputDialog(
                    null,
                    "Enter the date the accounts payable was originally recorded (YYYY-MM-DD):",
                    "Date Input",
                    JOptionPane.QUESTION_MESSAGE
                );

                if(inputDate == null){
                    JOptionPane.showMessageDialog(null, "Operation cancelled.", "Exit", JOptionPane.WARNING_MESSAGE);
                    return null;
                }

                try{
                    dateToFind = LocalDate.parse(inputDate, formatter);
                    isValid = true;
                }catch(DateTimeParseException e){
                    JOptionPane.showMessageDialog(
                            null,
                            "Invalid date or format! Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

            BigDecimal discountedAmount = credits.getAccountPayableAmount(dateToFind, date, acc.amount());
            if(discountedAmount == null){
                JOptionPane.showMessageDialog(null, "Either accounts payable at " + dateToFind + " is not found, or the amount paid is greater than the balance.");
                return null;
            }

            BigDecimal discountAmount = acc.amount().subtract(discountedAmount);

            JournalEntryItem updatedEntry = new JournalEntryItem(
                    acc.account(),
                    acc.amount(),
                    acc.entryType()
            );
            updatedEntries.add(updatedEntry);

            Account purchasesDiscountAcc = findAccount("purchases discounts");
            if(purchasesDiscountAcc == null){
                return null;
            }
            JournalEntryItem addedEntry = new JournalEntryItem(
                    purchasesDiscountAcc,
                    discountAmount,
                    EntryType.CREDIT
            );
            updatedEntries.add(addedEntry);

            totalDiscount = totalDiscount.add(discountAmount);
            }
        }
    
        if(totalDiscount.compareTo(BigDecimal.ZERO) > 0){
            adjustCashEntry(updatedEntries, totalDiscount);
        }

    return updatedEntries;
}

private static void adjustCashEntry(List<JournalEntryItem> entries, BigDecimal discountAmount){
    for(int i = 0; i < entries.size(); i++){
        JournalEntryItem item = entries.get(i);
        if(item.account().name().equalsIgnoreCase("cash")){
            entries.set(i, new JournalEntryItem(
                    item.account(),
                    item.amount().subtract(discountAmount),
                    item.entryType()
            ));
            return;
        }
    }
}
    
    private static boolean doubleEntryIsValid(List<JournalEntryItem> entry){
        BigDecimal totalDebits = entry.stream() 
            .filter(item -> item.entryType() == EntryType.DEBIT)
            .map(JournalEntryItem::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = entry.stream()
            .filter(item -> item.entryType() == EntryType.CREDIT)
            .map(JournalEntryItem::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if(totalDebits.compareTo(totalCredits) != 0){
            JOptionPane.showMessageDialog(null, "Transaction unbalanced: Debits: " + totalDebits + ", Credits: " + totalCredits);
            return false;
        }
        
        return true;
    }
    
    public static TAccount createTAccountAndInitializeBeginingBalance(String accountName, Transaction t0){
        Account account = findAccount(accountName);
        TAccount taccount = new TAccount(account);
        
        JournalEntryItem beginingBalance = t0
                .getItems()
                .stream()
                .filter(acc -> acc.account().name().equalsIgnoreCase(accountName))
                .findFirst()
                .orElse(null)
        ;
        
        if(beginingBalance != null){
            taccount.addEntry(beginingBalance);        
        }
        
        return taccount;
    }
    
    public static void addEntries(TAccount taccount, List<Transaction> t){ 
        List<JournalEntryItem> entries = new ArrayList<>();
        
        for(int j=0; j<t.size(); j++){
            t
            .get(j)
            .getItems()
            .stream()
            .forEach(acc -> {
                if(acc.account().name().equalsIgnoreCase(taccount.getAccount().name())){
                    entries.add(acc);
                }
            });
        }
        
        for(int i=0; i<entries.size(); i++){
            taccount.addEntry(entries.get(i));
        }
    }
    
    public static void displayTransaction(Transaction t){
        if(t == null){
            System.out.println("Transaction was not created(null).");
            return;
        }
        for(int i=0; i<t.getItems().size(); i++){
            System.out.println(t.getDate() + " | " + t.getItems().get(i).account().name() + " | " + t.getItems().get(i).entryType() + ": " + t.getItems().get(i).amount());
        }
        
        System.out.println(t.getDescription());
        System.out.println("-------------------------------------"); 
    }
    
    public static void displayTAcount(TAccount tacount){
        System.out.println("\n" + tacount.getAccount().name());
        System.out.println("-----------------------");
        System.out.println("Debits \n----------");
        tacount.getDebits().stream().forEach(deb -> System.out.println(deb.amount()));
        System.out.println("Credits \n----------");
        tacount.getCredits().stream().forEach(cred -> System.out.println(cred.amount()));
        System.out.println("\n" + tacount.getAccount().name() + " balance: " + tacount.getNetBalance());
    }
    
    public static void displayChartOfAccount(){
        ChartofAccounts coa = new ChartofAccounts();
        coa.display();
    }
}
