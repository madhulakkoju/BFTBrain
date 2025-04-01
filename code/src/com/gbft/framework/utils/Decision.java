package com.gbft.framework.utils;

public class Decision {
    private final String nextProtocol;
    private final String nextArchitecture;

    public Decision(String nextProtocol, String nextArchitecture) {
        this.nextProtocol = nextProtocol;
        this.nextArchitecture = nextArchitecture;
    }

    public String getNextProtocol() {
        return nextProtocol;
    }

    public String getNextArchitecture() {
        return nextArchitecture;
    }

    @Override
    public String toString() {
        return "Decision{" +
                "nextProtocol='" + nextProtocol + '\'' +
                ", nextArchitecture='" + nextArchitecture + '\'' +
                '}';
    }
}
