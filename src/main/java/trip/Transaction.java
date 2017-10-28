package trip;

/**
 * This is a DTO object representing a single transaction.
 * Only the <code>recipient</code> and <code>amount</code> are specified,
 * as the sender is common for entire <code>Reimbursement</code>.
 */
public class Transaction {
    private String recipient;
    private double amount;

    public Transaction(String recipient, double amount) {
        this.recipient = recipient;
        this.amount = amount;
    }

    public Transaction() {}

    public String getRecipient() {
        return this.recipient;
    }

    public void setRecipient(String recipient) { this.recipient = recipient; }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) { this.amount = amount; }
}
