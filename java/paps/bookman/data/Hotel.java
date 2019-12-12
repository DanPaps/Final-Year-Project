package paps.bookman.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class Hotel implements Parcelable {
    public static final Creator<Hotel> CREATOR = new Creator<Hotel>() {
        @Override
        public Hotel createFromParcel(Parcel in) {
            return new Hotel(in);
        }

        @Override
        public Hotel[] newArray(int size) {
            return new Hotel[size];
        }
    };
    private String hotelName;
    private String hotelEmail;
    private String hotelLocation;
    private String hotelPic;
    private String hotelDetails;
    private String hotelRating;
    private String hotelWallet;
    private String key;
    private double latitude;
    private double longitude;

    public Hotel() {
    }

    protected Hotel(Parcel in) {
        hotelName = in.readString();
        hotelEmail = in.readString();
        hotelLocation = in.readString();
        hotelPic = in.readString();
        hotelDetails = in.readString();
        hotelRating = in.readString();
        hotelWallet = in.readString();
        key = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hotelName);
        dest.writeString(hotelEmail);
        dest.writeString(hotelLocation);
        dest.writeString(hotelPic);
        dest.writeString(hotelDetails);
        dest.writeString(hotelRating);
        dest.writeString(hotelWallet);
        dest.writeString(key);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelEmail() {
        return hotelEmail;
    }

    public void setHotelEmail(String hotelEmail) {
        this.hotelEmail = hotelEmail;
    }

    public String getHotelLocation() {
        return hotelLocation;
    }

    public void setHotelLocation(String hotelLocation) {
        this.hotelLocation = hotelLocation;
    }

    public String getHotelPic() {
        return hotelPic;
    }

    public void setHotelPic(String hotelPic) {
        this.hotelPic = hotelPic;
    }

    public String getHotelDetails() {
        return hotelDetails;
    }

    public void setHotelDetails(String hotelDetails) {
        this.hotelDetails = hotelDetails;
    }

    public String getHotelRating() {
        return hotelRating;
    }

    public void setHotelRating(String hotelRating) {
        this.hotelRating = hotelRating;
    }

    public String getHotelWallet() {
        return hotelWallet;
    }

    public void setHotelWallet(String hotelWallet) {
        this.hotelWallet = hotelWallet;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
