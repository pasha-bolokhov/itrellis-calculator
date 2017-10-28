package trip;

import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The controller handling client requests
 */
@RestController
public class TripController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicLong counter = new AtomicLong();                    // used for marking messages with ID

    @RequestMapping("/trip")
    public TripResponse trip(@RequestBody Person[] people) {

        // Calculate debts and other analytics
        ExpenseAnalytics analytics = new ExpenseAnalytics(people);

        // Generate all payments
        Reimbursement[] allReimbursements = generatePayments(analytics);

        // Generate and send the response
        return new TripResponse(counter.incrementAndGet(), allReimbursements);
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
    private Reimbursement[] generatePayments(ExpenseAnalytics analytics) {
        List<Person> debtors = analytics.getDebtors();
        List<Person> recipients = analytics.getRecipients();

        // sort debtors in decreasing debt order
        debtors.sort( (a, b) -> -Double.compare(a.getAmount(), b.getAmount()) );

        // sort recipients in decreasing deficit order
        recipients.sort( (a, b) -> -Double.compare(a.getAmount(), b.getAmount()) );

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
     * Calculate and perform all equalizing payments.
     * <p>
     * This is an alternative core computational algorithm of the package.
     * <p>
     * This algorithm minimizes the amounts of each transaction by a given
     * debtor.
     * This is achieved by running a debtor across all recipients who
     * are sorted beforehand in the order of increasing credit.
     * Minimization is achieved by splitting the entire debtor's amount
     * equally among all recipients.
     * Such an "average" amount is in fact minimal, as going below it
     * at a given transaction will cause an increase in later transactions.
     *
     * Thus the recipients form an internal loop, while debtors an external one.
     *
     * @param analytics         initial state of debtors and recipients
     * @return                  an array of <code>Reimbursement</code> instances
     */
    private Reimbursement[] generateMinimalAmountPayments(ExpenseAnalytics analytics) {
        List<Person> debtors = analytics.getDebtors();
        List<Person> recipients = analytics.getRecipients();

        // sort debtors in decreasing debt order
        debtors.sort( (a, b) -> -Double.compare(a.getAmount(), b.getAmount()) );

        // sort recipients in increasing deficit order
        recipients.sort( (a, b) -> Double.compare(a.getAmount(), b.getAmount()) );

        // Fill in all payments
        // Since, when possible, payments are done in equal transactions to all unpaid people,
        // the array of recipients stays sorted in increasing deficit order
        // When a recipient is reimbursed in full, he/she is excluded from further consideration
        Person[] recipientArray = recipients.toArray(new Person[recipients.size()]);
        List<Reimbursement> reimbursementList = new ArrayList<Reimbursement>();             // prefer to keep it
        // as a list although
        // the size is known
        int firstUnpaid = 0;
        final int lastRecipient = recipientArray.length - 1;
        for (Person d : debtors) {
            List<Transaction> payments = new ArrayList<Transaction>();

            // run through all people who haven't been fully reimbursed yet
            for (int j = firstUnpaid; j <= lastRecipient; j++) {
                Person r = recipientArray[j];

                // attempt to split payments evenly
                // note that we have to re-adjust this amount after every transaction
                // because of rounding
                double equalPayments = d.getAmount() / (lastRecipient - j + 1);

                // Form a transaction from person "d" to person "r"
                double transaction = roundTransaction(equalPayments);

                if (r.getAmount() <= transaction) {             // person "r" about to be paid in full
                    // this is what they are getting paid
                    transaction = roundTransaction(r.getAmount());

                    // remove person "r" from further consideration
                    firstUnpaid++;
                }

                // perform the transaction
                d.pay(transaction);
                r.pay(transaction);
                payments.add(new Transaction(r.getName(), transaction));
            }

            // for a reimbursement from this debtor
            Reimbursement reimbursement = new Reimbursement(d.getName(),
                    payments.toArray(new Transaction[payments.size()]));
            reimbursementList.add(reimbursement);
        }

        // convert reimbursements into an array
        return reimbursementList.toArray(new Reimbursement[reimbursementList.size()]);
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
