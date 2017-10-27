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
        int n = people.length;
        Stream<Person> stream = Arrays.stream(people);

        // calculate total expenses
        // double grandTotal = Arrays.stream(people).mapToDouble(p -> p.calcTotal()).sum();
        double grandTotal = 0.0;
        for (Person p : people) {
            grandTotal += p.calcTotal();
        }

        // everyone's share
        double share = grandTotal / n;
        System.out.println("grand total = " + grandTotal);
        LOGGER.info("GGGG share = " + share);

        // calculate everyone's debt
        List<Person> debtors = new ArrayList<Person>();
        List<Person> recipients = new ArrayList<Person>();
        for (Person p : people) {
            p.calcDebt(share);
            if (p.getAmount() > 0) {
                debtors.add(p);
            } else {
                recipients.add(p);
            }
        }

        // sort debtors in decreasing debt
        debtors.sort((a,b) -> { return -Double.compare(a.getAmount(), b.getAmount()); });

        // GGGG stub
        return new Trip(counter.incrementAndGet(),
                        String.format(answerTemplate, people[0].getName(), 0.0));
    }
}
