package trip;

import java.util.concurrent.atomic.AtomicLong;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
public class TripController {
    private static final Logger LOGGER = Logger.getLogger(TripController.class.getName()); // GGGG

    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/trip")
    public TripResponse trip(@RequestBody Person[] people) {
        // calculate total expenses
        double grandTotal = 0.0;
        for (Person p : people) {
            grandTotal += p.calcTotal();
        }

        // everyone's share
        double share = grandTotal / people.length;
        System.out.format("GGGG grandTotal = %g, share = %g, remnant = %d cents\n",
                            grandTotal, share, (Math.round(grandTotal * 100.0) % people.length));

        // calculate everyone's debt
        List<Person> debtors = new ArrayList<Person>();
        List<Person> recipients = new ArrayList<Person>();
        for (Person p : people) {
            p.calcDebt(share);
            // sort the person either into debtors or recipients
            if (p.getAmount() > 0) {
                debtors.add(p);
            } else {
                recipients.add(p);
                // for recipients, the amount is negative initially
                p.setAmount(-p.getAmount());
            }
        }

        // sort debtors in decreasing debt order
        debtors.sort((a, b) -> -Double.compare(a.getAmount(), b.getAmount()) );

        // sort recipients in increasing deficit order
        recipients.sort((a, b) -> Double.compare(a.getAmount(), b.getAmount()) );

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
        Reimbursement[] allReimbursements = reimbursementList.toArray(new Reimbursement[reimbursementList.size()]);

        // generate and send the response
        return new TripResponse(counter.incrementAndGet(), allReimbursements);
    }

    /**
     *
     */
    private double roundTransaction(double transaction) {
        return (Math.floor(transaction * 100)) / 100.0;
    }
}
