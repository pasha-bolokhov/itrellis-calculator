package trip;

import java.util.stream.DoubleStream;

/**
 * This class represent a state of a person at a given moment.
 * The state changes upon doing a <code>Transaction</code>.
 * <p>
 * Initially the state is populated from the request from client.
 * <p>
 * For "recipients" (over-payers), <code>amount</code> gives
 * the sum this person has overpaid.
 * For "debtors" <code>amount</code> shows the amount they owe
 * <p>
 * For simplicity, debtors and recipients are not designated
 * separated classes. A <code>Person</code> initially does not know
 * whether he/she is a debtor or recipient. Use function <code>calcDebt()</code>
 * to give the user the information whether he/she is a debtor
 * or a recipient
 */
public class Person {

    private String      name;                   // person's name
    private double[]    expenses;               //
    private double      total = 0.0;            // total cost paid by this person
    private double      amount;                 // either amount owing or amount owed
    private boolean     freshman;
    private boolean     paidInFull = false;     // a flag showing whether this user has completed
                                                // all transactions

    public Person(String name, double[] expenses) {
        this.name = name;
        this.setExpenses(expenses);
    }

    public Person() {}

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double[] getExpenses() {
        return this.expenses;
    }

    public void setExpenses(double[] expenses) {
        this.expenses = expenses;
        this.calcTotal();
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getTotal() {
        return this.total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public boolean isFreshman() { return this.freshman; }

    /**
     * Calculate total cost initially accrued by the person
     *
     * @return          Total initial costs
     */
    public double calcTotal() {
        this.total = DoubleStream.of(this.expenses).sum();
        return this.total;
    }

    /**
     * Calculates how much this person owes to the rest of the team.
     * A negative amount means this person is a recipient
     *
     * @param equalShare        averaged cost of the entire trip
     * @return                  signed debt (negative debt meaning credit)
     */
    public double calcDebt(double equalShare, int numFreshmen, double totalExpense) {
        double debt = equalShare - this.total;

        if (this.isFreshman()) {
            debt += totalExpense * 0.1 / numFreshmen;
        }

        this.amount = debt;

        // negative debt means the person is a recipient
        if (debt < 0) {
            this.amount = -this.amount;
        }

     // return positive or negative debt amount
        return debt;
    }

    public boolean getPaidInFull() {
        return this.paidInFull;
    }

    /**
     * Applies a transaction amount to this person.
     * This function works both for debtors and recipients
     *
     * @param payment       the transaction amount
     *
     * @return              true/false depending on whether this
     *                      person has completed their payments
     */
    public boolean pay(double payment) {
        this.amount -= payment;

        if (this.amount <= 0.0) {
            this.paidInFull = true;
        }

        return this.paidInFull;
    }
}
