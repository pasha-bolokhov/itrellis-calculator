package trip;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class RequestTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());     // GGGG


    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void reimbursementTest() throws Exception, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Person[] people = objectMapper.readValue(new File("src/test/json/sample-0.json"), Person[].class);
        logger.info("GGGG first name is = " + people[0].getName());

        // fetch the payment plan
        TripResponse plan =
                restTemplate.postForObject("http://localhost:8080/trip", people, TripResponse.class);

        logger.info("GGGG got response plan with {} reimbursements", plan.getReimbursements().length);

        // check the payment plan
        checkPlan(plan, people);
    }

    private void checkPlan(TripResponse plan, Person[] people) {
        // turn array into map
//        Map<String, double> peopleMap =
//                Stream.of(people).collect(toMap(Person::getName, p -> p.getAmount()));
//        for (Reimbursement reimbursement : plan.getReimbursements()) {
//
//        }
    }
}
