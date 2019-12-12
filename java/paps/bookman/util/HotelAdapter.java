package paps.bookman.util;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import paps.bookman.R;
import paps.bookman.data.Hotel;
import paps.bookman.ui.DetailsActivity;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {
    private Activity context;
    private List<Hotel> hotels;

    public HotelAdapter(Activity context) {
        this.context = context;
        this.hotels = new ArrayList<>(0);
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HotelViewHolder(LayoutInflater.from(context).inflate(R.layout.item_hotel, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotels.get(position);

        GlideApp.with(context)
                .asBitmap()
                .load(hotel.getHotelPic())
                .error(R.color.content_placeholder)
                .fallback(R.color.content_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.color.content_placeholder)
                .override(Target.SIZE_ORIGINAL)
                .transition(withCrossFade())
                .into(holder.image);

        holder.location.setText(hotel.getHotelLocation());
        holder.name.setText(hotel.getHotelName());
        holder.rating.setRating(Float.parseFloat(hotel.getHotelRating()));
        holder.navDetails.setOnClickListener(view -> holder.itemView.performClick());

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra(DetailsActivity.EXTRA_HOTEL, hotel);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    public void addHotels(List<Hotel> newHotels) {
        hotels.clear();
        if (!newHotels.isEmpty()) {
            for (Hotel item : newHotels) {
                hotels.add(item);
                notifyItemRangeChanged(0, newHotels.size());
            }
        }

    }

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_hotel_name)
        TextView name;
        @BindView(R.id.item_hotel_rating)
        RatingBar rating;
        @BindView(R.id.item_hotel_location)
        TextView location;
        @BindView(R.id.item_hotel_image)
        ImageView image;
        @BindView(R.id.item_hotel_view)
        Button navDetails;

        public HotelViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
