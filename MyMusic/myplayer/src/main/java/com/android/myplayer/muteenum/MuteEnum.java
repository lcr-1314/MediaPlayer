package com.android.myplayer.muteenum;

public enum MuteEnum {
    MUTE_RIGHT,
    MUTE_LEFT,
    MUTE_CENTER;

    private String name;
    private int value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
