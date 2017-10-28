package trip;

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
