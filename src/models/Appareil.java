package models;

public class Appareil {
    private int appareilId;
    private String description;
    private String marque;
    private int clientId;
    private int categorieId;

    public Appareil(int appareilId, String description, String marque, int clientId, int categorieId) {
        this.appareilId = appareilId;
        this.description = description;
        this.marque = marque;
        this.clientId = clientId;
        this.categorieId = categorieId;
    }

    public Appareil(String description, String marque, int clientId, int categorieId) {
        this.description = description;
        this.marque = marque;
        this.clientId = clientId;
        this.categorieId = categorieId;
    }

    @Override
    public String toString() {
        return description != null ? description : ""; // If description is null, return an empty string
    }

    public int getAppareilId() {
        return appareilId;
    }

    public void setAppareilId(int appareilId) {
        this.appareilId = appareilId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }
}
