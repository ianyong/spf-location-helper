package io.github.ianyong.spfdivisionalboundaries;

public class EstablishmentDistance implements Comparable<EstablishmentDistance> {

    private String establishmentKmlId;
    private double distance;

    public EstablishmentDistance(String kmlId, double distance) {
        this.establishmentKmlId = kmlId;
        this.distance = distance;
    }

    public String getEstablishmentKmlId() {
        return establishmentKmlId;
    }

    public double getDistance() {
        return distance;
    }

    public int compareTo(EstablishmentDistance b) {
        if(distance > b.getDistance()) {
            return 1;
        } else if(distance < b.getDistance()) {
            return -1;
        }
        return 0;
    }

}
