package paps.bookman.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class Room implements Parcelable {
    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };
    private boolean availability;
    private String floorNumber;
    private String price;
    private String roomNumber;
    private String type;
    private String key;

    public Room() {
    }

    public Room(boolean availability, String floorNumber, String price, String roomNumber, String type, String key) {
        this.availability = availability;
        this.floorNumber = floorNumber;
        this.price = price;
        this.roomNumber = roomNumber;
        this.type = type;
        this.key = key;
    }

    protected Room(Parcel in) {
        availability = in.readByte() != 0;
        floorNumber = in.readString();
        price = in.readString();
        roomNumber = in.readString();
        type = in.readString();
        key = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (availability ? 1 : 0));
        dest.writeString(floorNumber);
        dest.writeString(price);
        dest.writeString(roomNumber);
        dest.writeString(type);
        dest.writeString(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public String getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(String floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
