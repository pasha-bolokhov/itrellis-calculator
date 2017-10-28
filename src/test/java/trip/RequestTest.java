package trip;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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

    /**
     *
     * @param fileName
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
     *
     * @param plan
     * @param people
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

                logger.info(String.format("GGGG %s[%.2f] \tpays\t %g \tto %s[%.2f] \t=>\t %s[%.2f] \t\t(%s[%g])",
                                        reimbursement.getName(), debt, transaction.getAmount(),
                                        transaction.getRecipient(), recipientsMap.get(transaction.getRecipient()).getAmount(),
                                        transaction.getRecipient(),
                                            recipientsMap.get(transaction.getRecipient()).getAmount() + transaction.getAmount(),
                                        reimbursement.getName(), debt - transaction.getAmount()));

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

        // check all debtors and recipients that the amounts have equalized
        double maxTotal = Stream.concat(allDebtors.stream(), allRecipients.stream())
                            .max( (a, b) -> Double.compare(a.getTotal(), b.getTotal()) ).get().getTotal();
        double minTotal = Stream.concat(allDebtors.stream(), allRecipients.stream())
                .min( (a, b) -> Double.compare(a.getTotal(), b.getTotal()) ).get().getTotal();

        logger.info("maximum discrepancy = {}", Math.abs(maxTotal - minTotal));

        // test maximum tolerance
        assertThat(Math.round(Math.abs(maxTotal - minTotal) * 100) / 100.0).isLessThanOrEqualTo(0.02);
    }
}
