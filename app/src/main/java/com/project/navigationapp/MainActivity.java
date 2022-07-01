package com.project.navigationapp;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.mapboxsdk.MapmyIndia;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mmi.services.account.MapmyIndiaAccountManager;
import com.mmi.services.api.autosuggest.MapmyIndiaAutoSuggest;
import com.mmi.services.api.autosuggest.model.AutoSuggestAtlasResponse;
import com.project.navigationapp.extras.Config;
import com.project.navigationapp.extras.SuggestedLocationRes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class MainActivity extends Activity implements AutoFillAdapter.MyCallback {

    RecyclerView autofill_recycler;
    MapView mapView;
    public MapboxMap mapboxMap1;
    private RecyclerView.Adapter adapter;
    private ArrayList<String> placename = new ArrayList<>();
    private ArrayList<String> placeaddress = new ArrayList<>();
    private ArrayList<Double> latitude = new ArrayList<>();
    private ArrayList<Double> longitude = new ArrayList<>();
    private List<SuggestedLocationRes> autoFillRes = new ArrayList<SuggestedLocationRes>();


    public abstract void onAppMapReady(MapboxMap mapboxMap);

    public abstract void onAutofillRowSelected(final double latitude, final double longitude);


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapmyIndiaAccountManager.getInstance().setRestAPIKey(Config.rest_api_key);
        MapmyIndiaAccountManager.getInstance().setMapSDKKey(Config.map_sdk_key);
        MapmyIndiaAccountManager.getInstance().setAtlasClientId(Config.client_id);
        MapmyIndiaAccountManager.getInstance().setAtlasClientSecret(Config.client_secret);
        MapmyIndiaAccountManager.getInstance().setAtlasGrantType(Config.grant_type);
        MapmyIndia.getInstance(this);

        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                mapboxMap1 = mapboxMap;
                mapboxMap.setMinZoomPreference(2.5);
                mapboxMap.setMaxZoomPreference(18.5);
                mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
                mapboxMap.getUiSettings().setTiltGesturesEnabled(false);
                mapboxMap.setPadding(20, 20, 20, 20);
                mapboxMap.getUiSettings().setLogoMargins(0, 0, 0, 0);
                onAppMapReady(mapboxMap);
            }

            @Override
            public void onMapError(int i, String s) {

            }
        });

        mapView.onCreate(savedInstanceState);

        EditText whereto = findViewById(R.id.where);

        whereto.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (whereto.getText().toString().length() > 1) {
                    placename.clear();
                    placeaddress.clear();
                    latitude.clear();
                    longitude.clear();

                    MapmyIndiaAutoSuggest.builder()
                            .query(whereto.getText().toString())
                            .build()
                            .enqueueCall(new Callback<AutoSuggestAtlasResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<AutoSuggestAtlasResponse> call, @NonNull Response<AutoSuggestAtlasResponse> response) {
                                    for (int i = 0; i < response.body().getSuggestedLocations().size(); i++) {
                                        placename.add(response.body().getSuggestedLocations().get(i).placeName);
                                        placeaddress.add(response.body().getSuggestedLocations().get(i).placeAddress);
                                        latitude.add(Double.parseDouble(response.body().getSuggestedLocations().get(i).latitude));
                                        longitude.add(Double.parseDouble(response.body().getSuggestedLocations().get(i).longitude));
                                    }
                                    autofill_recycler = findViewById(R.id.autofill);
                                    if (adapter == null) {
                                        adapter = new AutoFillAdapter(MainActivity.this, placename, placeaddress, MainActivity.this);
                                        autofill_recycler.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
                                        autofill_recycler.setAdapter(adapter);
                                    } else {
                                        adapter.notifyDataSetChanged();
                                        autofill_recycler.scrollToPosition(0);
                                        autofill_recycler.setVisibility(View.VISIBLE);
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<AutoSuggestAtlasResponse> call, @NonNull Throwable t) {
                                    t.printStackTrace();
                                }
                            });

                    if (s.toString().trim().length() == 0) {
                        placename.clear();
                        placeaddress.clear();
                        latitude.clear();
                        longitude.clear();
                        if (adapter != null)
                            adapter.notifyDataSetChanged();
                        autofill_recycler.setVisibility(View.GONE);
                    }
                }
            }
        });


        autofill_recycler = findViewById(R.id.autofill);
        autofill_recycler.addOnItemTouchListener(new RecyclerTouchListener(this,
                autofill_recycler, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                autofill_recycler.setVisibility(View.GONE);
                onAutofillRowSelected(latitude.get(position), longitude.get(position));
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));


    }

    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener) {

            this.clicklistener = clicklistener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recycleView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clicklistener != null) {
                        clicklistener.onLongClick(child, recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)) {
                clicklistener.onClick(child, rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}

