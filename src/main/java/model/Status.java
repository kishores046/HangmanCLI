package model;

public enum Status {

    WIN("WIN"),
    LOSE("LOSE"),
    DRAW("DRAW"),
    NOTHING("NOTHING");

    private final String value;

    public String getValue() {
        return value;
    }

    Status(String value) {
        this.value=value;
    }
}
