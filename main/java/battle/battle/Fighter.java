package battle.battle;

/**
 * Created by Cade on 7/31/2014.
 */
public class Fighter {
    private String name;
    private int str;
    public int health;

    public Fighter(){
        this("default",1,50);
    }

    public Fighter(String n, int s, int h){
        name = n;
        str = s;
        health = h;
    }

    public String getName(){
        return name;
    }

    public int getStr(){
        return str;
    }

    public int getHealth(){
        return health;
    }
}
