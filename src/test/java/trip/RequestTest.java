package trip;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs a number of tests
 * <p>
 * Test data is taken from Json files.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class RequestTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

    @Test
    public void alternativeTestOnResponse() throws Exception, IOException {
        runTestDataFile("src/test/json/sample-3.json");
    }

    @Test
    public void nonTrivialTestOnResponse() throws Exception, IOException {
        runTestDataFile("src/test/json/freshman-0.json");
    }

    /**
     * Reads Json data from <code>fileName</code>, sends it over to the server
     * and checks the response by running all transactions on the initial
     * state of people as obtained from <code>fileName</code>
     *
     * @param fileName          file path from which to load trip expenses
     * @throws Exception
     * @throws IOException
     */
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
     * Runs the transactions from the server response on the list of people
     * and checks whether the net payments have equalized
     * <p>
     * This function is run when after sending a test request to the server
     *
     * @param plan          A response from server containing payments to do
     * @param people        Array of payments as obtained from test data
     */
    private void checkPlan(TripResponse plan, Person[] people) {

        // count the debts and amounts
        ExpenseAnalytics analytics = new ExpenseAnalytics(people);
        List<Person> allDebtors = analytics.getDebtors();
        List<Person> allRecipients = analytics.getRecipients();

        // turn arrays into maps
        Map<String, Person> debtorsMap = allDebtors.stream()
                        .collect(Collectors.toMap(Person::getName, p -> p));
        Map<String, Person> recipientsMap = allRecipients.stream()
                        .collect(Collectors.toMap(Person::getName, p -> p));

        // run through all reimbursements
        for (Reimbursement reimbursement : plan.getReimbursements()) {
            assertThat(debtorsMap).containsKey(reimbursement.getName());

            Person debtor = debtorsMap.get(reimbursement.getName());
            double debt = debtor.getAmount();

            // perform all transactions
            for (Transaction transaction : reimbursement.getPayments()) {
                String recipientName = transaction.getRecipient();
                assertThat(recipientsMap).containsKey(recipientName);
                Person recipient = recipientsMap.get(recipientName);
                assertThat(debt).isGreaterThanOrEqualTo(transaction.getAmount());
                debt -= transaction.getAmount();
                recipient.setAmount(recipient.getAmount() - transaction.getAmount());

                // separately track how much they actually paid
                debtor.setTotal(debtor.getTotal() + transaction.getAmount());
                recipient.setTotal(recipient.getTotal() - transaction.getAmount());
            }

            debtor.setAmount(debt);
        }

        // check all FRESHMAN debtors and recipients that the amounts have equalized
        double maxFreshmanTotal = 0.0;
        double minFreshmanTotal = 0.0;
        maxFreshmanTotal = Stream.concat(allDebtors.stream(), allRecipients.stream()).filter(p -> p.isFreshman())
                .max( (a, b) -> Double.compare(a.getTotal(), b.getTotal()) ).get().getTotal();
        minFreshmanTotal = Stream.concat(allDebtors.stream(), allRecipients.stream()).filter(p -> p.isFreshman())
                            .min( (a, b) -> Double.compare(a.getTotal(), b.getTotal()) ).get().getTotal();

        logger.info("maximum FRESHMAN discrepancy = {}", Math.abs(maxFreshmanTotal - minFreshmanTotal));

        // test maximum tolerance
        assertThat(Math.round(Math.abs(maxFreshmanTotal - minFreshmanTotal) * 100) / 100.0).isLessThanOrEqualTo(0.05);

        // check all NON-freshman debtors and recipients that the amounts have equalized
        double maxNonFreshmanTotal = 0.0;
        double minNonFreshmanTotal = 0.0;
        maxNonFreshmanTotal = Stream.concat(allDebtors.stream(), allRecipients.stream()).filter(p -> !p.isFreshman())
                .max( (a, b) -> Double.compare(a.getTotal(), b.getTotal()) ).get().getTotal();
        minNonFreshmanTotal = Stream.concat(allDebtors.stream(), allRecipients.stream()).filter(p -> !p.isFreshman())
                .min( (a, b) -> Double.compare(a.getTotal(), b.getTotal()) ).get().getTotal();

        logger.info("maximum discrepancy = {}", Math.abs(maxNonFreshmanTotal - minNonFreshmanTotal));

        // test maximum tolerance
        assertThat(Math.round(Math.abs(maxNonFreshmanTotal - minNonFreshmanTotal) * 100) / 100.0).isLessThanOrEqualTo(0.05);
    }
}
