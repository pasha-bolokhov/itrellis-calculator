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

@RestController
public class TripController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());     // GGGG

    private final AtomicLong counter = new AtomicLong();

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
     *
     * @param analytics
     * @return
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

                System.out.format("GGGG RRRRRRRRR %s[%.2f] pays \t%g to \t%s[%.2f]\t ==> \t %s[%.2f]" +
                                " \t(%s[%.2f])\n",
                        d.getName(), d.getAmount(), transaction, r.getName(), r.getAmount(),
                        r.getName(), r.getAmount() - transaction,
                        d.getName(), d.getAmount() - transaction);

                // do the transaction
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

//        Stream.of(reimbursementMap.entrySet().toArray()).forEach(e -> System.out.format("%s\n", e.getKey().getName()));
    }

    /**
     *
     * @param analytics
     * @return
     */
    private Reimbursement[] generateMinimalAmountPayments(ExpenseAnalytics analytics) {
        List<Person> debtors = analytics.getDebtors();
        List<Person> recipients = analytics.getRecipients();

        // sort debtors in decreasing debt order
        debtors.sort( (a, b) -> -Double.compare(a.getAmount(), b.getAmount()) );

        // sort recipients in increasing deficit order
        recipients.sort( (a, b) -> Double.compare(a.getAmount(), b.getAmount()) );

        // GGGG
        debtors.stream().forEach(p -> System.out.format("GGGG debtor %s[paid %g] owes %g\n",
                p.getName(), p.getTotal(), p.getAmount()));
        recipients.stream().forEach(p -> System.out.format("GGGG recpt %s[paid %g] misses %g\n",
                p.getName(), p.getTotal(), p.getAmount()));

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

                System.out.format("GGGG TTTTTTTT %s[%.2f] pays \t%g to \t%s[%.2f]\t ==> \t %s[%.2f]" +
                                " \t(%s[%.2f])\n",
                        d.getName(), d.getAmount(), transaction, r.getName(), r.getAmount(),
                        r.getName(), r.getAmount() - transaction,
                        d.getName(), d.getAmount() - transaction);

                // do the transaction
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
     *
     * @param transactionAmount
     * @return
     */
    public static double roundTransaction(double transactionAmount) {
        return Math.floor(transactionAmount * 100) / 100.0;
    }

}
