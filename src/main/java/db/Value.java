package db;

/**
 * Represents a singular value
 */
public class Value {
    Object data;

    public Value(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public boolean isCategorical() {
        return data.getClass().equals(String.class);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Value) {
            return data.equals(((Value) obj).data);
        }
        return false;
    }
}
