package trip;

public class Reimbursement {
    private String name;
    private Transaction[] payments;

    public Reimbursement(String name, Transaction[] payments) {
        this.name = name;
        this.payments = payments;
    }

    public Reimbursement() {}

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public Transaction[] getPayments() {
        return payments;
    }

    public void setPayments(Transaction[] payments) {
        this.payments = payments;
    }
}
