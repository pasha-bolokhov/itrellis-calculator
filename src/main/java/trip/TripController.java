package trip;

import java.util.concurrent.atomic.AtomicLong;
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
    public Trip trip(@RequestBody Person person) {
        System.out.println("name = " + person.getName());
        person.calcDebt(0.0);
        LOGGER.info("Got amount = `" + person.getAmount() + "'");
        return new Trip(counter.incrementAndGet(),
                        String.format(answerTemplate, person.getName(), person.getAmount()));
    }
}
