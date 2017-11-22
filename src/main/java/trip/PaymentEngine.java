package trip;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PaymentEngine {
    private Person[] people;
    private ExpenseAnalytics analytics;

    public PaymentEngine(Person[] people) {
        this.people = people;
        this.analytics = new ExpenseAnalytics(this.people);
    }

    /**
     * Calculate and perform all equalizing payments.
     * <p>
     * This is the core computational algorithm of the package.
     * <p>
     * This algorithm minimizes the number of transactions by paying
     * as much as possible in the beginning of the process.
     * This is achieved by sorting the debtors and recipients in the large-to-small
     * amount order and applying all debtors to a given recipient (if possible)
     * before switching to the net recipient.
     * Thus the recipients form an external loop, while debtors an internal one.
     *
     * @param analytics         initial state of debtors and recipients
     * @return                  an array of <code>Reimbursement</code> instances
     */
    private Reimbursement[] generatePayments() {
        List<Person> debtors = analytics.getDebtors();
        List<Person> recipients = analytics.getRecipients();

        // sort debtors in decreasing debt order
        debtors.sort(Comparator.comparingDouble(Person::getAmount).reversed());

        // sort recipients in decreasing deficit order
        recipients.sort(Comparator.comparingDouble(Person::getAmount).reversed());

        // fill in each recipient by all debtors at once
        Person[] debtorArray = debtors.toArray(new Person[debtors.size()]);
        Map<Person, List<Transaction>> reimbursementMap =
                debtors.stream().collect(Collectors.toMap(p -> p, p -> new ArrayList<Transaction>()));
        int firstUnfinished = 0;
        for (Person r : recipients) {
            for (int j = firstUnfinished; j < debtorArray.length; j++) {
                Person d = debtorArray[j];

                double transaction = roundTransaction(d.getAmount());

                if (transaction <= r.getAmount()) {               // person "d" about to complete paying
                    firstUnfinished++;
                } else {
                    transaction = roundTransaction(r.getAmount());
                }

                // perform the transaction
                d.pay(transaction);
                r.pay(transaction);
                reimbursementMap.get(d).add(new Transaction(r.getName(), transaction));
            }
        }

        // transform the map of reimbursements into an array of reimbursements
        return reimbursementMap.entrySet().stream()
                .map( entry -> new Reimbursement(entry.getKey().getName(),
                        entry.getValue().toArray(new Transaction[0])) )
                .toArray(Reimbursement[]::new);
    }


    /**
     * Rounds an amount to a physical value
     * <p>
     * <code>floor()</code> tends to give slightly better
     * results than <code>round()</code> or <code>ceil()</code>
     *
     * @param transactionAmount     amount to round
     * @return                      rounded sum
     */
    public static double roundTransaction(double transactionAmount) {
        return Math.floor(transactionAmount * 100) / 100.0;
    }
}
