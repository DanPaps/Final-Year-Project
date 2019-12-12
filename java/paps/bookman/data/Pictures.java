package paps.bookman.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class Pictures implements Parcelable {

    public static final Creator<Pictures> CREATOR = new Creator<Pictures>() {
        @Override
        public Pictures createFromParcel(Parcel in) {
            return new Pictures(in);
        }

        @Override
        public Pictures[] newArray(int size) {
            return new Pictures[size];
        }
    };
    private String imageUrl;
    private String name;

    public Pictures() {
    }

    public Pictures(String imageUrl, String name) {
        this.imageUrl = imageUrl;
        this.name = name;
    }

    protected Pictures(Parcel in) {
        imageUrl = in.readString();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(name);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Pictures{" +
                "imageUrl='" + imageUrl + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
