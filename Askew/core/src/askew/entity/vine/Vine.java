/*
 * RopeBridge.java
 *
 * The class is a classic example of how to subclass ComplexPhysicsObject.
 * You have to implement the createJoints() method to stick in all of the
 * joints between objects.
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package askew.entity.vine;

import askew.GlobalConfiguration;
import askew.MantisAssetManager;
import askew.entity.Entity;
import askew.entity.FilterGroup;
import askew.entity.obstacle.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import lombok.Getter;
import lombok.Setter;

/**
 * A vine with segments connected by revolute joints.
 * <p>
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue"})
public class Vine extends ComplexObstacle {

    public static final float lheight = .7f;
    public static final String[] VINE_TEXTURES = {"texture/vine/vine.png", "texture/vine/vine2.png", "texture/vine/vine3.png", "texture/vine/vine4.png"};
    public static final float DAMPING_ROTATION = 5f;
    private static final String VINE_NAME = "vine";
    /**
     * The debug name for the entire obstacle
     */
    private static final String PLANK_NAME = "vine_piece";
    /**
     * The debug name for each segment
     */
    private static final String ANCHOR_NAME = "vine_pin";
    /**
     * The debug name for each anchor pin
     */
    private static final float ANCHOR_RADIUS = 0.1f;
    /**
     * The density of each plank in the bridge
     */
    private static final float VINE_WIDTH = .10f;
    private static transient String VINE_TEXTURE;        // default texture
    /**
     * The spacing between each piece
     */
    private final transient boolean topPin;
    //JSON
    @Getter
    @Setter
    protected float numLinks;
    /**
     * number of vine pieces
     */
    @Getter
    @Setter
    protected float x;
    /**
     * x-coord of top anchor
     */
    @Getter
    @Setter
    protected float y;
    /**
     * y-coord of bottom anchor
     */
    @Getter
    @Setter
    protected float angle = 5f;
    /**
     * starting angle of vine
     */
    @Getter
    @Setter
    protected float omega = -400f;
    /**
     * The radius of each anchor pin
     */
    private transient float BASIC_DENSITY;
    // Invisible anchor entities
    private transient WheelObstacle start = null;                // anchor point of vine (top)
    private transient WheelObstacle finish = null;                // optional bottom anchor (bottom)
    /**
     * Set damping constant for joint rotation in vines
     */

    // Dimension information
    private transient Vector2 dimension;
    /**
     * The length of the entire vine
     */
    private transient Vector2 planksize;
    /**
     * The size of a vine piece
     */
    private transient float linksize = 1.0f;
    /**
     * The length of each vine piece
     */
    private transient float spacing = 0.0f;
    @Getter
    private transient Obstacle endpt;                        // to help with tutorial getting vine endpt
    /**
     * starting angular velocity of vine
     */
    @Getter
    @Setter
    private boolean pin = false;                                // default no bottom anchor
    private int texture = 0;                                        // default texture index


    public Vine(float x, float y, float length, float angle, float omega, int texture) {
        this(x, y, length, angle, omega, texture, true);
    }

    public Vine(float x, float y, float length, float angle, float omega, int texture, boolean topPin) {
        super(x, y);
        this.x = x;
        this.y = y;
        this.numLinks = length;
        this.pin = false;
        this.angle = angle;
        this.omega = omega;
        this.topPin = topPin;
        this.texture = texture;
        this.setDrawNumber(Entity.DN_VINE);
        VINE_TEXTURE = VINE_TEXTURES[texture];
        build();
    }

    public void build() {
        setName(VINE_NAME);

        float x1 = x;
        float y1 = y - numLinks;

        this.BASIC_DENSITY = GlobalConfiguration.getInstance().getAsFloat("vineDensity");


        planksize = new Vector2(VINE_WIDTH, lheight);
        linksize = lheight;


        // Compute the bridge length
        dimension = new Vector2(x1 - x, y1 - y);
        float length = dimension.len();
        Vector2 norm = new Vector2(dimension);
        norm.nor();
        norm.rotate(angle);

        // If too small, only make one plank.
        int nLinks = (int) (length / linksize);
        if (nLinks <= 1) {
            nLinks = 1;
            linksize = length;
            spacing = 0;
        } else {
            spacing = length - nLinks * linksize;
            spacing /= (nLinks - 1);
        }

        Vector2 pos = new Vector2();
        for (int ii = 0; ii < nLinks; ii++) {
            float t = ii * (linksize + spacing) + linksize / 2.0f;
            pos.set(norm);
            pos.scl(t);
            pos.add(x, y);
            BoxObstacle plank = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
            plank.setName(PLANK_NAME + ii);
            plank.setDensity(BASIC_DENSITY);
            plank.setAngle((float) Math.toRadians(angle));
            plank.setAngularVelocity(omega * (nLinks - ii - 1) / (nLinks));
            Filter f = new Filter();
            f.maskBits = FilterGroup.WALL | FilterGroup.HAND;
            f.categoryBits = FilterGroup.VINE;
            plank.setFilterData(f);
            bodies.add(plank);
        }
        endpt = bodies.get(bodies.size - 1);

        setCustomScale(3.0f, 1.05f);
    }


    public void rebuild() {
        bodies.clear();
        build();
    }

    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        this.x = x;
        this.y = y;
    }

    /**
     * Creates the joints for this object.
     * <p>
     * This method is executed as part of activePhysics. This is the primary method to
     * override for custom physics entities.
     *
     * @param world Box2D world to store joints
     * @return true if object allocation succeeded
     */
    protected boolean createJoints(World world) {
        assert bodies.size > 0;

        Vector2 anchor1 = new Vector2();
        Vector2 anchor2 = new Vector2(0, linksize / 2);

        // Create the top anchor
        // Normally, we would do this in constructor, but we have
        // reasons to not add the anchor to the bodies list.
        Vector2 pos = bodies.get(0).getPosition();
        pos.y += linksize / 2;
        start = new WheelObstacle(pos.x, pos.y, ANCHOR_RADIUS);
        Filter f = new Filter();
        f.maskBits = FilterGroup.NOCOLLIDE;
        f.categoryBits = FilterGroup.NOCOLLIDE;
        start.setFilterData(f);
        start.setName(ANCHOR_NAME + 0);
        start.setDensity(BASIC_DENSITY);
        start.setBodyType(BodyDef.BodyType.StaticBody);
        start.activatePhysics(world);


        // Definition for a revolute joint
        RevoluteJointDef jointDef = new RevoluteJointDef();

        Joint joint;

        if (topPin) {
            // Initial joint
            jointDef.bodyB = start.getBody();
            jointDef.bodyA = bodies.get(0).getBody();
            jointDef.localAnchorB.set(anchor1);
            jointDef.localAnchorA.set(anchor2);
            jointDef.collideConnected = false;
            joint = world.createJoint(jointDef);
            joints.add(joint);
        }


        // Link the pieces together
        anchor1.y = -linksize / 2;
        for (int ii = 0; ii < bodies.size - 1; ii++) {
            // join the planks
            jointDef.bodyB = bodies.get(ii).getBody();
            jointDef.bodyA = bodies.get(ii + 1).getBody();
            jointDef.localAnchorB.set(anchor1);
            jointDef.localAnchorA.set(anchor2);
            jointDef.collideConnected = false;
            joint = world.createJoint(jointDef);
            joints.add(joint);
        }

        // optional
        if (pin) {
            // Create the bottom anchor
            Obstacle last = bodies.get(bodies.size - 1);

            pos = last.getPosition();
            pos.y += linksize;
            finish = new WheelObstacle(pos.x, pos.y, ANCHOR_RADIUS);
            finish.setFilterData(f);
            finish.setName(ANCHOR_NAME + 1);
            finish.setDensity(BASIC_DENSITY);
            finish.setBodyType(BodyDef.BodyType.StaticBody);
            finish.activatePhysics(world);

            // Final joint
            anchor2.y = 0;
            jointDef.bodyB = last.getBody();
            jointDef.bodyA = finish.getBody();
            jointDef.localAnchorB.set(anchor1);
            jointDef.localAnchorA.set(anchor2);
            joint = world.createJoint(jointDef);
            joints.add(joint);
        }

        return true;
    }

    /**
     * Destroys the physics Body(s) of this object if applicable,
     * removing them from the world.
     *
     * @param world Box2D world that stores body
     */
    public void deactivatePhysics(World world) {
        super.deactivatePhysics(world);
        if (start != null) {
            start.deactivatePhysics(world);
        }
    }

    /**
     * Returns the texture for the individual pieces
     *
     * @return the texture for the individual pieces
     */
    public TextureRegion getTexture() {
        if (bodies.size == 0) {
            return null;
        }
        return ((SimpleObstacle) bodies.get(0)).getTexture();
    }

    @Override
    public void setTextures(MantisAssetManager manager) {
        Texture vineTexture = manager.get(VINE_TEXTURE, Texture.class);
        TextureRegion regionedTexture = new TextureRegion(vineTexture);
        for (Obstacle body : bodies) {
            ((SimpleObstacle) body).setTexture(regionedTexture);
        }
    }

}