package com.example.s182093.taqforlocalgoverment;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private String TAG = "Sample";

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    private Boolean isBoolean = false;

    private final ArrayList<LatLng> save_path = new ArrayList<>();
    private ArrayList<String> lltList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent =this.getIntent();
        lltList = intent.getStringArrayListExtra("watasityauyo");
        Button btn = findViewById(R.id.change_mode_button);
        btn.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        int count = 0;
        String stedll[] = new String[2];
        ArrayList<String> getWaypoints = new ArrayList<>();

        for (int i = 0; i < lltList.size(); i++) {

            stedll[0] = lltList.get(0);

            for (int j = 2; j < lltList.size(); j++) {

                if (!lltList.get(j).equals("END")) {

                    getWaypoints.add(lltList.get(j - 1));

                } else {

                    stedll[1] = lltList.get(j - 1);
                    count = j;
                    break;
                }
            }

            for (int j = 0; j <= count; j++) {

                if (lltList.size() != 0) {
                    Log.v(TAG, lltList.get(0));
                    lltList.remove(0);
                } else {
                    break;
                }
            }

            requestGPS(stedll[0], stedll[1], getWaypoints);
            getWaypoints.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(save_path.get(0), 11));

        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.change_mode_button) {
            mMap.clear();
            ArrayList<LatLng> reserve = new ArrayList<>(save_path);
            Button btn = findViewById(R.id.change_mode_button);
            if (isBoolean == true) {
                btn.setText(R.string.route);
                timesAddLine(reserve);

                isBoolean = false;

            } else if (isBoolean == false) {
                btn.setText(R.string.heat_map);
                addHeatMap(reserve);

                isBoolean = true;

            }
        }
    }

    public void requestGPS(String stLatLng, String edLatLng, ArrayList<String> waypoints) {

        //Define list to get all latlng for the route
        List<LatLng> path = new ArrayList();

        String start[] = (stLatLng.split(",", 0));
        String stLat = start[0];
        String stLng = start[1];

        LatLng stPosiLatLng = new LatLng(Double.parseDouble(stLat), Double.parseDouble(stLng));

        String end[] = (edLatLng.split(",", 0));
        String edLat = end[0];
        String edLng = end[1];

        LatLng edPosiLatLng = new LatLng(Double.parseDouble(edLat), Double.parseDouble(edLng));

        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        mMap.addMarker(new MarkerOptions().position(stPosiLatLng).icon(icon));
        mMap.addMarker(new MarkerOptions().position(edPosiLatLng));


        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyAHmbl5Ef0Cud7rt5pvjoRbmHTZ4vtYKAo")
                .build();

        DirectionsApiRequest request = DirectionsApi.getDirections(context, stLatLng, edLatLng);
        for (int i = 0; i < waypoints.size(); i++) {
            request.waypoints(waypoints.get(i));
        }

        try {
            DirectionsResult res = request.await();


            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs != null) {
                    for (int i = 0; i < route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j = 0; j < leg.steps.length; j++) {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0) {
                                    for (int k = 0; k < step.steps.length; k++) {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {

                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {

                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

        //Draw the polyline
        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.RED).width(5);
            mMap.addPolyline(opts);
            for (int i = 0; i < path.size(); i++) {
                save_path.add(path.get(i));
            }
            save_path.add(new LatLng(90.0, -81.0));
            Log.v(TAG,String .valueOf(save_path.size()));
        }



        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edPosiLatLng, 6));

    }


    private void timesAddLine(ArrayList<LatLng> list) {

        LatLng obje = new LatLng(90.0, -81.0);

        if (list.size() > 0) {
            int count = 0;
            for (int i = 0; i < list.size(); i++) {

                ArrayList<LatLng> iii = new ArrayList<>();

                for (int j = 0; j < list.size(); j++) {

                    if (!list.get(j).equals(obje)) {

                        iii.add(list.get(j));

                    } else {

                        count = j;
                        break;

                    }
                }
                BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                mMap.addMarker(new MarkerOptions().position(iii.get(0)).icon(icon));
                mMap.addMarker(new MarkerOptions().position(iii.get(iii.size() - 1)));
                PolylineOptions opts = new PolylineOptions().addAll(iii).color(Color.RED).width(5);
                mMap.addPolyline(opts);

                for (int j = 0; j <= count; j++) {

                    if (list.size() != 0) {

                        list.remove(0);

                    } else {

                        break;

                    }
                }
            }
        }
    }

    private void addHeatMap(ArrayList<LatLng> list) {

        Log.v(TAG,String .valueOf(list.size()));

        ArrayList<LatLng> set = new ArrayList<>();
        Collections.addAll(set, new LatLng(90.0, -81.0));
        list.removeAll(set);


        mProvider = new HeatmapTileProvider.Builder()
                .data(list)     //座標データ
                .opacity(1.0)   //透過度の設定
                .build();
        // ヒートマップタイルプロバイダーを使用して、マップにヒートマップを追加
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }
}