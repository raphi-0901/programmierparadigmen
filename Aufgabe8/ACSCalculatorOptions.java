public record ACSCalculatorOptions(int iterations, int antAmount, double probability, double alpha, double beta,
                                   double persistence) {
    /**
     * @param iterations  number of iteration of the algorithm - must be > 0
     * @param antAmount   number of ants for the calculation - increases accuracy per iteration - must be > 0
     * @param probability probability of ants taking the best vertex - must be >= 0
     * @param alpha       impact of pheromones on ant decision-making - must be >= 0
     * @param beta        impact of distance on ant decision-making - must be >= 0
     * @param persistence factor of pheromone retention - should be 0<=x<=1. if the value is not within these bounds, it gets cut the nearest border
     */
    public ACSCalculatorOptions(int iterations, int antAmount, double probability, double alpha, double beta, double persistence) {
        this.iterations = iterations;
        this.antAmount = antAmount;
        this.probability = probability;
        this.alpha = alpha;
        this.beta = beta;
        this.persistence = Math.clamp(persistence, 0, 1);
    }

    @Override
    public String toString() {
        return "ACSCalculatorOptions{" +
                "antAmount=" + antAmount +
                ", probability=" + probability +
                ", alpha=" + alpha +
                ", beta=" + beta +
                ", persistence=" + persistence +
                ", iterations=" + iterations +
                '}';
    }
}
