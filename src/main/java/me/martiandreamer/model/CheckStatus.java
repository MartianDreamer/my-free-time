package me.martiandreamer.model;

public record CheckStatus(String previousAction,
                          String employeeName,
                          String day,
                          int intime,
                          int outtime,
                          int workedTime,
                          int morningTime,
                          int afternoonTime,
                          int lastStartBreak,
                          int lastEndBreak,
                          String informMessages,
                          int type
) {
    public static String CHECKIN = "CHECKIN";
}
