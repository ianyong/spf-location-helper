package io.github.ianyong.spflocationhelper;

public class EstablishmentDistance implements Comparable<EstablishmentDistance> {

    private String establishmentKmlId;
    private String name;
    private double distance;

    public EstablishmentDistance(String kmlId, String name, double distance) {
        establishmentKmlId = kmlId;
        this.name = name;
        this.distance = distance;
    }

    public String getEstablishmentKmlId() {
        return establishmentKmlId;
    }

    public String getName() {
        return name;
    }

    public double getDistance() {
        return distance;
    }

    public int compareTo(EstablishmentDistance b) {
        return Double.compare(distance, b.getDistance());
    }

}
