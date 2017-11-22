package trip;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
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

        // Initiate engine of calculations
        PaymentEngine engine = new PaymentEngine(people);

        // Generate all payments
        Reimbursement[] allReimbursements = engine.generatePayments();

        // Generate and send the response
        return new TripResponse(counter.incrementAndGet(), allReimbursements);
    }
}
