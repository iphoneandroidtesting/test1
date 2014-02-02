package com.nmotion.android;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.image.loader.ImageFetcher;
import com.nmotion.R;
import com.nmotion.android.models.Restaurant;
import com.nmotion.android.network.NetworkException;
import com.nmotion.android.utils.AppUtils;
import com.nmotion.android.utils.Config;
import com.nmotion.android.view.RestaurantMapListDialog;
import com.nmotion.android.view.SlidingMenu;

public class RestaurantsMapScreen extends FragmentActivity {
    final static int DOUBLE_ACCURACY = 6;
    private boolean firstFix, toDownloadOnLocationObtained;
    private ArrayList<Restaurant> restaurants;
    private CopyOnWriteArrayList<Restaurant> restaurantsVisible = new CopyOnWriteArrayList<Restaurant>();
    private Timer timer;
    double currentLatitude, currentLongitude;
    private SupportMapFragment mapFragment;
    private GoogleMap gmap;
    private MyInfoWindowAdapter adapter;
    private ImageView imageView;
    private EditText searchField;
    private DownloadRestaurantListTask downloadRestaurantListTask;
    private CalculateItemsTask calculateItemsTask;
    private static final int KOEFF = 10;
    private double oldZoomLevel, curZoomLevel;
    private LatLng oldTarget;
    private SlidingMenu baseLayout;
    ImageFetcher fetcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // layout depends on screen size
                                                // and orientation
        baseLayout = (SlidingMenu) findViewById(R.id.base_layout);
        baseLayout.injectMenuById(R.layout.layout_navigation_menu);

        baseLayout.injectContentById(R.layout.layout_restaurants_map);
        findViewById(R.id.btn_menu_list).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.txt_screen_name)).setText(R.string.txt_restaurants_nearby);

        findViewById(R.id.btn_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseLayout.animateToggle();
            }
        });

        imageView = (ImageView) findViewById(R.id.imageView1);
        fetcher = new ImageFetcher(this, 0, 0);
        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        SupportMapFragment.newInstance(new GoogleMapOptions().compassEnabled(true));
        gmap = mapFragment.getMap();
        adapter = new MyInfoWindowAdapter();
        gmap.setInfoWindowAdapter(adapter);
        gmap.setMyLocationEnabled(true);
        gmap.getUiSettings().setCompassEnabled(true);

        gmap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location arg0) {
                currentLatitude = arg0.getLatitude();
                currentLongitude = arg0.getLongitude();
                if (toDownloadOnLocationObtained) {
                    toDownloadOnLocationObtained = false;
                    if (searchField.getText().length() == 0) {
                        download(null);
                    } else if (searchField.getText().length() >= Config.SEARCH_SYMBOLS_LENGTH) {
                        download(searchField.getText().toString());
                    }
                }
            }
        });

        gmap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker arg0) {
                arg0.hideInfoWindow();
                Intent intent = new Intent(RestaurantsMapScreen.this, RestaurantInfoScreen.class);
                for (Restaurant rest : restaurants) {
                    if (rest.name.equals(arg0.getTitle())
                            && new BigDecimal(rest.latitude).setScale(DOUBLE_ACCURACY, BigDecimal.ROUND_HALF_UP)
                                    .doubleValue() == new BigDecimal(arg0.getPosition().latitude).setScale(
                                    DOUBLE_ACCURACY, BigDecimal.ROUND_HALF_UP).doubleValue()
                            && new BigDecimal(rest.longitude).setScale(DOUBLE_ACCURACY, BigDecimal.ROUND_HALF_UP)
                                    .doubleValue() == new BigDecimal(arg0.getPosition().longitude).setScale(
                                    DOUBLE_ACCURACY, BigDecimal.ROUND_HALF_UP).doubleValue()) {
                        if (rest.getList().size() > 0){
                            rest.getList().remove(rest);
                            rest.addList(rest);
                            new RestaurantMapListDialog(RestaurantsMapScreen.this, rest.getList()).show();// show
                        }
                                                                                                          // dialog
                        else {
                            intent.putExtra(RestaurantInfoScreen.DATA_RESTAURANT_ID, rest.id);
                            startActivity(intent);
                        }
                        break;
                    }
                }

            }
        });

        gmap.setOnCameraChangeListener(new OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition arg0) {
                if (!firstFix) {
                    initMap();
                    firstFix = true;
                    return;
                }
                if (oldTarget == null) oldTarget = arg0.target;
                int delta = 80;
                float dx = gmap.getProjection().toScreenLocation(arg0.target).x
                        - gmap.getProjection().toScreenLocation(oldTarget).x;
                float dy = gmap.getProjection().toScreenLocation(arg0.target).y
                        - gmap.getProjection().toScreenLocation(oldTarget).y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > delta || oldZoomLevel != arg0.zoom) {
                    oldZoomLevel = curZoomLevel;
                    curZoomLevel = arg0.zoom;
                    calculateItems();
                }
                oldTarget = arg0.target;
            }
        });

        searchField = (EditText) findViewById(R.id.txt_search_restaurant);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(final Editable s) {
                if (timer != null) {
                    timer.cancel();
                    if (downloadRestaurantListTask != null) {
                        downloadRestaurantListTask.cancel(true);
                    }
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (s.length() == 0) {
                                    download(null);
                                } else if (s.length() >= Config.SEARCH_SYMBOLS_LENGTH) {
                                    download(s.toString());
                                }
                            }
                        });
                    }
                }, 1000, 99999999);
            }
        });
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (App.getInstance().getPreferencesManager().isCrash()){
            AppUtils.showDialog(this, "Error", getString(R.string.crash_message)).setOnDismissListener(new OnDismissListener() {                    
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();                        
                }
            });
        }
    }

    private void download(String search) {
        if (downloadRestaurantListTask != null) {
            downloadRestaurantListTask.cancel(true);
        }
        downloadRestaurantListTask = new DownloadRestaurantListTask();
        downloadRestaurantListTask.execute(String.valueOf(currentLatitude), String.valueOf(currentLongitude), search);
    }

    private void displayPlacesOnMap() {
        restaurants = App.getInstance().getCache().getRestaurants();
        if (restaurants == null || restaurants.size() == 0) {
            if (currentLatitude == 0 && currentLongitude == 0) {
                toDownloadOnLocationObtained = true;
                return;
            }
            if (searchField.getText().length() == 0) {
                download(null);
            } else if (searchField.getText().length() >= Config.SEARCH_SYMBOLS_LENGTH) {
                download(searchField.getText().toString());
            }
            return;
        }

        calculateItems();
        LatLngBounds.Builder bld = new LatLngBounds.Builder();
        final LatLngBounds bounds;
        for (Restaurant rest : restaurants) {
            if (rest.distance<50)
                bld.include(new LatLng(rest.latitude, rest.longitude));
            System.out.println("distance to rest "+rest.distance);
        }
        if (restaurants != null && restaurants.size() > 0) {
            bounds = bld.build();
            try {
                gmap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                if (gmap.getMyLocation()!=null)
                    gmap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(gmap.getMyLocation().getLatitude(), gmap.getMyLocation().getLongitude())));
            } catch (IllegalStateException e) {
                gmap.setOnCameraChangeListener(new OnCameraChangeListener() {

                    @Override
                    public void onCameraChange(CameraPosition arg0) {
                        // Move camera.
                        gmap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                        if (gmap.getMyLocation()!=null)
                            gmap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(gmap.getMyLocation().getLatitude(), gmap.getMyLocation().getLongitude())));
                        // Remove listener to prevent position reset on camera
                        // move.
                        gmap.setOnCameraChangeListener(null);
                    }
                });
            }
        }
    }

    private void initMap() {
        if (Config.USE_FAKE_LOCATION) {
            currentLatitude = Config.FAKE_LAT;
            currentLongitude = Config.FAKE_LON;
            displayPlacesOnMap();
        } else {
            if (!gmap.isMyLocationEnabled()) gmap.setMyLocationEnabled(true);
            if (gmap.getMyLocation() != null) {
                currentLatitude = gmap.getMyLocation().getLatitude();
                currentLongitude = gmap.getMyLocation().getLongitude();
            }

            displayPlacesOnMap();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gmap.setMyLocationEnabled(true);
    }

    @Override
    protected void onPause() {
        gmap.setMyLocationEnabled(false);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (downloadRestaurantListTask != null) downloadRestaurantListTask.cancel(true);
        if (calculateItemsTask != null) calculateItemsTask.cancel(true);
        super.onStop();
    }

    public void onListClick(View view) {
        AppUtils.hideKeyBoard(this);
        Intent intent = new Intent(getApplicationContext(), RestaurantsListScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class DownloadRestaurantListTask extends AsyncTask<String, Void, Void> {

        private ProgressDialog progressDialog;

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog = AppUtils.showProgressDialog(RestaurantsMapScreen.this, R.string.txt_getting_restaurants,
                    false);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            try {
                App.getInstance()
                        .getCache()
                        .setRestaurants(
                                App.getInstance().getNetworkService()
                                        .getRestaurants(arg0[0], arg0[1], arg0[2], RestaurantsMapScreen.this));
            } catch (NetworkException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (App.getInstance().getCache().getRestaurants().isEmpty()) {
                AppUtils.showToast(getApplicationContext(), R.string.txt_no_results);
            } else {
                displayPlacesOnMap();
            }
        }
    };

    private void calculateItems() {
        if (calculateItemsTask != null) {
            calculateItemsTask.cancel(true);
        }
        calculateItemsTask = new CalculateItemsTask();
        calculateItemsTask.execute();
    }

    class CalculateItemsTask extends AsyncTask<Void, Restaurant, Void> {
        VisibleRegion vb;

        CalculateItemsTask() {
            vb = gmap.getProjection().getVisibleRegion();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            findViewById(R.id.progressBar).setVisibility(View.GONE);
        }

        private void myOverlaysClear() {
            for (Restaurant item : restaurants) {
                item.getList().clear();
            }
            // if we zoom in not clear existing pins
            if (oldZoomLevel != gmap.getCameraPosition().zoom) {
                restaurantsVisible.clear();
                gmap.clear();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO: this is not good solution, need to remove in feature
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.LEFT_OF, R.id.btn_menu_list);
            findViewById(R.id.progressBar).setLayoutParams(params);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            myOverlaysClear();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            boolean isImposition;
            for (Restaurant itemFromAll : restaurants) {
                if (vb.latLngBounds.contains(new LatLng(itemFromAll.latitude, itemFromAll.longitude))) {
                    isImposition = false;
                    for (Restaurant item : restaurantsVisible) {
                        if (itemFromAll == item) {
                            isImposition = true;
                            break;
                        }
                        if (isImposition(itemFromAll, item, vb)) {
                            item.addList(itemFromAll);
                            isImposition = true;
                            break;
                        }
                    }
                    if (!isImposition) {
                        // myOverlays is ArraySet and if we zoom in, old pins
                        // not add again
                        restaurantsVisible.addIfAbsent(itemFromAll);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            for (Restaurant rest : restaurantsVisible) {
                MarkerOptions marker = new MarkerOptions().position(new LatLng(rest.latitude, rest.longitude))
                        .title(rest.name).snippet(rest.image);
                gmap.addMarker(marker);
            }
            findViewById(R.id.progressBar).setVisibility(View.GONE);
        };
    };

    private boolean isImposition(Restaurant item1, Restaurant item2, VisibleRegion vb) {
        double latspan = vb.latLngBounds.northeast.latitude - vb.latLngBounds.southwest.latitude;
        double delta = latspan / KOEFF;
        double dx = item1.latitude - item2.latitude;
        double dy = item1.longitude - item2.longitude;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < delta) {
            return true;
        } else {
            return false;
        }
    }

    class MyInfoWindowAdapter implements InfoWindowAdapter {
        boolean skip;

        @Override
        public View getInfoContents(final Marker arg0) {
            View layout = LayoutInflater.from(RestaurantsMapScreen.this)
                    .inflate(R.layout.balloon_restaurant_item, null);
            TextView text = (TextView) layout.findViewById(R.id.balloon_item_title);
            ImageView image = (ImageView) layout.findViewById(R.id.balloon_item_image);
            for (Restaurant rest : restaurants) {
                if (rest.name.equals(arg0.getTitle())
                        && new BigDecimal(rest.latitude).setScale(DOUBLE_ACCURACY, BigDecimal.ROUND_HALF_UP)
                                .doubleValue() == new BigDecimal(arg0.getPosition().latitude).setScale(DOUBLE_ACCURACY,
                                BigDecimal.ROUND_HALF_UP).doubleValue()
                        && new BigDecimal(rest.longitude).setScale(DOUBLE_ACCURACY, BigDecimal.ROUND_HALF_UP)
                                .doubleValue() == new BigDecimal(arg0.getPosition().longitude).setScale(
                                DOUBLE_ACCURACY, BigDecimal.ROUND_HALF_UP).doubleValue() && rest.getList().size() > 0) {
                    text.setText(getString(R.string.click_to_view_restaurants));
                    image.setVisibility(View.GONE);
                    return layout;
                }
            }
            text.setText(arg0.getTitle());
            if (skip) {
                if (imageView.getDrawable() != null)
                    image.setImageDrawable(imageView.getDrawable());
                else
                    skip = false;
            }
            if (arg0.getSnippet() != null && !skip) {
                fetcher.loadImage(arg0.getSnippet(), imageView);
                image.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                skip = true;
                                arg0.showInfoWindow();
                            }
                        });
                    }
                }, 2500);
            }
            if (skip) skip = false;
            return layout;
        }

        @Override
        public View getInfoWindow(Marker arg0) {
            return null;
        }
    }

}
