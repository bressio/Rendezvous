package com.bressio.rendezvous.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.bressio.rendezvous.Rendezvous;
import com.bressio.rendezvous.helpers.PlayerSettings;

public class DesktopLauncher {
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = PlayerSettings.GAME_WIDTH;
        config.height = PlayerSettings.GAME_HEIGHT;
        config.fullscreen = PlayerSettings.FULLSCREEN;
        new LwjglApplication(new Rendezvous(), config);
    }
}
