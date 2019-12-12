package paps.bookman.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import paps.bookman.R;
import paps.bookman.data.Hotel;
import paps.bookman.data.User;
import paps.bookman.util.ConstantUtil;
import paps.bookman.util.GlideApp;
import paps.bookman.util.HotelAdapter;
import paps.bookman.util.HotelUserPrefs;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "HomeActivity";
    private static final int REQUEST_PROFILE = 3;

    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.swipe_home)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.home_grid)
    RecyclerView grid;
    @BindView(android.R.id.empty)
    ProgressBar loading;
    private HotelUserPrefs prefs;
    private View headerView;
    private List<Hotel> hotels;
    private HotelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        prefs = new HotelUserPrefs(this);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);
        setupProfile();

        hotels = new ArrayList<>(0);
        adapter = new HotelAdapter(this);
        grid.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        grid.setLayoutManager(layoutManager);
        grid.setItemAnimator(new DefaultItemAnimator());
        grid.setHasFixedSize(true);
        grid.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        loadData(false);
        checkEmptyState();

        refreshLayout.setOnRefreshListener(() -> {
            if (!hotels.isEmpty()) hotels.clear();
            loadData(false);
        });

    }

    private void checkEmptyState() {
        if (adapter.getItemCount() > 0) {
            TransitionManager.beginDelayedTransition(drawer);
            refreshLayout.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
        } else {
            TransitionManager.beginDelayedTransition(drawer);
            refreshLayout.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }
    }

    private void loadData(boolean isPopular) {
        prefs.db.getReference().child(ConstantUtil.HOTEL_REF).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.exists()) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        Hotel hotel = snap.getValue(Hotel.class);
                        if (isPopular) {
                            if (hotel != null && (Integer.parseInt(hotel.getHotelRating()) > 3)) {
                                hotels.add(hotel);
                            }
                        } else {
                            hotels.add(hotel);
                        }
                    }

                    if (refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
                    adapter.addHotels(hotels);
                    checkEmptyState();
                } else {
                    setError("Hotels cannot be retrieved at this moment");
                    checkEmptyState();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                setError(databaseError.getMessage());
            }
        });
    }

    private void setupProfile() {
        ImageView cover = headerView.findViewById(R.id.header_cover);
        TextView username = headerView.findViewById(R.id.header_username);
        TextView email = headerView.findViewById(R.id.header_email);
        ImageView profile = headerView.findViewById(R.id.header_picture);

        User user = prefs.getUser();
        username.setText(user.getUsername());
        email.setText(user.getEmail());

        //Lighten the text if the user has a profile image
        if (prefs.getPicture() != null && !TextUtils.isEmpty(prefs.getPicture())) {
            username.setTextColor(getResources().getColor(R.color.text_primary_light));
            email.setTextColor(getResources().getColor(R.color.text_secondary_light));
        }

        GlideApp.with(this)
                .asBitmap()
                .load(user.getPicture())
                .placeholder(R.color.content_placeholder)
                .error(R.color.content_placeholder)
                .fallback(R.color.content_placeholder)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .transition(withCrossFade())
                .into(cover);

        GlideApp.with(this)
                .asBitmap()
                .load(user.getPicture())
                .placeholder(R.drawable.avatar_placeholder)
                .placeholder(R.drawable.avatar_placeholder)
                .placeholder(R.drawable.avatar_placeholder)
                .override(Target.SIZE_ORIGINAL)
                .circleCrop()
                .transition(withCrossFade())
                .into(profile);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.hotels) {
            if (!hotels.isEmpty()) hotels.clear();
            loadData(false);
        } else if (id == R.id.popular) {
            if (!hotels.isEmpty()) hotels.clear();
            loadData(true);
        } else if (id == R.id.on_map) {
            startActivity(new Intent(HomeActivity.this, MapsActivity.class));
        } else if (id == R.id.logout) {
            new MaterialDialog.Builder(this)
                    .content("Are you sure you want to logout?")
                    .positiveText("Yes")
                    .negativeText("Dismiss")
                    .onPositive((dialog, which) -> {
                        prefs.logOut();
                        startActivity(new Intent(HomeActivity.this, AuthActivity.class));
                        finish();
                    })
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .build().show();
        } else if (id == R.id.help) {

        } else if (id == R.id.profile) {
            startActivityForResult(new Intent(this, ProfileActivity.class), REQUEST_PROFILE);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PROFILE) {
            if (resultCode == ProfileActivity.RESULT_UPDATING) {
                setupProfile();
            }
        }
    }

    private void setError(CharSequence message) {
        Snackbar.make(drawer, message, Snackbar.LENGTH_LONG).show();
    }
}
