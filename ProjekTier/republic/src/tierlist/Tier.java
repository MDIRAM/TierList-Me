package tierlist;

import java.util.ArrayList; import java.util.Iterator;

public abstract class Tier {

public ArrayList<TierItem> items = new ArrayList<>();

public void addItem(TierItem item) {
    items.add(item);
}

public TierItem findItem(String name) {
    for (TierItem item : items) {
        if (item.getName().equalsIgnoreCase(name)) {
            return item;
        }
    }
    return null;
}

public void removeItem(TierItem item) {
    items.remove(item);
}

public void showItems() {
    System.out.print(getTierName() + " : ");
    if (items.isEmpty()) {
        System.out.println("-");
    } else {
        Iterator<TierItem> it = items.iterator();
        while (it.hasNext()) {
            System.out.print(it.next().getName());
            if (it.hasNext()) {
                System.out.print(", ");
            }
        }
        System.out.println();
    }
}

public abstract String getTierName();

}

