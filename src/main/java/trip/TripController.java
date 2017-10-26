package trip;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
public class TripController {
    private static final Logger LOGGER = Logger.getLogger(TripController.class.getName());

    private static final String answerTemplate = "Payment is for %s";
    private final AtomicLong counter = new AtomicLong();


    @RequestMapping("/trip")
    public Trip trip(@RequestBody Person person) {
        System.out.println("name = " + person.getName());
        LOGGER.info("Got person = `" + person.toString() + "'");
        return new Trip(counter.incrementAndGet(), person.getName());
    }


//    @RequestMapping("/trip")
//    public Trip trip(@RequestParam(value = "player", defaultValue = "Meagan") String player) {
//        return new Trip(counter.incrementAndGet(), String.format(answerTemplate, player));
//    }
}
