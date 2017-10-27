package trip;

import java.util.stream.DoubleStream;

public class Person {

    private String      name;
    private double[]    expenses;
    private double      total = 0.0;
    private double      amount;                // either owing or to be paid
    private boolean     paidInFull = false;

    public Person(String name, double[] expenses) {
        this.name = name;
        this.expenses = java.util.Arrays.copyOfRange(expenses, 0, expenses.length - 1);
    }

    public Person() {
        this.name = "";
        this.expenses = new double[] {0, 0};
    }

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

    public double calcTotal() {
        this.total = DoubleStream.of(this.expenses).sum();
        return this.total;
    }

    public double calcDebt(double equalShare) {
        this.amount = equalShare - this.total;
        return this.amount;
    }

    public boolean getPaidInFull() {
        return this.paidInFull;
    }

    public boolean pay(double payment) {
        this.amount -= payment;

        if (this.amount <= 0.0) {
            this.paidInFull = true;
        }

        // GGGG
        if (this.amount < 0) {
            System.out.format("person %s got paid %g but was owed only %g\n",
                                this.name, payment, this.amount + payment);
        }

        return this.paidInFull;
    }
}
