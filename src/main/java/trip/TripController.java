package trip;

import java.util.concurrent.atomic.AtomicLong;
import java.util.Arrays;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
public class TripController {
    private static final Logger LOGGER = Logger.getLogger(TripController.class.getName());

    private static final String answerTemplate = "Payment for %s equals %g";
    private final AtomicLong counter = new AtomicLong();

    public Person[] people;

    @RequestMapping("/trip")
    public Trip trip(@RequestBody Person[] people) {
        LOGGER.info("Got " + people.length + " people");
        this.people = people;

        // calculate total expenses
        double grandTotal = Arrays.stream(this.people).mapToDouble(p -> p.calcTotal()).sum();

        LOGGER.info("Total expenses = " + grandTotal);
        return new Trip(counter.incrementAndGet(),
                        String.format(answerTemplate, people[0].getName(), 0.0));
    }
}
