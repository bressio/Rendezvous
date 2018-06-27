package com.bressio.rendezvous.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.bressio.rendezvous.entities.objects.Inventory;
import com.bressio.rendezvous.forge.BodyBuilder;
import com.bressio.rendezvous.graphics.AnimationRegion;
import com.bressio.rendezvous.graphics.Animator;
import com.bressio.rendezvous.graphics.ResourceHandler;
import com.bressio.rendezvous.scenes.Match;

import static com.bressio.rendezvous.scheme.PhysicsAdapter.*;

public abstract class Soldier extends Entity {

    private final float radius;
    private final float linearDamping;
    private int speed;
    private final short categoryBits;
    private final short maskBits;
    private final Object userData;
    private AnimationRegion animationRegion;
    private Object lastSelectedObjectClass;
    private Object lastEquippedArmorClass;
    private Object lastEquippedHelmetClass;

    private Fixture fixture;

    private Animator animator;
    private int health = 100;
    private int armor = 0;
    private Inventory inventory;

    private Texture pointlight;

    private boolean isFiring;

    Soldier(Match match, Vector2 position, float radius, float linearDamping, int speed,
            AnimationRegion animationRegion, short categoryBits, short maskBits, Object userData) {
        super(match, position, animationRegion, ResourceHandler.TextureAtlasPath.SOLDIER_ATLAS);

        this.radius = radius;
        this.linearDamping = linearDamping;
        this.speed = speed;
        this.animationRegion = animationRegion;
        this.categoryBits = categoryBits;
        this.userData = userData;
        this.maskBits = maskBits;
        init();
        buildBody();
    }

    private void init() {
        animator = new Animator(this, animationRegion);
        inventory = new Inventory(getMatch());
        setOrigin(pScaleCenter(animationRegion.getFrameWidth()), pScaleCenter(animationRegion.getFrameHeight()));
        setBounds(0, 0, pScale(animationRegion.getFrameWidth()), pScale(animationRegion.getFrameHeight()));
        setRegion(animator.getIdleTexture());
        pointlight = getMatch().getResources().getTexture(ResourceHandler.TexturePath.POINTLIGHT);
    }

    @Override
    protected void buildBody() {
        setBody(new BodyBuilder(getMatch().getWorld(), getPosition())
                .withBodyType(BodyDef.BodyType.DynamicBody)
                .withRadius(pScale(radius))
                .withLinearDamping(linearDamping)
                .withCategoryBits(categoryBits)
                .withMaskBits(maskBits)
                .withUserData(userData)
                .build());
        fixture = getBody().getFixtureList().first();
    }

    public void update(float delta) {
        setPosition(
                getBody().getPosition().x - pCenter(getWidth()),
                getBody().getPosition().y - pCenter(getHeight()));
        setRegion(animator.getFrame(delta, 1));
        verifyItems();
    }

    private void verifyItems() {
        Object selectedObjectClass = inventory.getItem(getMatch().getHud().getSelectedSlot()).getClass();
        Object selectedAmorClass = inventory.getEquipmentItems().get(1).getClass();
        Object selectedHelmetClass = inventory.getEquipmentItems().get(0).getClass();

        if (selectedObjectClass != lastSelectedObjectClass || selectedAmorClass != lastEquippedArmorClass ||
                selectedHelmetClass != lastEquippedHelmetClass) {
            animator.verify(selectedAmorClass, selectedHelmetClass, selectedObjectClass);
        }
        lastSelectedObjectClass = selectedObjectClass;
        lastEquippedArmorClass = selectedAmorClass;
        lastEquippedHelmetClass = selectedHelmetClass;
    }

    @Override
    public void draw(Batch batch) {
        if (isFiring) {
            batch.draw(pointlight,
                    getBody().getPosition().x - pScaleCenter(pointlight.getWidth()),
                    getBody().getPosition().y - pScaleCenter(pointlight.getHeight()),
                    pScale(pointlight.getWidth()),
                    pScale(pointlight.getHeight()));
        }
        super.draw(batch);
    }

    public void setFiring(boolean firing) {
        isFiring = firing;
    }

    public void switchAnimation(AnimationRegion animationRegion, ResourceHandler.TextureAtlasPath textureAtlasPath) {
        this.animationRegion = animationRegion;
        setRegion(getMatch().getResources().getTextureAtlas(textureAtlasPath).findRegion(animationRegion.getRegion()));
        animator = new Animator(this, animationRegion);
        setBounds(0, 0, pScale(animationRegion.getFrameWidth()), pScale(animationRegion.getFrameHeight()));
        setOrigin(pScaleCenter(animationRegion.getFrameWidth()), pScaleCenter(animationRegion.getFrameHeight()));
    }

    public void changeHealth(int difference) {
        health = health + difference < 0 ? 0 : health + difference > 100 ? 100 : health + difference;
    }

    public Inventory getInventory() {
        return inventory;
    }

    int getSpeed() {
        return speed;
    }

    public int getHealth() {
        return health;
    }

    public int getArmor() {
        return armor;
    }

    public void setArmor(int armor) {
        this.armor = armor;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public Fixture getFixture() {
        return fixture;
    }
}
