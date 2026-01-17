package tierlist;

public class TierItem {
    private String name;
    private String imagePath;

    public TierItem(String name) {
        this.name = name;
        this.imagePath = null;
    }

    public TierItem(String name, String imagePath) {
        this.name = name;
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}

