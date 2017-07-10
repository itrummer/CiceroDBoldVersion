package sql;

public class Attribute {
    String attribute;
    String alias;

    public Attribute(String attribute, String alias) {
        this.attribute = attribute;
        this.alias = alias;
    }

    public Attribute(String attribute) {
        this(attribute, null);
    }

    public String getName() {
        return alias != null ? alias : attribute;
    }

    public String formatSQL() {
        return alias == null ? attribute : attribute + " as \"" + alias + "\"";
    }
}
