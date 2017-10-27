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
    private static final Logger LOGGER = Logger.getLogger(TripController.class.getName());

    private static final String answerTemplate = "Payment for %s equals %g";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/trip")
    public Trip trip(@RequestBody Person[] people) {
        LOGGER.info("GGGG Got " + people.length + " people");

        // calculate total expenses
        double grandTotal = 0.0;
        for (Person p : people) {
            grandTotal += p.calcTotal();
        }

        // everyone's share
        int n = people.length;
        double share = grandTotal / n;
        System.out.format("GGGG grandTotal = %g, share = %g\n", grandTotal, share);

        // calculate everyone's debt
        List<Person> debtors = new ArrayList<Person>();
        List<Person> recipients = new ArrayList<Person>();
        for (Person p : people) {
            p.calcDebt(share);
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

        debtors.stream().forEach(p -> System.out.format("GGGG debtor %s owes %g\n", p.getName(), p.getAmount()));
        recipients.stream().forEach(p -> System.out.format("GGGG recpt %s misses %g\n", p.getName(), p.getAmount()));

        // Fill in all payments
        Person[] recipientArray = recipients.toArray(new Person[recipients.size()]);
        int firstUnpaid = 0;
        int lastRecipient = recipientArray.length - 1;
        for (Person p : debtors) {
            int numUnpaid = lastRecipient - firstUnpaid + 1;    // this allows to split the payment evenly
                                                                // across all unpaid people

            double equalPayments = p.getAmount() / numUnpaid;         // attempt to split payments evenly

            for (int j = firstUnpaid; j <= lastRecipient; j++) {
                Person r = recipientArray[j];
                double transaction = equalPayments;

                if (r.getAmount() <= transaction) {                 // person "r" about to be paid in full
                    transaction = r.getAmount();
                    firstUnpaid++;
                    numUnpaid--;                                // numUnpaid decreases at most once per loop
                }

                System.out.format("GGGG TTTTTTTT %s[%g] pays %g to %s[%g]  ==>  %s[%g]\n",
                        p.getName(), p.getAmount(), transaction, r.getName(), r.getAmount(),
                        r.getName(), r.getAmount() - transaction);

                p.pay(transaction);
                r.pay(transaction);
            }
        }

        // GGGG stub
        return new Trip(counter.incrementAndGet(),
                        String.format(answerTemplate, people[0].getName(), 0.0));
    }
}
