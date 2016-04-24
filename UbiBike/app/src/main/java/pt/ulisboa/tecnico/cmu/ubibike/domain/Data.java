package pt.ulisboa.tecnico.cmu.ubibike.domain;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by andriy on 12.03.2016.
 */
public class Data {

    private int uid;
    private String username;
    private String sessionToken;
    private String publicKeyToken;

    //private ArrayList<Chat> mConversations;

    private HashMap<Integer, BikePickupStation> mBikeStations;
    private ArrayList<Trajectory> mTrajectories;
    private Trajectory mLastTrackedTrajectory;
    private Bike mBikeBooked;
    private int mCurrentBikeBookingStation;
    private LatLng mLastPosition;
    private Date dateUpdated;




    public Data(int id, String usrn) {
        uid = id;
        username = usrn;
        //mConversations = new ArrayList<>();
        mBikeStations = new HashMap<>();
        mTrajectories = new ArrayList<>();
        mLastPosition = new LatLng(0.0, 0.0); //TODO last position
        dateUpdated = new Date();
    }

    public Data(int uid, String username, String sessionToken, String publicKeyToken, ArrayList<String/*TODO change*/> mConversations,
                ArrayList<BikePickupStation> mBikeStations, ArrayList<Trajectory> mTrajectories,
                LatLng mLastPosition, Date dateUpdated) {

        HashMap<Integer, BikePickupStation> stations = new HashMap<>();

        for(BikePickupStation station : mBikeStations){
            stations.put(station.getSid(), station);
        }

        this.uid = uid;
        this.username = username;
        this.sessionToken = sessionToken;
        this.publicKeyToken = publicKeyToken;
        //this.mConversations = mConversations;
        this.mBikeStations = stations;
        this.mTrajectories = mTrajectories;
        this.mLastPosition = mLastPosition;
        this.dateUpdated = dateUpdated;
        //this.peersNearby = new HashMap<>();
    }


    /**
     * Gets all conversations
     *
     * @return - list of chats
     */
    /*public ArrayList<Chat> getConversations() {
        return mConversations;
    }*/


    /**
     * Gets bike stations with bikes available to pickup
     *
     * @return - list of stations
     */
    public ArrayList<BikePickupStation> getBikeStations() {
        return new ArrayList<>(mBikeStations.values());
    }

    /**
     * Gets bike stations with a given ID
     *
     * @param sid - station ID
     * @return - BikePickupStation object
     */
    public BikePickupStation getBikePickupStationById(int sid){
        return mBikeStations.get(sid);
    }

    /**
     * Gets a Trajectory given an id
     *
     * @param trajectoryID - id
     * @return - Trajectory
     */
    public Trajectory getTrajectory(int trajectoryID) {
        return mTrajectories.get(new Integer(trajectoryID));
    }

    /**
     * @return - number of trajectories
     */
    public int getTrajectoriesCount() {
        return mTrajectories.size();
    }


    /**
     * Gets all past trajectories sorted by the time they finished
     *
     * @return - trajectories list
     */
    public ArrayList<Trajectory> getAllTrajectories() {

        ArrayList<Trajectory> trajectories = mTrajectories;
        Collections.sort(trajectories);

        return trajectories;
    }

    /**
     * Sets last GPS synced position to given one
     *
     * @param latitude  - coordinate
     * @param longitude - coordinate
     */
    public void setLastPosition(double latitude, double longitude) {
        mLastPosition = new LatLng(latitude, longitude);
    }

    /**
     * @return - last GPS synced position
     */
    public LatLng getLastPosition() {
        return new LatLng(38.737681, -9.138382);    //TODO hardcoded
    }

    /**
     * Sets booked bike
     *
     * @param bike - Bike object to add to collection
     */
    public void setBikeBooked(Bike bike){
        mBikeBooked = bike;
    }

    public Bike getBikeBooked(){
        return mBikeBooked;
    }

    public Date getLastUpdated() {
        return dateUpdated;
    }

    public void setLastUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getPublicKeyToken() {
        return publicKeyToken;
    }

    public boolean hasPublicKeyToken(){ return publicKeyToken != null; }

    public void setPublicKeyToken(String publicKeyToken) {
        this.publicKeyToken = publicKeyToken;
    }

    public void setTrajectories(ArrayList<Trajectory> trajectories) {
        mTrajectories = trajectories;
    }

    public Trajectory getLastTrackedTrajectory() {
        return mLastTrackedTrajectory;
    }

    public void setLastTrackedTrajectory(Trajectory mLastTrackedTrajectory) {
        this.mLastTrackedTrajectory = mLastTrackedTrajectory;
    }

    public void setBikeStations(ArrayList<BikePickupStation> bikeStations) {
        HashMap<Integer, BikePickupStation> stations = new HashMap<>();

        for(BikePickupStation station : bikeStations){
            stations.put(station.getSid(), station);
        }

        mBikeStations = stations;
    }

    public int getTotalPointsEarned(){
        int total = 0;

        for(Trajectory trj: mTrajectories){
            total += trj.getPointsEarned();
        }

        return total;
    }
}
