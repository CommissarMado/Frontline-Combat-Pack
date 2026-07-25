package frontline.combat.fcp.firecontrol;

public record FireControlComputation(FireControlStatus status, FireControlSolution solution) {

    public static FireControlComputation failure(FireControlStatus status) {
        return new FireControlComputation(status, null);
    }

    public static FireControlComputation success(FireControlSolution solution) {
        return new FireControlComputation(FireControlStatus.ALIGNING, solution);
    }

    public boolean isSuccess() {
        return solution != null;
    }
}
