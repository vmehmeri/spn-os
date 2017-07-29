package spn.netkat;

public class IfThenElse implements Policy {
    private Policy policy;

    public IfThenElse(Predicate cond, Policy thenBranch, Policy elseBranch) {
        this.policy = new Union(
            new Sequence(new Filter(cond), thenBranch),
            new Sequence(new Filter(new Not(cond)), elseBranch));
    }

    public String toString() {
        return this.policy.toString();
    }
}
