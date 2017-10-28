package trip;

public class TripResponse {

    private final long id;
    private final Reimbursement[] reimbursements;

    public TripResponse(long id, Reimbursement[] reimbursements) {
        this.id = id;
        this.reimbursements = reimbursements;
    }

    public long getId() {
        return this.id;
    }

    public Reimbursement[] getReimbursements() {
        return this.reimbursements;
    }
}
