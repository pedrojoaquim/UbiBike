package pt.ulisboa.tecnico.cmu.ubibike.services;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;


import java.util.ArrayList;

import pt.ulisboa.tecnico.cmu.ubibike.ApplicationContext;
import pt.ulisboa.tecnico.cmu.ubibike.domain.BikePickupStation;
import pt.ulisboa.tecnico.cmu.ubibike.domain.Trajectory;
import pt.ulisboa.tecnico.cmu.ubibike.utils.SphericalUtil;

public class TrajectoryTracker extends Service implements LocationListener {


    private static final long TRACKING_TIME_INTERVAL = 1000;    //in milisseconds
    private static final float TRACKING_MINIMUM_DISTANCE = 5;   //in meters

    private boolean mPositionChanged;
    private Location mLastPosition;

    private boolean mNearStation = false;
    private BikePickupStation mStation;

    private boolean mNearBike = false;

    private boolean mNewTrajectory = false;
    private boolean mTrackingEnabled = false;
    private boolean mFinishTracking = false;
    private boolean mStopTracking = false;
    private StopTrajectoryTrackingReceiver mStopTrackingReceiver;
    private NearBookedBikeReceiver mNearBikeReceiver;

    private Trajectory mTrajectory;

    @Override
    public void onCreate() {
        super.onCreate();


        LocationManager lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ignore, as permissions are set
            return;
        }
        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TRACKING_TIME_INTERVAL,TRACKING_MINIMUM_DISTANCE, this);

        mLastPosition = lManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(mLastPosition != null) {
            Toast.makeText(TrajectoryTracker.this, "Last Position: " + mLastPosition.toString(), Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(TrajectoryTracker.this, "Last Position = null", Toast.LENGTH_SHORT).show();
            mLastPosition = new Location(LocationManager.GPS_PROVIDER);
        }


        //registering stop receiver
        IntentFilter filterStop = new IntentFilter(StopTrajectoryTrackingReceiver.STOP);
        filterStop.addCategory(Intent.CATEGORY_DEFAULT);
        mStopTrackingReceiver = new StopTrajectoryTrackingReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mStopTrackingReceiver, filterStop);

        //registering near booked bike receiver
        IntentFilter filterNearby = new IntentFilter(NearBookedBikeReceiver.NEAR_BOOKED_BIKE);
        filterNearby.addCategory(Intent.CATEGORY_DEFAULT);
        mNearBikeReceiver = new NearBookedBikeReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mNearBikeReceiver, filterNearby);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mStopTrackingReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNearBikeReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        MyRunnable mainBackgroundRunnable = new MyRunnable();
        Thread thread = new Thread(mainBackgroundRunnable);
        thread.start();

        Toast.makeText(TrajectoryTracker.this, "Tracking thread started", Toast.LENGTH_SHORT).show();

        return START_NOT_STICKY;
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("UbiBike", "NewLocation: " + location.getLatitude() + "   " + location.getLongitude());

        //was user near station on previous position
        boolean prevPositionNearStation = mNearStation;

        if(mLastPosition != location){
            mLastPosition = location;
            mPositionChanged = true;

            ApplicationContext.getInstance().getData().setLastPosition(location.getLatitude(),
                                                                        location.getLongitude());

            ApplicationContext.getInstance().updateUI();

        }
        else{
            mPositionChanged = false;
        }

        if(mPositionChanged){

            ArrayList<BikePickupStation> stations = ApplicationContext.getInstance().getData().getBikeStations();

            mNearStation = false;

            //checking if user is near some station
            for(BikePickupStation station : stations){

                LatLng currentPosition = new LatLng(mLastPosition.getLatitude(), mLastPosition.getLongitude());
                double distance = SphericalUtil.computeDistanceBetween(currentPosition, station.getStationPosition());

                //in range of 5 meters, why not
                if(distance < 5.0){
                    mNearStation = true;
                    mStation = station;
                    break;
                }
            }
        }

        //START TRACKING - user moved away from station on bike (wasnt already being tracked)
        //BIKE PICK UP
        if(prevPositionNearStation && !mNearStation && mNearBike && !mTrackingEnabled){
            mTrackingEnabled = true;
            mNewTrajectory = true;

            int bookedBikeID = ApplicationContext.getInstance().getData().getBikeBooked().getBid();

            //telling server
            ApplicationContext.getInstance().getServerCommunicationHandler().
                    performBikePickDropRequest(bookedBikeID, mStation.getSid(), true);

        }
        //PAUSE TRACKING - user is away from station and got off his bike
        else if(!prevPositionNearStation && !mNearStation && !mNearBike){
            mTrackingEnabled = false;
        }
        //RESUME TRACKING - user is away from station and on his bike
        else if(!prevPositionNearStation && !mNearStation && mNearBike){
            mTrackingEnabled = true;
        }
        //PAUSE TRACKING - user near station but off his bike
        else if(prevPositionNearStation && mNearStation && !mNearBike){
            mTrackingEnabled = false;
        }
        //FINISH TRACKING - user parked the bike and left the station
        //BIKE DROP
        else if(prevPositionNearStation && !mNearStation && !mNearBike){
            mTrackingEnabled = false;
            mFinishTracking = true;

            int bookedBikeID = ApplicationContext.getInstance().getData().getBikeBooked().getBid();

            //telling server
            ApplicationContext.getInstance().getServerCommunicationHandler().
                    performBikePickDropRequest(bookedBikeID, mStation.getSid(), false);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(TrajectoryTracker.this, "OnStatusChanged", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(TrajectoryTracker.this, "OnProviderEnabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(TrajectoryTracker.this, "OnProviderDisabled", Toast.LENGTH_SHORT).show();
    }


    private class MyRunnable implements Runnable {

        public void run() {

            //main tracking loop
            while(!mStopTracking){

                if(mTrackingEnabled && mPositionChanged) {

                    //new trajectory to register
                    if (mNewTrajectory) {
                        int trajectoryID = ApplicationContext.getInstance().getData().getNextTrajectoryID();
                        int startStationID = mStation.getSid();
                        double startLatitude = mStation.getPositionLatitude();
                        double startLongitude = mStation.getPositionLongitude();

                        mTrajectory = new Trajectory(trajectoryID, startStationID, startLatitude, startLongitude);
                        mNewTrajectory = false;

                        Log.d("UbiBike", "[Trajectory " + mTrajectory.getTrajectoryID() + "]" + "Starting registering new trajectory.");
                    }

                    mTrajectory.addRoutePosition(mLastPosition.getLatitude(), mLastPosition.getLongitude());

                    Log.d("UbiBike", "[Trajectory " + mTrajectory.getTrajectoryID() + "]" + "Position added");

                    mPositionChanged = false;
                }
                //finish current trajectory tracking
                else if(mFinishTracking) {
                    mTrajectory.finishRoute();

                    mTrajectory.setEndStationID(mStation.getSid());

                    Log.d("UbiBike", "[Trajectory " + mTrajectory.getTrajectoryID() + "]" + "Tracking finished");

                    ApplicationContext.getInstance().getData().setLastTrackedTrajectory(mTrajectory);

                    if(ApplicationContext.getInstance().getActivity() != null){
                        ApplicationContext.getInstance().getActivity().
                                showTrajectoryOnMap(mTrajectory.getTrajectoryID(), true, false);
                    }

                    mFinishTracking = false;
                }

                //Sleep a little bit to avoid constant looping
               /* try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("Uncaught exception", e.toString());
                }*/
            }

            stopSelf();
        }
    };


    /**
     * Receiver to catch Intent that signals when user is near bike previously booked
     */
    public class NearBookedBikeReceiver extends BroadcastReceiver {

        public static final String NEAR_BOOKED_BIKE = "near_booked_bike";

        @Override
        public void onReceive(Context context, Intent intent) {
            mNearBike = intent.getBooleanExtra(NEAR_BOOKED_BIKE, false);

            if(mNearBike) {
                Toast.makeText(TrajectoryTracker.this, "Booked bike nearby", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(TrajectoryTracker.this, "Booked bike isn't nearby", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Receiver to catch Intent that signals service to terminate
     */
    public class StopTrajectoryTrackingReceiver extends BroadcastReceiver {

        public static final String STOP = "stop_tracking_service";

        @Override
        public void onReceive(Context context, Intent intent) {
            mStopTracking = true;
            Toast.makeText(TrajectoryTracker.this, "", Toast.LENGTH_SHORT).show();
        }
    }
}
