package tierlist;

public class TierListData {
    private String name;
    private String description;
    private TierManager manager;

    public TierListData(String name, String description) {
        this.name = name;
        this.description = description;
        this.manager = new TierManager();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TierManager getManager() {
        return manager;
    }
}
