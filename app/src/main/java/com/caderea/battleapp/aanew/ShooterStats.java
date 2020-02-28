package com.caderea.battleapp.aanew;

public class ShooterStats implements Fighter {
    int power;
    int precision;
    int fireSpeed;
    int rounds;

    @Override
    public void setStats(BaseStats baseStats) {
        power = baseStats.getStr();
        precision = baseStats.getIntel();
        fireSpeed = baseStats.getDex();
        rounds = baseStats.getStm();
    }
}
