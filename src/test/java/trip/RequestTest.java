package trip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;
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
    public void simpleTestOnResponse() throws Exception, IOException {
        runTestDataFile("src/test/json/sample-0.json");
    }

    @Test
    public void longerTestOnResponse() throws Exception, IOException {
        runTestDataFile("src/test/json/sample-1.json");
    }

    @Test
    public void evenLongerTestOnResponse() throws Exception, IOException {
        runTestDataFile("src/test/json/sample-2.json");
    }

    private void runTestDataFile(String fileName) throws Exception, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Person[] people = objectMapper.readValue(new File(fileName), Person[].class);

        // fetch the payment plan
        TripResponse plan =
                restTemplate.postForObject("http://localhost:8080/trip", people, TripResponse.class);

        logger.info("response plan with {} reimbursements", plan.getReimbursements().length);

        // check the payment plan
        checkPlan(plan, people);
    }

    /**
     *
     * @param plan
     * @param people
     */
    private void checkPlan(TripResponse plan, Person[] people) {

        // count the debts and amounts
        ExpenseAnalytics analytics = new ExpenseAnalytics(people);

        // turn arrays into maps
        Map<String, Double> debtorsMap =
                analytics.getDebtors().stream().collect(Collectors.toMap(Person::getName, p -> p.getAmount()));
        Map<String, Double> recipientsMap =
                analytics.getRecipients().stream().collect(Collectors.toMap(Person::getName, p -> p.getAmount()));

        // run through all reimbursements
        for (Reimbursement reimbursement : plan.getReimbursements()) {
            assertThat(debtorsMap).containsKey(reimbursement.getName());

            double debt = debtorsMap.get(reimbursement.getName());

            // perform all transactions
            for (Transaction transaction : reimbursement.getPayments()) {

                logger.info(String.format("GGGG %s[%.2f] \tpays\t %.2f \tto %s[%.2f] \t=>\t %s[%.2f] \t\t(%s[%.2f])",
                                        reimbursement.getName(), debt, transaction.getAmount(),
                                        transaction.getRecipient(), recipientsMap.get(transaction.getRecipient()),
                                        transaction.getRecipient(),
                                            recipientsMap.get(transaction.getRecipient()) + transaction.getAmount(),
                                        reimbursement.getName(), debt - transaction.getAmount()));

                String recipientName = transaction.getRecipient();
                assertThat(recipientsMap).containsKey(recipientName);
                assertThat(debt).isGreaterThanOrEqualTo(transaction.getAmount());
                debt -= transaction.getAmount();
                recipientsMap.put(recipientName,
                                    recipientsMap.get(recipientName) - transaction.getAmount());
            }

            debtorsMap.put(reimbursement.getName(), debt);
        }

        // check all debtors and recipients that the amounts have equalized
        debtorsMap.forEach( (name, value) -> { assertThat(Math.abs(value)).isLessThanOrEqualTo(0.01); } );
        recipientsMap.forEach( (name,value) -> { assertThat(Math.abs(value)).isLessThanOrEqualTo(0.01); } );
    }
}
