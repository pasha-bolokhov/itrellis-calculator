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
import org.springframework.http.ResponseEntity;


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
//        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/trip", String.class).contains("GGGG"));
//        System.out.println("******************* GGGG response is: " +
//                            this.restTemplate.getForObject("http://localhost:" + port + "/trip", String.class));
        ObjectMapper objectMapper = new ObjectMapper();
        Person[] people = objectMapper.readValue(new File("src/test/json/sample-0.json"), Person[].class);
        logger.info("GGGG first name is = " + people[0].getName());

        // fetch the payment plan
        TripResponse plan =
                restTemplate.postForObject("http://localhost:8080/trip", people, TripResponse.class);

        logger.info("GGGG got response plan with {} reimbursements", plan.getReimbursements().length);

        
    }
}
