package spn.netkat;

public class IfThen implements Policy {
    private Policy policy;

    public IfThen(Predicate cond, Policy thenBranch) {
        this.policy = new Union(
            new Sequence(new Filter(cond), thenBranch));
    }

    public String toString() {
        return this.policy.toString();
    }
}
