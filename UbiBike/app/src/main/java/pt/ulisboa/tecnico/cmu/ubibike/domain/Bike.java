package pt.ulisboa.tecnico.cmu.ubibike.domain;


public class Bike {

    private int bid;
    private String uuid;
    private int sid;

    public Bike(int bid, String uuid, int sid) {
        this.bid = bid;
        this.uuid = uuid;
        this.sid = sid;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }
}
