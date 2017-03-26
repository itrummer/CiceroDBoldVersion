package planner;

/**
 * An item to be included in a voice plan. Accepts a VoicePlanner
 */
public interface PlanItem {
    void accept(VoicePlanner voicePlanner);
}
