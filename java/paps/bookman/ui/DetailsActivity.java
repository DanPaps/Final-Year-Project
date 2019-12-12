package paps.bookman.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import paps.bookman.R;
import paps.bookman.data.Facility;
import paps.bookman.data.Hotel;
import paps.bookman.data.HotelImage;
import paps.bookman.util.ConstantUtil;
import paps.bookman.util.GlideApp;
import paps.bookman.util.HotelUserPrefs;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_HOTEL = "EXTRA_HOTEL";
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    ImageButton fab;
    @BindView(R.id.details_image)
    ImageView image;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.details_hotel)
    TextView details;
    @BindView(R.id.details_assets)
    TextView assets;
    @BindView(R.id.details_rating)
    RatingBar ratingBar;
    @BindView(R.id.container)
    ViewGroup container;
    @BindView(R.id.details_pager)
    RecyclerView grid;


    private HotelUserPrefs prefs;
    private GoogleMap map;
    private Hotel hotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        prefs = new HotelUserPrefs(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.details_map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_HOTEL)) {
            hotel = intent.getParcelableExtra(EXTRA_HOTEL);
            setupHotel();
        }

        getHotelImages();
    }

    private void getHotelImages() {
        List<HotelImage> images = new ArrayList<>(0);
        prefs.db.getReference().child(ConstantUtil.HOTEL_REF).child(hotel.getKey()).child("Images")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot != null && dataSnapshot.exists()) {
                                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                for (DataSnapshot data : children) {
                                    if (data.exists()) {
                                        HotelImage hotelImage = data.getValue(HotelImage.class);
                                        if (hotelImage != null) {
                                            images.add(hotelImage);
                                        }
                                    }
                                }

                                //Attach adapter
                                ImageAdapter pagerAdapter = new ImageAdapter(images);
                                grid.setAdapter(pagerAdapter);
                                grid.setLayoutManager(new LinearLayoutManager(DetailsActivity.this, LinearLayoutManager.HORIZONTAL, false));
                                grid.setHasFixedSize(true);
                                grid.setItemAnimator(new DefaultItemAnimator());

                            } else {
                                Snackbar.make(container, "Unable to retrieve images for " + hotel.getHotelName(), Snackbar.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Snackbar.make(container, e.getLocalizedMessage(), Snackbar.LENGTH_INDEFINITE)
                                    .show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Snackbar.make(container, databaseError.getMessage(), Snackbar.LENGTH_LONG).show();

                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void setupHotel() {
        //Hotel picture
        GlideApp.with(this)
                .load(hotel.getHotelPic())
                .override(Target.SIZE_ORIGINAL)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .transition(withCrossFade())
                .error(R.color.content_placeholder)
                .fallback(R.color.content_placeholder)
                .placeholder(R.color.content_placeholder)
                .into(image);

        toolbarLayout.setTitle(hotel.getHotelName());
        details.setText(hotel.getHotelDetails());
        ratingBar.setRating(Float.parseFloat(hotel.getHotelRating()));
        getHotelAssets();
    }

    private void getHotelAssets() {
        prefs.db.getReference().child(ConstantUtil.HOTEL_REF).child(hotel.getKey())
                .child("Assets").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.exists()) {
                    try {
                        Facility facility = dataSnapshot.getValue(Facility.class);
                        if (facility != null) {
                            String val = "";
                            for (int i = 0; i < facility.getFacilities().size(); i++) {
                                val += facility.getFacilities().get(i) + ", ";
                            }

                            assets.setText(val);

                        } else {
                            Toast.makeText(DetailsActivity.this, "No facility found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(DetailsActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DetailsActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @OnClick(R.id.fab)
    void addReservation() {
        if (prefs.isLoggedIn()) {
            Intent intent = new Intent(this, ReservationsActivity.class);
            intent.putExtra(ReservationsActivity.EXTRA_HOTEL, hotel);
            startActivity(intent);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        if (hotel != null) {
            LatLng hotelPosition = new LatLng(hotel.getLatitude(), hotel.getLongitude());
            MarkerOptions options = new MarkerOptions().position(hotelPosition).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                    .title(hotel.getHotelName()).snippet(hotel.getHotelLocation());
            map.addMarker(options);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(hotelPosition, 16.0f));

        }
    }

    class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

        private List<HotelImage> images;

        public ImageAdapter(List<HotelImage> images) {
            this.images = images;
        }


        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ImageViewHolder(getLayoutInflater().inflate(R.layout.image_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            HotelImage hotelImage = images.get(position);
            GlideApp.with(getApplicationContext())
                    .load(hotelImage.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.color.content_placeholder)
                    .placeholder(R.color.content_placeholder)
                    .placeholder(R.color.content_placeholder)
                    .override(Target.SIZE_ORIGINAL)
                    .transition(withCrossFade())
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.image_view)
            ImageView imageView;

            public ImageViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
