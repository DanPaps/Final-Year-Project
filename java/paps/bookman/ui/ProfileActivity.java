package paps.bookman.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import paps.bookman.R;
import paps.bookman.data.Reservation;
import paps.bookman.data.User;
import paps.bookman.util.ConstantUtil;
import paps.bookman.util.GlideApp;
import paps.bookman.util.HotelUserPrefs;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ProfileActivity extends AppCompatActivity {
    public static final int IMAGE_REQUEST_CODE = 9;
    public static final int RESULT_UPDATING = 2;

    @BindView(R.id.profile_image)
    ImageView image;
    @BindView(R.id.profile_username)
    TextView username;
    @BindView(R.id.profile_email)
    TextView email;
    @BindView(R.id.profile_phone)
    TextView phone;
    @BindView(R.id.container)
    ViewGroup container;
    @BindView(R.id.profile_back)
    ImageButton back;
    @BindView(R.id.profile_edit)
    ImageButton edit;
    @BindView(R.id.loading)
    ProgressBar progress;
    @BindView(R.id.reservations_grid)
    RecyclerView grid;
    @BindView(R.id.empty_reservations)
    TextView empty;


    private MaterialDialog loading;
    private HotelUserPrefs prefs;
    private Uri resultUri = null;
    private boolean hasChanges = false;
    private boolean hasReservations = false;
    private ReservationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        prefs = new HotelUserPrefs(this);
        loading = ConstantUtil.getDialog(this);

        if (prefs.isLoggedIn()) {
            setupHeader();
        }

    }

    private void setupHeader() {
        User user = prefs.getUser();
        GlideApp.with(getApplicationContext())
                .load(user.getPicture())
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .fallback(R.drawable.avatar_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .override(Target.SIZE_ORIGINAL)
                .circleCrop()
                .transition(withCrossFade())
                .into(image);

        username.setText(user.getUsername());
        email.setText(user.getEmail());
        phone.setText(user.getPhone());

        loadReservations();
    }

    private void loadReservations() {
        List<Reservation> reservations = new ArrayList<>(0);
        prefs.db.getReference().child(ConstantUtil.RESERVATION_REF).child(prefs.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            hasReservations = dataSnapshot.getChildrenCount() > 0;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                reservations.add(snapshot.getValue(Reservation.class));
                            }

                            adapter = new ReservationAdapter(reservations);
                            grid.setAdapter(adapter);
                            grid.setHasFixedSize(true);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(ProfileActivity.this);
                            grid.setLayoutManager(layoutManager);
                            grid.setItemAnimator(new DefaultItemAnimator());
                            grid.addItemDecoration(new DividerItemDecoration(ProfileActivity.this, layoutManager.getOrientation()));
                            checkEmptyState();

                        } else {
                            hasReservations = false;
                            checkEmptyState();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        showMessage(databaseError.getMessage());
                    }
                });
    }

    @OnClick(R.id.profile_back)
    void dismiss() {
        onBackPressed();
    }

    @OnClick(R.id.profile_phone)
    void editPhone() {
        showEditDialog(1);
    }

    @OnClick(R.id.profile_edit)
    void editProfile() {
        new MaterialDialog.Builder(this)
                .title("Choose option to edit...")
                .theme(Theme.LIGHT)
                .negativeText("Dismiss")
                .items("Username", "Phone Number", "Profile Image")
                .onNegative((dialog, which) -> dialog.dismiss())
                .itemsCallback((dialog, itemView, position, text) -> {
                    if (position == 0 || position == 1) showEditDialog(position);
                    else image.performClick();
                })
                .build().show();
    }

    private void showEditDialog(int i) {
        View v = getLayoutInflater().inflate(R.layout.edit_profile, null, false);
        EditText content = v.findViewById(R.id.edt_profile);

        switch (i) {
            case 0:
                content.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                break;
            default:
                content.setInputType(InputType.TYPE_CLASS_PHONE);
        }

        new MaterialDialog.Builder(this)
                .title("Update profile information")
                .theme(Theme.LIGHT)
                .negativeText("Dismiss")
                .positiveText("Save")
                .customView(v, true)
                .onNegative((dialog, which) -> dialog.dismiss())
                .onPositive((dialog, which) -> {
                    dialog.dismiss();
                    if (i == 0) username.setText(content.getText().toString());
                    else phone.setText(content.getText().toString());
                    hasChanges = true;
                })
                .build().show();

    }

    @OnClick(R.id.profile_image)
    void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Pick profile image"), IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_REQUEST_CODE) {
                if (data != null) {
                    resultUri = data.getData();
                    GlideApp.with(getApplicationContext())
                            .load(data.getData())
                            .override(Target.SIZE_ORIGINAL)
                            .circleCrop()
                            .into(image);

                    uploadImage();
                } else {
                    showMessage("Profile image was not selected");
                }
            }
        }
    }

    private void uploadImage() {
        loading.show();
        prefs.storage.getReference().child(ConstantUtil.USER_REF)
                .child(prefs.getUid() + ".jpg")
                .putFile(resultUri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        StorageTask<UploadTask.TaskSnapshot> task = taskSnapshot.getTask();
                        if (task.isSuccessful()) {
                            resultUri = taskSnapshot.getDownloadUrl();
                            hasChanges = true;
                            loading.dismiss();
                            showMessage("Profile image uploaded successfully");
                        } else {
                            showMessage(task.getException().getMessage());
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage(e.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (hasChanges) pushUpdate();
        else super.onBackPressed();
    }

    private void pushUpdate() {
        showMessage("Updating your profile information");
        loading.show();

        Map<String, Object> map = new HashMap<>(0);
        map.put("username", username.getText().toString());
        map.put("phone", phone.getText().toString());
        if (resultUri != null) {
            map.put("picture", resultUri.toString());

            //Save offline
            prefs.setPicture(resultUri.toString());
        }

        //Save offline
        prefs.setUsername(username.getText().toString());
        prefs.setPhone(phone.getText().toString());


        prefs.db.getReference().child(ConstantUtil.USER_REF).child(prefs.getUid())
                .updateChildren(map)
                .addOnFailureListener(this, e -> showMessage(e.getMessage()))
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            loading.dismiss();
                            hasChanges = false;
                            Toast.makeText(ProfileActivity.this, "Update was successful", Toast.LENGTH_SHORT).show();
                            setResultAndFinish();
                        } else {
                            showMessage(task.getException().getMessage());
                        }
                    }
                });
    }

    private void setResultAndFinish() {
        setResult(RESULT_UPDATING);
        finishAfterTransition();
    }

    private void showMessage(CharSequence message) {
        if (loading.isShowing()) loading.dismiss();
        Snackbar.make(container, message, Snackbar.LENGTH_LONG).show();
    }

    private void checkEmptyState() {
        if (adapter != null && adapter.getItemCount() > 0) {
            if (hasReservations) {
                TransitionManager.beginDelayedTransition(container);
                progress.setVisibility(View.GONE);
                empty.setVisibility(View.GONE);
                grid.setVisibility(View.VISIBLE);
            } else {
                TransitionManager.beginDelayedTransition(container);
                progress.setVisibility(View.GONE);
                empty.setVisibility(View.VISIBLE);
                grid.setVisibility(View.GONE);
            }
        } else {
            TransitionManager.beginDelayedTransition(container);
            progress.setVisibility(View.VISIBLE);
            empty.setVisibility(View.VISIBLE);
            grid.setVisibility(View.GONE);
        }
    }

    class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationsHolder> {

        private List<Reservation> reservations;

        public ReservationAdapter(List<Reservation> reservations) {
            this.reservations = reservations;
        }

        @NonNull
        @Override
        public ReservationsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ReservationsHolder(getLayoutInflater().inflate(R.layout.item_reservation, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ReservationsHolder holder, int position) {
            Reservation reservation = reservations.get(position);

            GlideApp.with(getApplicationContext())
                    .load(reservation.getHotel().getHotelPic())
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .circleCrop()
                    .override(Target.SIZE_ORIGINAL)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .transition(withCrossFade())
                    .fallback(R.drawable.avatar_placeholder)
                    .into(holder.image);

            holder.floor.setText(String.format("Floor #: %s", reservation.getRoom().getFloorNumber()));
            holder.room.setText(String.format("Room #: %s", reservation.getRoom().getRoomNumber()));
            holder.key.setText(String.format("Reservation Key: %s", reservation.getKey()));
            holder.name.setText(reservation.getHotel().getHotelName());

            holder.cancel.setOnClickListener(view -> {
                int pos = position;
                Snackbar snackbar = Snackbar.make(view, "Do you want to cancel reservation?", Snackbar.LENGTH_LONG);
                snackbar.setAction("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        doDeletion(reservation,pos);
                    }
                }).show();
            });
        }

        private void doDeletion(Reservation reservation, int pos) {
            loading.show();
            prefs.db.getReference().child(ConstantUtil.RESERVATION_REF).child(prefs.getUid())
                    .child(reservation.getKey()).getRef().removeValue().addOnCompleteListener(ProfileActivity.this, task -> {
                        if (task.isSuccessful()) {
                            Map<String,Object> map = new HashMap<>(0);
                            map.put("availability",true);
                            prefs.db.getReference().child(ConstantUtil.HOTEL_REF).child(reservation.getHotel().getKey())
                                    .child("Rooms").child(reservation.getRoom().getKey())
                                    .updateChildren(map)
                                    .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                loading.dismiss();
                                                Toast.makeText(getApplicationContext(), "Reservation successfully cancelled", Toast.LENGTH_SHORT).show();
                                                finishAfterTransition();
                                            }
                                        }
                                    });
                        }
            });
        }

        @Override
        public int getItemCount() {
            return reservations.size();
        }

        class ReservationsHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.res_image)
            ImageView image;
            @BindView(R.id.res_floor)
            TextView floor;
            @BindView(R.id.res_key)
            TextView key;
            @BindView(R.id.res_room)
            TextView room;
            @BindView(R.id.res_name)
            TextView name;
            @BindView(R.id.res_cancel)
            Button cancel;

            public ReservationsHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
