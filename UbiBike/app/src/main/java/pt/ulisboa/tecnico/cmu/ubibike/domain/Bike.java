package pt.ulisboa.tecnico.cmu.ubibike.domain;


public class Bike {

    private int bid;
    private String uuid;    //in our case the WIFI Direct peers device id

    public Bike(int bid, String uuid, int sid) {
        this.bid = bid;
        this.uuid = uuid;
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
}