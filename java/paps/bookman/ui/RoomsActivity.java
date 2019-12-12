package paps.bookman.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import paps.bookman.R;
import paps.bookman.data.Hotel;
import paps.bookman.data.Pictures;
import paps.bookman.data.Reservation;
import paps.bookman.data.Room;
import paps.bookman.util.ConstantUtil;
import paps.bookman.util.HotelUserPrefs;

public class RoomsActivity extends AppCompatActivity {
    public static final String EXTRA_PICTURE = "EXTRA_PICTURE";
    public static final String EXTRA_HOTEL = "EXTRA_HOTEL";

    @BindView(R.id.container)
    ViewGroup container;
    @BindView(R.id.content_container)
    ViewGroup content;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.no_rooms)
    ImageView noRooms;

    private MaterialDialog loading;
    private HotelUserPrefs prefs;
    private boolean hasFoundRoom = false;

    private Pictures pictures;
    private Hotel hotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        prefs = new HotelUserPrefs(this);
        loading = ConstantUtil.getDialog(this);

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_PICTURE)) {
            pictures = intent.getParcelableExtra(EXTRA_PICTURE);
            hotel = intent.getParcelableExtra(EXTRA_HOTEL);
            loading.show();
            loadRooms();
            checkEmptyState();
        }

    }

    private void loadRooms() {
        List<Room> rooms = new ArrayList<>(0);
        prefs.db.getReference().child(ConstantUtil.HOTEL_REF).child(hotel.getKey()).child("Rooms")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            try {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    if (snapshot.exists()) {
                                        Room room = snapshot.getValue(Room.class);
                                        if (room != null
                                                && room.isAvailability()
                                                && room.getType().equalsIgnoreCase(pictures.getName())) {
                                            rooms.add(room);
                                        }
                                    }
                                }

                                if (rooms.isEmpty()) {
                                    hasFoundRoom = false;
                                    checkEmptyState();
                                } else {
                                    setAvailableRoom(rooms.get(0));
                                    hasFoundRoom = true;
                                    checkEmptyState();
                                }

                            } catch (Exception e) {
                                showMessage(e.getMessage());
                            }
                        } else {
                            hasFoundRoom = false;
                            checkEmptyState();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        showMessage(databaseError.getMessage());
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void setAvailableRoom(Room room) {
        ImageView imageRoom = findViewById(R.id.room_image);
        TextView roomNumber = findViewById(R.id.room_number);
        TextView floorNumber = findViewById(R.id.floor_number);

        roomNumber.setText("Room " + room.getRoomNumber());
        floorNumber.setText("Floor " + room.getFloorNumber());

        if (room.getType().equalsIgnoreCase("Single")) {
            imageRoom.setImageResource(R.drawable.single);
        } else if (room.getType().equalsIgnoreCase("Double")) {
            imageRoom.setImageResource(R.drawable.double_room);
        } else {
            imageRoom.setImageResource(R.drawable.economy);
        }


        findViewById(R.id.check_in).setOnClickListener(view -> {
            loading.show();
            new Handler().postDelayed(() -> {
                DatabaseReference reference = prefs.db.getReference().child(ConstantUtil.RESERVATION_REF).child(prefs.getUid()).push();
                Reservation reservation = new Reservation(prefs.getUser(), hotel, room, reference.getKey());

                reference.setValue(reservation).addOnCompleteListener(RoomsActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            prefs.db.getReference().child(ConstantUtil.HOTEL_REF).child(hotel.getKey()).child("Rooms")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot != null && dataSnapshot.exists()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    if (snapshot.exists()) {
                                                        if (Objects.equals(room.getKey(), snapshot.getKey())) {
                                                            updateRoom(snapshot.getKey());
                                                        }
                                                    }
                                                }

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            showMessage(databaseError.getMessage());
                                        }
                                    });
                        } else {
                            showMessage(task.getException().getMessage());
                        }
                    }
                }).addOnFailureListener(RoomsActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage(e.getMessage());
                    }
                });

            }, 1500);
        });
    }

    private void updateRoom(String key) {
        Map<String, Object> map = new HashMap<>(0);
        map.put("availability", false);
        DatabaseReference reference = prefs.db.getReference().child(ConstantUtil.HOTEL_REF).child(hotel.getKey()).child("Rooms")
                .child(key);
        reference.updateChildren(map)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RoomsActivity.this, "Room reserved successfully", Toast.LENGTH_SHORT).show();
                            finishAfterTransition();
                        } else showMessage(task.getException().getMessage());
                    }
                }).addOnFailureListener(this, e -> showMessage(e.getMessage()));
    }


    private void checkEmptyState() {
        if (loading.isShowing()) loading.dismiss();
        if (hasFoundRoom) {
            TransitionManager.beginDelayedTransition(container);
            noRooms.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
        } else {
            TransitionManager.beginDelayedTransition(container);
            noRooms.setVisibility(View.VISIBLE);
            content.setVisibility(View.GONE);
        }
    }

    private void showMessage(CharSequence message) {
        if (loading.isShowing()) loading.dismiss();
        if (message != null) {
            Snackbar.make(container, message, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

}
