package paps.bookman.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import paps.bookman.R;
import paps.bookman.data.Hotel;
import paps.bookman.util.ConstantUtil;
import paps.bookman.util.HotelUserPrefs;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION = 5;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private MaterialDialog loading;
    private HotelUserPrefs prefs;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        setActionBar(toolbar);

        prefs = new HotelUserPrefs(this);
        loading = ConstantUtil.getDialog(this);
        loading.show();

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (prefs.isLoggedIn()) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng userLocation = new LatLng(prefs.getLat(), prefs.getLng());
        mMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .snippet(prefs.getAddress())
                .title(prefs.getUsername())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16.0f));

        prefs.db.getReference().child(ConstantUtil.HOTEL_REF).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot != null && dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.exists()) {
                                loading.dismiss();
                                Hotel hotel = snapshot.getValue(Hotel.class);
                                plotHotelMarker(hotel);
                            }
                        }

                    }
                } catch (Exception e) {
                    Toast.makeText(MapsActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
            try {
                Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                        .build(MapsActivity.this);
                startActivityForResult(intent, REQUEST_LOCATION);
            } catch (Exception e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    private void plotHotelMarker(Hotel hotel) {
        LatLng latLng = new LatLng(hotel.getLatitude(), hotel.getLongitude());
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions()
                    .title(hotel.getHotelName())
                    .position(latLng)
                    .snippet(hotel.getHotelLocation())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_LOCATION) {
                if (data != null) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    if (mMap != null) {
                        LatLng latLng = place.getLatLng();
                        Toast.makeText(this, "Navigating to " + place.getAddress(), Toast.LENGTH_SHORT).show();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f));
                    }
                }
            }
        }
    }
}
