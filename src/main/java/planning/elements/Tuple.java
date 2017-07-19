package planning.elements;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import planning.Speakable;

import java.util.*;

/**
 * Represents a tuple in a database table
 */
public class Tuple implements Speakable {
    List<String> attributes;
    Map<String, Value> valueAssignments;
    String cachedLongFormResultWithoutContext;

    /**
     * Constructor for a Tuple with a list of String attributes
     */
    public Tuple(List<String> attributes) {
        this.attributes = attributes;
        this.valueAssignments = new HashMap<>();
        this.cachedLongFormResultWithoutContext = null;
    }

    /**
     * Adds a ValueAssignment to this Tuple.
     * @param column The attribute or column name for the value
     * @param value The value for the new value assignment
     */
    public void addValueAssignment(String column, Value value) {
        valueAssignments.put(column, value);
    }

    @JsonIgnore
    public List<String> getAttributes() {
        return attributes;
    }

    public int timeSavingsFromValueDomain(ValueDomain d) {
        Value v = valueAssignments.get(d.getAttribute());
        if (v != null && d.contains(v)) {
            return v.toSpeechText(true).length();
        }
        return 0;
    }

    /**
     * Retrieves the value for the specified attribute
     */
    public Value valueForAttribute(String attribute) {
        return valueAssignments.get(attribute);
    }

    public String toSpeechText(boolean inLongForm) {
        return toSpeechText(null, inLongForm);
    }

    /**
     * Calculates the speech text for speaking this Tuple in a given Context. When given a nonnull
     * Context, we eliminate outputting attribute-value pairs for which the Context fixes a domain.
     * @param c The Context in which to output this Tuple
     * @return The speech representation of this Tuple within the given Context
     */
    public String toSpeechText(Context c, boolean inLongForm) {
        if (c == null && inLongForm && cachedLongFormResultWithoutContext != null) {
            return cachedLongFormResultWithoutContext;
        }
        if (c != null && !c.matches(this)) {
            return toSpeechText(null, inLongForm);
        }
        StringBuilder result = new StringBuilder("");
        boolean firstAttribute = true;
        for (String attribute : attributes) {
            if (c == null || !c.isAttributeFixed(attribute)) {
                Value v = valueForAttribute(attribute);
                if (firstAttribute) {
                    result.append(v.toSpeechText(inLongForm));
                } else {
                    result.append(", ");
                    result.append(v.toSpeechText(inLongForm));
                    result.append(" ");
                    result.append(attribute);
                }
                firstAttribute = false;
            }
        }

        String t = result.toString();
        if (c == null && inLongForm) {
            cachedLongFormResultWithoutContext = t;
        }
        return t;
    }

    @JsonAnyGetter
    public Map<String, Object> getValueAssignments() {
        Map<String, Object> result = new HashMap<>();
        for (String key : valueAssignments.keySet()) {
            result.put(key, valueAssignments.get(key).getValue());
        }
        return result;
    }

    public class ValueAssignmentEntry {
        String attribute;
        Object value;

        public ValueAssignmentEntry(String attribute, Object value) {
            this.attribute = attribute;
            this.value = value;
        }

        public String getAttribute() {
            return attribute;
        }

        public Object getValue() {
            return value;
        }
    }
}
