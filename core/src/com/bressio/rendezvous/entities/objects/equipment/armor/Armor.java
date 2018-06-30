package com.bressio.rendezvous.entities.objects.equipment.armor;

import com.bressio.rendezvous.entities.Soldier;
import com.bressio.rendezvous.entities.objects.EntityObject;
import com.bressio.rendezvous.scenes.Match;

public abstract class Armor extends EntityObject {

    private int armorPoints;
    private int damage;

    Armor(Match match) {
        super(match);
    }

    public abstract void updateName();

    @Override
    public abstract boolean transformSoldier(Soldier soldier);

    public int getArmorPoints() {
        return 100 - ((damage / armorPoints) * 100);
    }

    void setArmorPoints(int armorPoints) {
        this.armorPoints = armorPoints;
    }

    public int getDamage() {
        return damage;
    }

    public void takeDamage(int damage) {
        this.damage += damage;
    }
}