package tierlist;

public class TierManager {

    private Tier s = new STier();
    private Tier a = new ATier();
    private Tier b = new BTier();
    private Tier c = new CTier();
    private Tier d = new DTier();

    public Tier getTier(String name) {
        switch (name.toUpperCase()) {
            case "S": return s;
            case "A": return a;
            case "B": return b;
            case "C": return c;
            case "D": return d;
            default: return null;
        }
    }

    public void showAll() {
        System.out.println("\n===== TIER LIST =====");
        s.showItems();
        a.showItems();
        b.showItems();
        c.showItems();
        d.showItems();
    }

    public boolean moveItem(String itemName, String fromTier, String toTier) {
        Tier from = getTier(fromTier);
        Tier to = getTier(toTier);

        if (from == null || to == null) return false;

        TierItem item = from.findItem(itemName);
        if (item == null) return false;

        from.removeItem(item);
        to.addItem(item);
        return true;
    }
}
