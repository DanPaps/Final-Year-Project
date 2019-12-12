package paps.bookman.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class Reservation implements Parcelable {
    public static final Creator<Reservation> CREATOR = new Creator<Reservation>() {
        @Override
        public Reservation createFromParcel(Parcel in) {
            return new Reservation(in);
        }

        @Override
        public Reservation[] newArray(int size) {
            return new Reservation[size];
        }
    };
    private User user;
    private Hotel hotel;
    private Room room;
    private String key;

    public Reservation() {
    }

    public Reservation(User user, Hotel hotel, Room room, String key) {
        this.user = user;
        this.hotel = hotel;
        this.room = room;
        this.key = key;
    }

    protected Reservation(Parcel in) {
        user = in.readParcelable(User.class.getClassLoader());
        hotel = in.readParcelable(Hotel.class.getClassLoader());
        room = in.readParcelable(Room.class.getClassLoader());
        key = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
        dest.writeParcelable(hotel, flags);
        dest.writeParcelable(room, flags);
        dest.writeString(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
