package trip;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs analysis of all payments and splits
 * the array of people into "debtors" and "recipients"
 * <p>
 * This class is useful both for the calculator
 * and for testing purposes
 */
public class ExpenseAnalytics {
    private List<Person> debtors;
    private List<Person> recipients;

    private double total;              // total amount that was paid by everybody
    private double share;              // total (unrounded) amount divided by number of all people
    private int numFreshmen;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ExpenseAnalytics(Person[] people) {
        // calculate total expenses
        double grandTotal = 0.0;
        numFreshmen = 0;
        for (Person p : people) {
            grandTotal += p.getTotal();
            if (p.isFreshman()) {
                numFreshmen++;
            }
        }
        this.setTotal(grandTotal);

        logger.info("got number of freshmen {}", numFreshmen);

        // everyone's share
        double share = this.getTotal() / people.length;
        if (numFreshmen > 0) {
            share *= 0.9;
        }

        this.setShare(share);

        // calculate everyone's debt and split the team into debtors and recipients
        List<Person> debtors = new ArrayList<>();
        List<Person> recipients = new ArrayList<>();
        for (Person p : people) {
            // sort the person either into debtors or recipients
            if (p.calcDebt(share, this.numFreshmen, this.getTotal()) > 0) {
                debtors.add(p);
            } else {
                recipients.add(p);
            }
        }
        this.setDebtors(debtors);
        this.setRecipients(recipients);
    }

    public List<Person> getDebtors() {
        return debtors;
    }

    public void setDebtors(List<Person> debtors) {
        this.debtors = debtors;
    }

    public List<Person> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Person> recipients) {
        this.recipients = recipients;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getShare() {
        return share;
    }

    public void setShare(double share) {
        this.share = share;
    }

}
