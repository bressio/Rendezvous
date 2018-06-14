package com.bressio.rendezvous.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.bressio.rendezvous.graphics.ResourceHandler;
import com.bressio.rendezvous.objects.Medkit;
import com.bressio.rendezvous.scenes.Match;

public class Chest extends Loot {

    public Chest(World world, TiledMap map, Rectangle bounds, SpriteBatch batch, ResourceHandler resources, Match match) {
        super(world, map, bounds, batch, resources, match);
        getFixture().setUserData(this);
        getItems().add(new Medkit());
        getItems().add(new Medkit());
    }

    @Override
    public void onPlayerEnter() {
        super.onPlayerEnter();
    }

    @Override
    public void onPlayerLeave() {
        super.onPlayerLeave();
    }
}
