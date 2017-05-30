package sql;

import java.util.HashSet;
import java.util.Set;

public class Relation {
    public static Relation RESTAURANTS = new Relation("restaurants", "restaurant");
    public static Relation MACBOOKS = new Relation("macbooks", "model");
    public static Relation FOOTBALL = new Relation("football", "team");
    public static Relation YELP = new Relation("yelp", "restaurant");
    public static Relation PHONES = new Relation("phones", "model");

    static {
        RESTAURANTS.addAttribute("user_rating", "user rating");
        RESTAURANTS.addAttribute("price");
        RESTAURANTS.addAttribute("cuisine");

        MACBOOKS.addAttribute("inch_display", "inch display");
        MACBOOKS.addAttribute("gigabytes_of_memory", "gigabytes of memory");
        MACBOOKS.addAttribute("gigabytes_of_storage", "gigabytes of storage");
        MACBOOKS.addAttribute("dollars");
        MACBOOKS.addAttribute("gigahertz");
        MACBOOKS.addAttribute("processor");
        MACBOOKS.addAttribute("hours_battery_life", "hours battery life");
        MACBOOKS.addAttribute("trackpad");
        MACBOOKS.addAttribute("pounds");

        FOOTBALL.addAttribute("wins");
        FOOTBALL.addAttribute("losses");
        FOOTBALL.addAttribute("win_percentage");
        FOOTBALL.addAttribute("total_points_for");
        FOOTBALL.addAttribute("total_points_against");
        FOOTBALL.addAttribute("net_points_scored");
        FOOTBALL.addAttribute("touchdowns");
        FOOTBALL.addAttribute("conference");

        YELP.addAttribute("user_rating", "user rating");
        YELP.addAttribute("price");
        YELP.addAttribute("reviews");
        YELP.addAttribute("area");
        YELP.addAttribute("category");

        PHONES.addAttribute("core_processors", "core processors");
        PHONES.addAttribute("operating_system", "operating system");
        PHONES.addAttribute("grams");
        PHONES.addAttribute("megapixels");
        PHONES.addAttribute("gigabytes_of_storage", "gigabytes_of_storage");
        PHONES.addAttribute("gigabytes_of_ram", "gigabytes of ram");
    }

    String name;
    Attribute primaryKey;
    Set<Attribute> attributes;

    public Relation(String name, String primaryKey) {
        this.name = name;
        this.primaryKey = new Attribute(primaryKey);
        this.attributes = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void addAttribute(String attribute, String alias) {
        attributes.add(new Attribute(attribute, alias));
    }

    public void addAttribute(String attribute) {
        attributes.add(new Attribute(attribute));
    }

    public Query queryWithColumns(int c) {
        String[] columns = new String[c+1];
        columns[0] = primaryKey.formatSQL();
        int i = 1;
        for (Attribute a : attributes) {
            if (i >= columns.length) {
                break;
            }
            columns[i] = a.formatSQL();
            i++;
        }
        return new Query(columns, this);
    }

}
