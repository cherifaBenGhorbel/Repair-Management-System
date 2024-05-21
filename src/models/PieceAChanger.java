package models;

public class PieceAChanger {
    private int pieceOrdreId;
    private OrdreReparation ordre;
    private PiecesDetachees piece;
    private int quantite;
    private String clientName;

    public PieceAChanger(int pieceOrdreId, OrdreReparation ordre, PiecesDetachees piece, int quantite) {
        this.pieceOrdreId = pieceOrdreId;
        this.ordre = ordre;
        this.piece = piece;
        this.quantite = quantite;
    }

    public int getPieceOrdreId() {
        return pieceOrdreId;
    }

    public void setPieceOrdreId(int pieceOrdreId) {
        this.pieceOrdreId = pieceOrdreId;
    }

    public OrdreReparation getOrdre() {
        return ordre;
    }

    public void setOrdre(OrdreReparation ordre) {
        this.ordre = ordre;
    }

    public PiecesDetachees getPiece() {
        return piece;
    }

    public void setPiece(PiecesDetachees piece) {
        this.piece = piece;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
}
