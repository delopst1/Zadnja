public class User {
    private int id;
    private String ime;
    private String priimek;
    private String email;
    private String davcnaStevilka;

    public User(int id, String ime, String priimek, String email, String davcnaStevilka) {
        this.id = id;
        this.ime = ime;
        this.priimek = priimek;
        this.email = email;
        this.davcnaStevilka = davcnaStevilka;
    }

    public String getIme() {
        return ime;
    }

    public String getPriimek() {
        return priimek;
    }

    public String getEmail() {
        return email;
    }
}
