package com.caderea.battleapp.aanew;

public class FightingStats implements Fighter {
    int str;
    int spd;
    int def;

    @Override
    public void setStats(BaseStats baseStats) {
        str = baseStats.getStr();
        spd = baseStats.getDex();
        def = baseStats.getStm();
    }
}
