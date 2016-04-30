package pt.ulisboa.tecnico.cmu.ubibike.peercommunication.termite;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.ulisboa.tecnico.cmu.ubibike.ApplicationContext;
import pt.ulisboa.tecnico.cmu.ubibike.UbiBike;
import pt.ulisboa.tecnico.cmu.ubibike.domain.Bike;
import pt.ulisboa.tecnico.cmu.ubibike.peercommunication.CommunicationTasks;
import pt.ulisboa.tecnico.cmu.ubibike.peercommunication.GroupChat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class SimWifiP2pBroadcastReceiver extends  BroadcastReceiver{
    private UbiBike mActivity;

    public SimWifiP2pBroadcastReceiver(UbiBike activity) {
        super();
        mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
            if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(mActivity, "WiFi Direct enabled",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mActivity, "WiFi Direct disabled",
                        Toast.LENGTH_SHORT).show();
            }

        } else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            Toast.makeText(mActivity, "Peer list changed",
                    Toast.LENGTH_SHORT).show();

            SimWifiP2pDeviceList devices = (SimWifiP2pDeviceList) intent.getSerializableExtra(
                                                            SimWifiP2pBroadcast.EXTRA_DEVICE_LIST);

            checkBookedBikeInRange(devices);

            Set<String> devicesBeforeUpdate = ApplicationContext.getInstance().
                                                getNearbyPeerCommunication().getNearDevicesSet();

            Set<String> devicesAfterUpdate = new HashSet<>();

            for(SimWifiP2pDevice device : devices.getDeviceList()){
                devicesAfterUpdate.add(device.deviceName);
            }

            Set<String>  newDevices = new HashSet(devicesAfterUpdate);
            newDevices.removeAll(devicesBeforeUpdate);

            for(String newDevice : newDevices){

                String newDeviceVirtAddr = devices.getByName(newDevice).virtDeviceAddress;

                ApplicationContext.getInstance().
                        getNearbyPeerCommunication().addDeviceNearby(newDevice, newDeviceVirtAddr);

               /* ApplicationContext.getInstance().getActivity().getCommunicationTasks().new OutgoingCommunicationTask.executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR, newDevice);*/

                //TODO continue here

            }



        } else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {

            Toast.makeText(mActivity, "Network membership changed",
                    Toast.LENGTH_SHORT).show();

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);


            ApplicationContext.getInstance().getNearbyPeerCommunication().




        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {

            SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                    SimWifiP2pBroadcast.EXTRA_GROUP_INFO);


        }
    }


    /**
     * Checks whether or not user is near to booked bike
     * Notifies Trajectory tracking service if so
     *
     * @param devices - peers in range list
     */
    private void checkBookedBikeInRange(SimWifiP2pDeviceList devices){

        Bike bookedBike = ApplicationContext.getInstance().getData().getBikeBooked();

        //if there is booked bike, check whether or not is nearby
        if(bookedBike != null) {

            for (SimWifiP2pDevice device : devices.getDeviceList()) {

                //we are near booked bike
                if (device.deviceName.equals(bookedBike.getUuid())) {
                    mActivity.notifyNearBookedBike(true);
                    return;
                }
            }

            //if we didnt return yet, booked bike isnt in range -> notify tracking service
            mActivity.notifyNearBookedBike(false);
        }
    }
}