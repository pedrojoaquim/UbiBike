package pt.ulisboa.tecnico.cmu.ubibike.fragments.maps;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.style.UpdateAppearance;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmu.ubibike.ApplicationContext;
import pt.ulisboa.tecnico.cmu.ubibike.R;
import pt.ulisboa.tecnico.cmu.ubibike.UbiBike;
import pt.ulisboa.tecnico.cmu.ubibike.domain.BikePickupStation;
import pt.ulisboa.tecnico.cmu.ubibike.domain.Trajectory;
import pt.ulisboa.tecnico.cmu.ubibike.fragments.UpdatableUI;


/**
 * Created by andriy on 12.03.2016.
 */
public abstract class MapFragment extends Fragment implements UpdatableUI {

    protected GoogleMap mGoogleMap;
    protected SupportMapFragment mSupportMapFragment;

    protected View mView;


    protected abstract void onCreateSpecific();
    protected abstract void setUIElements();
    protected abstract void showSpecificMap();
    public abstract void updateUI();


    protected UbiBike getParentActivity(){
        return (UbiBike) getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getParentActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mView =  inflater.inflate(R.layout.map_fragment, null, false);

        setHasOptionsMenu(true);
        getParentActivity().invalidateOptionsMenu();

        onCreateSpecific();

        setUIElements();

        setMap();

        return mView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_map_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    private void setMap(){

        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.add(R.id.google_map, mSupportMapFragment).commit();
        }

        if (mSupportMapFragment != null) {

            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    if (googleMap != null) {

                        mGoogleMap = googleMap;

                        googleMap.getUiSettings().setAllGesturesEnabled(true);

                        showSpecificMap();
                    }
                }
            });
        }
    }
}