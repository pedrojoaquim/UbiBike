package pt.ulisboa.tecnico.cmu.ubibike.peercommunication.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmu.ubibike.ApplicationContext;
import pt.ulisboa.tecnico.cmu.ubibike.peercommunication.NearbyPeerCommunication;



public class IncomingCommunicationTask extends AsyncTask<Void, String, Void> {

    private SimWifiP2pSocketServer serverSocket;

    @Override
    protected Void doInBackground(Void... params) {

        String receivedContent = "";

        try {

            serverSocket = new SimWifiP2pSocketServer(10001);   //TODO

            ApplicationContext.getInstance().getNearbyPeerCommunication().setDeviceServerSocket(serverSocket);

        } catch (IOException e) {
            Log.e("Uncaught exception", e.toString());
        }
        while (!Thread.currentThread().isInterrupted()) {

            try {

                SimWifiP2pSocket sock = serverSocket.accept();

                try {
                    BufferedReader sockIn = new BufferedReader(
                            new InputStreamReader(sock.getInputStream()));
                    String st = sockIn.readLine();
                    receivedContent += st;
                    sock.getOutputStream().write(("\n").getBytes());

                    NearbyPeerCommunication.processReceivedMessage(receivedContent);
                }
                catch (IOException e) {
                    Log.d("Error reading socket:", e.getMessage());
                }
            }
            catch (IOException e) {
                Log.d("Error socket:", e.getMessage());
                break;
            }
        }

        return null;
    }
}