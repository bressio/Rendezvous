package com.bressio.rendezvous.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bressio.rendezvous.Rendezvous;
import com.bressio.rendezvous.entities.Player;
import com.bressio.rendezvous.graphics.ResourceHandler;
import com.bressio.rendezvous.gui.HUD;
import com.bressio.rendezvous.helpers.PhysicsManager;
import com.bressio.rendezvous.helpers.PlayerSettings;
import com.bressio.rendezvous.helpers.WorldBuilder;

import static com.bressio.rendezvous.helpers.PhysicsManager.pCenter;
import static com.bressio.rendezvous.helpers.PhysicsManager.pScale;
import static com.bressio.rendezvous.helpers.PlayerSettings.DEBUG_MODE;

public class Match implements Screen {

    private final int WIDTH;
    private final int HEIGHT;

    private Rendezvous game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private HUD hud;

    private OrthogonalTiledMapRenderer renderer;

    private World world;
    private Box2DDebugRenderer boxColliderRenderer;

    private Player player;

    private final Vector2 mouseInWorld2D = new Vector2();
    private final Vector3 mouseInWorld3D = new Vector3();

    private ResourceHandler resources;

    public Match(Rendezvous game) {
        WIDTH = PlayerSettings.GAME_WIDTH;
        HEIGHT = PlayerSettings.GAME_HEIGHT;

        resources = new ResourceHandler(ResourceHandler.AnimationAtlas.ENTITIES, ResourceHandler.TileMap.TILEMAP);

        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(pScale(1366), pScale(768), camera);
        hud = new HUD(game.getBatch(), resources);

        renderer = new OrthogonalTiledMapRenderer(resources.getMap(), pScale(1));
        camera.position.set(pScale(3200), pScale(3200), 0);

        world = new World(PhysicsManager.GRAVITY, true);
        boxColliderRenderer = new Box2DDebugRenderer();

        new WorldBuilder(world, resources.getMap());

        player = new Player(world, this, 32, 5, 3200, 3200);
    }

    private void handleInput(float delta) {

        float directionX = 0;
        float directionY = 0;
        int directions = 0;
        final int speed = 10;

        if (Gdx.input.isKeyPressed(Input.Keys.A)){
            directionX -= speed;
            directions++;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.D)){
            directionX += speed;
            directions++;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)){
            directionY += speed;
            directions++;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.S)){
            directionY -= speed;
            directions++;
        }

        if (directionX != 0 || directionY != 0) {
            if (directions == 1) {
                player.getBody().applyForce(new Vector2(directionX, directionY), player.getBody().getWorldCenter(), true);
            } else {
                float a = (float)((speed + Math.pow(directions, 2)) / (speed * directions));
                player.getBody().applyForce(new Vector2(directionX * a, directionY * a), player.getBody().getWorldCenter(), true);
            }
        }
    }

    private void update(float delta) {

        mouseInWorld3D.x = Gdx.input.getX();
        mouseInWorld3D.y = Gdx.input.getY();
        mouseInWorld3D.z = 0;
        camera.unproject(mouseInWorld3D);
        mouseInWorld2D.x = mouseInWorld3D.x;
        mouseInWorld2D.y = mouseInWorld3D.y;

        float angle = MathUtils.radiansToDegrees * MathUtils.atan2(mouseInWorld2D.y - (player.getY() + pCenter(player.getHeight())), mouseInWorld2D.x - (player.getX() + pCenter(player.getWidth())));

        if(angle < 0){
            angle += 360;
        }
        player.setRotation(angle - 90);

        handleInput(delta);
        world.step(1 / 60f, 6, 2);
        player.update(delta);
        camera.position.set(player.getBody().getPosition(), 0);
        camera.update();
        renderer.setView(camera);
    }

    public TextureAtlas getAtlas() {
        return resources.getAtlas();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();

        if (DEBUG_MODE) {
            boxColliderRenderer.render(world, camera.combined);
        }

        game.getBatch().setProjectionMatrix(camera.combined);

        game.getBatch().begin();
        player.draw(game.getBatch());
        game.getBatch().end();

        game.getBatch().setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
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
        boxColliderRenderer.dispose();
    }
}
