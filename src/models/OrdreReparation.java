package models;

public class OrdreReparation {
    private int ordreId;
    private String clientName;
    private String description;
    private Appareil appareil;
    private int nbHeuresMO;

    public OrdreReparation(int ordreId, String clientName, String description, Appareil appareil, int nbHeuresMO) {
        this.ordreId = ordreId;
        this.clientName = clientName;
        this.description = description;
        this.appareil = appareil;
        this.nbHeuresMO = nbHeuresMO;
    }



	public OrdreReparation(int ordreId, String description) {
		super();
		this.ordreId = ordreId;
		this.description = description;
	}



	public int getOrdreId() {
        return ordreId;
    }

    public void setOrdreId(int ordreId) {
        this.ordreId = ordreId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNbHeuresMO() {
        return nbHeuresMO;
    }

    public void setNbHeuresMO(int nbHeuresMO) {
        this.nbHeuresMO = nbHeuresMO;
    }

    public Appareil getAppareil() {
        return appareil;
    }

    public void setAppareil(Appareil appareil) {
        this.appareil = appareil;
    }

    @Override
    public String toString() {
        return description != null ? description : ""; // If description is null, return an empty string
    }
}
