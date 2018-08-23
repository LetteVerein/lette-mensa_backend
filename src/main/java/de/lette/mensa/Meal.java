package de.lette.mensa;

public class Meal {

    private String name;
    private String beachte;
    private String kcal;
    private String eiweisse;
    private String fette;
    private String kohlenhydrate;
    private String beschreibung;
    private String preis;
    private String zusatzstoffe;

    public Meal(String name, String preis)
    {
        this.name = name;
        this.preis = preis;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBeachte() {
        return beachte;
    }

    public void setBeachte(String beachte) {
        this.beachte = beachte;
    }

    public String getKcal() {
        return kcal;
    }

    public void setKcal(String kcal) {
        this.kcal = kcal;
    }

    public String getEiweisse() {
        return eiweisse;
    }

    public void setEiweisse(String eiweisse) {
        this.eiweisse = eiweisse;
    }

    public String getFette() {
        return fette;
    }

    public void setFette(String fette) {
        this.fette = fette;
    }

    public String getKohlenhydrate() {
        return kohlenhydrate;
    }

    public void setKohlenhydrate(String kohlenhydrate) {
        this.kohlenhydrate = kohlenhydrate;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getPreis() {
        return preis;
    }

    public void setPreis(String preis) {
        this.preis = preis;
    }

    public String getZusatzstoffe() {
        return zusatzstoffe;
    }

    public void setZusatzstoffe(String zusatzstoffe) {
        this.zusatzstoffe = zusatzstoffe;
    }
}
