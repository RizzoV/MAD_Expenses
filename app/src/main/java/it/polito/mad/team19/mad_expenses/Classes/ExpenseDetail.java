package it.polito.mad.team19.mad_expenses.Classes;

/**
 * Created by Valentino on 29/04/2017.
 */

public class ExpenseDetail {

    private String creditorName;
    private String debtorName;
    private String creditorId;
    private String debtorId;
    private String amount;
    private String creditorImage;
    private String debtorImage;

    public ExpenseDetail(String creditor, String debtor, String creditorId, String debtorId, String amount, String creditorImage, String debtorImage) {
        this.creditorName = creditor;
        this.debtorName = debtor;
        this.amount = amount;
        this.creditorImage = creditorImage;
        this.debtorImage = debtorImage;
        this.debtorId = debtorId;
        this.creditorId = creditorId;
    }

    public String getCreditorId() {
        return creditorId;
    }

    public String getDebtorId() {
        return debtorId;
    }

    public String getCreditor() {
        return creditorName;
    }

    public void setCreditor(String creditor) {
        this.creditorName = creditor;
    }

    public String getDebtor() {
        return debtorName;
    }

    public void setDebtor(String debitor) {
        this.debtorName = debitor;
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
