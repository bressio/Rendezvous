package com.bressio.rendezvous.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bressio.rendezvous.Rendezvous;
import com.bressio.rendezvous.entities.Loot;
import com.bressio.rendezvous.entities.Player;
import com.bressio.rendezvous.events.InputTracker;
import com.bressio.rendezvous.events.RendezvousController;
import com.bressio.rendezvous.events.WorldContactListener;
import com.bressio.rendezvous.forge.WorldBuilder;
import com.bressio.rendezvous.graphics.ResourceHandler;
import com.bressio.rendezvous.gui.HUD;
import com.bressio.rendezvous.gui.LootInterface;
import com.bressio.rendezvous.gui.MatchMap;
import com.bressio.rendezvous.gui.PauseMenu;
import com.bressio.rendezvous.languages.Internationalization;
import com.bressio.rendezvous.objects.EntityObject;
import com.bressio.rendezvous.scheme.PhysicsAdapter;

import java.util.ArrayList;

import static com.bressio.rendezvous.scheme.PhysicsAdapter.*;
import static com.bressio.rendezvous.scheme.PlayerSettings.*;

public class Match implements Screen {

    public enum GameState {
        RUNNING, PAUSED, TACTICAL, LOOTING
    }

    // game
    private Rendezvous game;
    private Internationalization i18n;
    private ResourceHandler resources;

    // rendering
    private OrthographicCamera camera;
    private Viewport viewport;
    private HUD hud;
    private PauseMenu pause;
    private LootInterface loot;
    private MatchMap matchMap;
    private OrthogonalTiledMapRenderer renderer;
    private OrthogonalTiledMapRenderer overRenderer;
    private Box2DDebugRenderer collisionDebugRenderer;
    private int cameraZoom = 1;

    // world
    private World world;
    private WorldBuilder worldBuilder;
    private Player player;
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private TiledMap overMap;
    private TextureAtlas atlas;

    // events
    private InputTracker input;
    private GameState state;
    private RendezvousController rendezvousController;

    Match(Rendezvous game) {
        this.game = game;
        loadResources();
        setupCamera();
        setupRenderer();
        setupCursor();
        forgeWorld();
        setupInputTracker();
    }

    private void loadResources() {
        resources = new ResourceHandler();
        resources.loadMatchResources();
        atlas = resources.getTextureAtlas(ResourceHandler.TextureAtlasPath.ENTITY_ATLAS);
        mapLoader = new TmxMapLoader();
        map = resources.getTiledMap(ResourceHandler.TiledMapPath.TILEMAP);
        overMap = resources.getTiledMap(ResourceHandler.TiledMapPath.OVER_TILEMAP);
        i18n = new Internationalization();
    }

    private void setupCamera() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(pScale(GAME_WIDTH) * cameraZoom, pScale(GAME_HEIGHT) * cameraZoom, camera);
        viewport.apply();
        hud = new HUD(game.getBatch(), resources);
        pause = new PauseMenu(game.getBatch(), i18n, resources, this);
//        loot = new LootInterface(game.getBatch(), i18n, resources, this);
        matchMap = new MatchMap(game.getBatch(), i18n, resources);
        camera.position.set(pScale((float) Math.sqrt(MAP_AREA)), pScale((float) Math.sqrt(MAP_AREA)), 0);
        camera.update();
    }

    private void setupRenderer() {
        renderer = new OrthogonalTiledMapRenderer(map, PhysicsAdapter.getScale());
        overRenderer = new OrthogonalTiledMapRenderer(overMap, PhysicsAdapter.getScale());
        collisionDebugRenderer = new Box2DDebugRenderer();
        state = GameState.RUNNING;
    }

    private void setupCursor() {
        setCursor(ResourceHandler.PixmapPath.MATCH_CURSOR, true);
    }

    private void forgeWorld() {
        world = new World(GRAVITY, true);
        worldBuilder = new WorldBuilder(world, map, game.getBatch(), resources, this);
        player = new Player(world, this, 32, 5, 10, worldBuilder.getPlayerSpawnPoint());
        world.setContactListener(new WorldContactListener());
        rendezvousController = new RendezvousController(game.getBatch(), i18n , hud, resources, player);
    }

    private void setupInputTracker() {
        input = new InputTracker(camera);
        Gdx.input.setInputProcessor(input);
    }

    private void update(float delta) {
        world.step(1 / 60f, 6, 2);
        rendezvousController.update(delta);
        player.update(delta);
        matchMap.update(delta, player.getBody().getPosition());
        // smoothly moves the camera to the player's position with an offset generated by the mouse input
        camera.position.lerp(new Vector3(
                player.getBody().getPosition().x + (float)input.getRelativeX() / 500,
                player.getBody().getPosition().y - (float)input.getRelativeY() / 500,
                0), 0.05f);
        // the camera position is rounded to avoid artifacts on unfiltered textures
        camera.position.set((float) ((double) Math.round(camera.position.x * 100d) / 100d),
                (float) ((double) Math.round(camera.position.y * 100d) / 100d), camera.position.z);
        camera.update();
        renderer.setView(camera);
        overRenderer.setView(camera);
    }

    private void handlePauseMenu(float delta) {
        if (InputTracker.isPressed(InputTracker.ESC)){
            if (state == GameState.RUNNING || state == GameState.TACTICAL || state == GameState.LOOTING) {
                input.resetAllKeys();
                Gdx.input.setInputProcessor(pause.getStage());
                setCursor(ResourceHandler.PixmapPath.MENU_CURSOR, false);
                setState(GameState.PAUSED);
            }
        }
    }

    public void handleLootInterface(float delta, ArrayList<EntityObject> items) {
        if (InputTracker.isPressed(InputTracker.E)){
            if (state == GameState.RUNNING || state == GameState.TACTICAL) {
                loot = new LootInterface(game.getBatch(), i18n, resources, this, items, player.getInventory().getItems());
                input.resetAllKeys();
                Gdx.input.setInputProcessor(loot.getStage());
                setCursor(ResourceHandler.PixmapPath.MENU_CURSOR, false);
                setState(GameState.LOOTING);
            }
        }
    }

    private void handleMatchMap(float delta) {
        if (InputTracker.isPressed(InputTracker.M)){
            if (state == GameState.RUNNING) {
                input.resetSecondaryKeys();
                setCursor(ResourceHandler.PixmapPath.MENU_CURSOR, false);
                setState(GameState.TACTICAL);
            } else if (state == GameState.TACTICAL) {
                input.resetSecondaryKeys();
                setCursor(ResourceHandler.PixmapPath.MATCH_CURSOR, true);
                setState(GameState.RUNNING);
            }
        }
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public ResourceHandler getResources() {
        return resources;
    }

    private void pausedRender(float delta) {
        game.getBatch().setProjectionMatrix(pause.getStage().getCamera().combined);
        pause.getStage().draw();
    }

    private void lootingRender(float delta) {
        game.getBatch().setProjectionMatrix(loot.getStage().getCamera().combined);
        loot.getStage().draw();
        loot.getUpperStage().draw();
    }

    private void mapRender(float delta) {
        game.getBatch().setProjectionMatrix(matchMap.getStage().getCamera().combined);
        matchMap.getStage().draw();
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public void delegateInputProcessor() {
        Gdx.input.setInputProcessor(input);
    }

    public void setCursor(ResourceHandler.PixmapPath pixmapPath, boolean isCentered) {
        Pixmap pixmap = resources.getPixmap(pixmapPath);
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pixmap,
                isCentered ? pCenter(pixmap.getWidth()) : 0,
                isCentered ? pCenter(pixmap.getHeight()) : 0));
    }

    private void updateLoot(float delta) {
        for (Loot loot : worldBuilder.getLoot()) {
            loot.update(delta);
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (state == GameState.RUNNING || state == GameState.TACTICAL || state == GameState.LOOTING) {
            update(delta);
        }
        Gdx.gl.glClearColor((float)19 / 255, (float)174 / 255, (float)147 / 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();

        if (DEBUG_MODE) {
            collisionDebugRenderer.render(world, camera.combined);
        }

        game.getBatch().setProjectionMatrix(camera.combined);

        game.getBatch().begin();
        player.draw(game.getBatch());
        game.getBatch().end();

        overRenderer.render();

        for (Loot loot : worldBuilder.getLoot()) {
            loot.drawInteractionButton();
        }

        //rendezvousController.updateGraphics(delta);

        game.getBatch().setProjectionMatrix(hud.getStage().getCamera().combined);
        hud.drawVignette(delta);
        hud.drawHealthBars(delta, player.getHealth(), player.getArmor());
        hud.getStage().draw();

        hud.updateHealthBars(delta, player.getHealth(), player.getArmor());


        if (state != GameState.TACTICAL) {
            hud.drawMinimap(delta, player.getBody().getPosition());
        }

        if (state == GameState.PAUSED) {
            pausedRender(delta);
            pause.update(delta);
        } else if (state == GameState.TACTICAL) {
            mapRender(delta);
        } else if (state == GameState.LOOTING) {
            lootingRender(delta);
            loot.update(delta);
        }

        input.update();
        handlePauseMenu(delta);
        handleMatchMap(delta);

        updateLoot(delta);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        renderer.dispose();
        world.dispose();
        collisionDebugRenderer.dispose();
        resources.dispose();
    }
}
