package it.polito.mad.team19.mad_expenses.Classes;

/**
 * Created by Valentino on 29/04/2017.
 */

public class ExpenseDetail {

    private String creditor;
    private String debtor;
    private String amount;
    private String creditorImage;
    private String debtorImage;

    public ExpenseDetail(String creditor, String debtor, String amount, String creditorImage, String debtorImage) {
        this.creditor = creditor;
        this.debtor = debtor;
        this.amount = amount;
        this.creditorImage = creditorImage;
        this.debtorImage = debtorImage;
    }
    public String getCreditor() {
        return creditor;
    }

    public void setCreditor(String creditor) {
        this.creditor = creditor;
    }

    public String getDebtor() {
        return debtor;
    }

    public void setDebtor(String debitor) {
        this.debtor = debitor;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCreditorImage() {
        return creditorImage;
    }

    public void setCreditorImage(String creditorImage) {
        this.creditorImage = creditorImage;
    }

    public String getDebtorImage() {
        return debtorImage;
    }

    public void setDebtorImage(String debitorImage) {
        this.debtorImage = debitorImage;
    }


}
