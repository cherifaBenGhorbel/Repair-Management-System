package models;

import java.sql.Blob;
import javafx.scene.image.Image;

public class PiecesDetachees {
    private int id;
    private String name;
    private double price;
    private Image photo;

    public PiecesDetachees(int id, String name, double price, Image photo) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.photo = photo;
    }


	


	public PiecesDetachees(int id, String name, double price, Blob blob) {
		super();
		this.id = id;
		this.name = name;
		this.price = price;
	}




	public PiecesDetachees(int id, String name, Image photo) {
		super();
		this.id = id;
		this.name = name;
		this.photo = photo;
	}



	public PiecesDetachees(String name, double price) {
		super();
		this.name = name;
		this.price = price;
	}


	public PiecesDetachees(int id, String name, double price) {
		super();
		this.id = id;
		this.name = name;
		this.price = price;
	}



	public PiecesDetachees(int id, String name, Blob blob) {
		super();
		this.id = id;
		this.name = name;

	}



	// Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Image getPhoto() {
        return photo;
    }

    public void setPhoto(Image photo) {
        this.photo = photo;
    }





}
