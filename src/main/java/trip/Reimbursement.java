package trip;

public class Reimbursement {
    private final String name;
    private final Transaction[] payments;

    public Reimbursement(String name, Transaction[] payments) {
        this.name = name;
        this.payments = payments;
    }

    public String getName() {
        return name;
    }

    public Transaction[] getPayments() {
        return payments;
    }
}
