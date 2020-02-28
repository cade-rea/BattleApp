package com.caderea.battleapp.aanew;

public class RacingStats implements Fighter {
    int topSpeed;
    int accel;
    int handling;
    int weight;

    @Override
    public void setStats(BaseStats baseStats) {
        topSpeed = baseStats.getStr();
        accel = baseStats.getIntel();
        handling = baseStats.getDex();
        weight = baseStats.getStm();
    }
}
