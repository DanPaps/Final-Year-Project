package paps.bookman.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class HotelImage implements Parcelable {
    public static final Creator<HotelImage> CREATOR = new Creator<HotelImage>() {
        @Override
        public HotelImage createFromParcel(Parcel in) {
            return new HotelImage(in);
        }

        @Override
        public HotelImage[] newArray(int size) {
            return new HotelImage[size];
        }
    };
    private String imageUrl;

    public HotelImage() {
    }

    public HotelImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    protected HotelImage(Parcel in) {
        imageUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
