package paps.bookman.data;

import java.util.List;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class Facility {
    private List<String> facilities;

    public Facility() {
    }

    public Facility(List<String> facilities) {
        this.facilities = facilities;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
    }
}
