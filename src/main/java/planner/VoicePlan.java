package planner;

/**
 * Representation of a VoicePlan
 */
public abstract class VoicePlan {

    /**
     * Computes the result of a voice plan
     * @return The output for a voice plan
     */
    public abstract String getResult();

    /**
     * Computes the cost of a voice plan as the time taken to speak all parts of a plan
     * @return The cost of this voice plan
     */
    public abstract double getCost();

}
