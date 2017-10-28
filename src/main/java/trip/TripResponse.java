package trip;

/**
 * This is a DTO class to be send to the client.
 * This is the ultimate response data which contains
 * sequences of payments to be done
 */
public class TripResponse {

    private long id;
    private Reimbursement[] reimbursements;

    public TripResponse(long id, Reimbursement[] reimbursements) {
        this.id = id;
        this.reimbursements = reimbursements;
    }

    public TripResponse() {}

    public long getId() {
        return this.id;
    }

    public Reimbursement[] getReimbursements() {
        return this.reimbursements;
    }
}
