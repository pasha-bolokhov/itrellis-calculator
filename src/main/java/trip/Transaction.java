package trip;

public class Transaction {
    private final String recipient;
    private final double amount;

    public Transaction(String recipient, double amount) {
        this.recipient = recipient;
        this.amount = amount;
    }

    public String getRecipient() {
        return this.recipient;
    }

    public double getAmount() {
        return this.amount;
    }
}
