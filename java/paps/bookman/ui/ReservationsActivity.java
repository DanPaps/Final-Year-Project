package paps.bookman.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import paps.bookman.R;
import paps.bookman.data.Hotel;
import paps.bookman.data.Pictures;
import paps.bookman.util.ConstantUtil;
import paps.bookman.util.GlideApp;
import paps.bookman.util.HotelUserPrefs;

public class ReservationsActivity extends AppCompatActivity {
    public static final String EXTRA_HOTEL = "EXTRA_HOTEL";
    @BindView(R.id.container)
    ViewGroup container;
    @BindView(R.id.grid)
    RecyclerView grid;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private MaterialDialog loading;
    private HotelUserPrefs prefs;

    private Hotel hotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        prefs = new HotelUserPrefs(this);
        loading = ConstantUtil.getDialog(this);

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_HOTEL)) {
            hotel = intent.getParcelableExtra(EXTRA_HOTEL);
            loading.show();
            loadHotelData();
        }

    }

    private void loadHotelData() {
        List<Pictures> pictures = new ArrayList<>(0);
        DatabaseReference reference = prefs.db.getReference().child(ConstantUtil.HOTEL_REF).child(hotel.getKey());
        reference.child("RoomPictures").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.exists()) {
                    if (loading.isShowing()) loading.dismiss();
                    try {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Pictures picture = snapshot.getValue(Pictures.class);
                            pictures.add(picture);
                        }
                    } catch (Exception e) {
                        showMessage(e.getMessage());
                    }

                    PicturesAdapter adapter = new PicturesAdapter(pictures);
                    grid.setAdapter(adapter);
                    grid.setHasFixedSize(true);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(ReservationsActivity.this);
                    grid.setLayoutManager(layoutManager);
                    grid.setItemAnimator(new DefaultItemAnimator());
                    grid.addItemDecoration(new DividerItemDecoration(ReservationsActivity.this, layoutManager.getOrientation()));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showMessage(databaseError.getMessage());
            }
        });
    }

    private void showMessage(CharSequence message) {
        Snackbar.make(container, message, Snackbar.LENGTH_LONG).show();
    }

    class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.PicturesViewHolder> {
        private List<Pictures> pictures;

        public PicturesAdapter(List<Pictures> pictures) {
            this.pictures = pictures;
        }

        @NonNull
        @Override
        public PicturesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PicturesViewHolder(getLayoutInflater().inflate(R.layout.item_picture, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PicturesViewHolder holder, int position) {
            Pictures picture = this.pictures.get(position);
            GlideApp.with(getApplicationContext())
                    .load(picture.getImageUrl())
                    .placeholder(R.color.content_placeholder)
                    .fallback(R.color.content_placeholder)
                    .error(R.color.content_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .override(Target.SIZE_ORIGINAL)
                    .into(holder.image);

            holder.title.setText(picture.getName());
            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(ReservationsActivity.this, RoomsActivity.class);
                intent.putExtra(RoomsActivity.EXTRA_PICTURE, picture);
                intent.putExtra(RoomsActivity.EXTRA_HOTEL, hotel);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return pictures.size();
        }

        class PicturesViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.room_type_title)
            TextView title;
            @BindView(R.id.room_type_image)
            ImageView image;

            public PicturesViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
