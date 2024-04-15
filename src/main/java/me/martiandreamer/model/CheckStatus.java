package me.martiandreamer.model;

public record CheckStatus(String previousAction) {
    public static String CHECKIN = "CHECKIN";
}
