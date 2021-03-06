package askew.entity.wall;

import askew.GameCanvas;
import askew.MantisAssetManager;
import askew.entity.FilterGroup;
import askew.entity.obstacle.PolygonObstacle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;

/**
 * Wall to obstruct Flow's movement. This is a polygon, so you can get
 * creative with how the wall is shaped if you'd like.
 * <p>
 * We're building a wall and making libgdx pay for it
 */
@SuppressWarnings("FieldCanBeLocal")
public class WallModel extends PolygonObstacle {

    private static final float WALL_FRICTION = 1.0f;
    private static final float WALL_RESTITUTION = 0.1f;
    private final int color;
    private final transient Color tint;
    private transient TextureRegion circleTextureRegion;
    private transient TextureRegion edgeTextureRegion;
    // Instance variables
    private float x;
    private float y;
    private boolean thorn;

    /**
     * The points that define the convex hull of the wall. Must be an even number (2n) of points representing (x1,y1) ... (xn,yn)
     */
    private float[] points;

    public WallModel(float x, float y, float[] points, int color, boolean
            thorn) {
        super(points, x, y);
        this.x = x;
        this.y = y;
        this.thorn = thorn;
        this.points = points;
        this.color = color;

        tint = new Color(color);
        this.setBodyType(BodyDef.BodyType.StaticBody);
        this.setDensity(0);
        this.setFriction(WALL_FRICTION);
        this.setRestitution(WALL_RESTITUTION);
        Filter f = new Filter();
        if (thorn) {
            f.maskBits = FilterGroup.SLOTH | FilterGroup.VINE;
            f.categoryBits = FilterGroup.LOSE;
            this.setName("thorns");
        } else {
            f.maskBits = FilterGroup.BODY | FilterGroup.VINE;
            f.categoryBits = FilterGroup.WALL;
        }

        this.setFilterData(f);
    }

    @Override
    public void setTextures(MantisAssetManager manager) {
        if (circleTextureRegion == null) {
            TextureRegion wallTextureRegion;
            wallTextureRegion = manager.getProcessedTextureMap().get(MantisAssetManager.WALL_TEXTURE);
            if (thorn)  edgeTextureRegion = manager.getProcessedTextureMap()
                    .get(MantisAssetManager.THORN_TEXTURE);
                else
                    edgeTextureRegion = manager.getProcessedTextureMap()
                    .get(MantisAssetManager.EDGE_TEXTURE);
            edgeTextureRegion.setV(.035f);
            edgeTextureRegion.setV2(.965f);
            circleTextureRegion = new TextureRegion(manager.get("texture/wall/corner.png", Texture.class));
            setTexture(wallTextureRegion);
        }
    }

    @Override
    public void draw(GameCanvas canvas) {

        float edgeWidth = 16f;
        if (thorn) edgeWidth = 32f;

        // Draw corners
        for (int i = 0; i < points.length; i += 2) {
            //TextureRegion region, Color tint, float ox, float oy,float x, float y, float angle, float sx, float sy)
            canvas.draw(circleTextureRegion, tint, circleTextureRegion.getRegionWidth() / 2f, circleTextureRegion.getRegionHeight() / 2f, (getX() + points[i]) * drawScale.x, (getY() + points[i + 1]) * drawScale.y, 0, edgeWidth / edgeTextureRegion.getRegionHeight() / 2, edgeWidth / edgeTextureRegion.getRegionHeight() / 2);
        }

        // Base draw
        if (region != null) {
            canvas.draw(region, tint, 0, 0, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 1, 1);
        }

        // TODO: Still need to set scaling on y, determines how thick
        for (int i = 0; i < points.length; i += 2) {
            float x1 = points[i];
            float y1 = points[i + 1];
            float x2 = points[(i + 2) % points.length];
            float y2 = points[(i + 3) % points.length];

            edgeTextureRegion.setRegionWidth((int) Math.sqrt((drawScale.y * drawScale.y * (y2 - y1) * (y2 - y1))
                    + (drawScale.x * drawScale.x * (x2 - x1) * (x2 - x1))));
            canvas.draw(edgeTextureRegion, tint, 0, 0,
                    (getX() + x1) * drawScale.x, (getY() + y1) * drawScale.y,
                    (float) Math.atan2(y2 - y1, x2 - x1), 1, edgeWidth / edgeTextureRegion.getRegionHeight());
        }


    }

    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        this.x = x;
        this.y = y;
    }

    public void pinchCreate(float bdx, float bdy) {
        // find the closest point in the existing points list and insert after.
        // the closest point minimizes pinch distance
        int index = -1;
        float minDst = Float.MAX_VALUE;
        for (int i = 0; i < points.length; i += 2) {
            float curX = points[i];
            float curY = points[i + 1];
            float nextX = points[(i + 2) % points.length];
            float nextY = points[(i + 3) % points.length];
            float dx = curX - bdx;
            float dy = curY - bdy;
            float dst = (float) Math.sqrt(dx * dx + dy * dy);
            float dx2 = nextX - bdx;
            float dy2 = nextY - bdy;
            float dst2 = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);
            if ((dst + dst2) < minDst) {
                index = i;
                minDst = dst + dst2;
            }
        }

        float[] newPoints = new float[points.length + 2];
        for (int i = 0; i <= index; i += 2) {
            newPoints[i] = points[i];
            newPoints[i + 1] = points[i + 1];
        }

        newPoints[index + 2] = bdx;
        newPoints[index + 3] = bdy;

        for (int i = index + 4; i < newPoints.length; i += 2) {
            newPoints[i] = points[i - 2];
            newPoints[i + 1] = points[i + 1 - 2];
        }
        this.points = newPoints;
        this.initShapes(points);
        this.initBounds();
    }

    public void pinchDelete(float bdx, float bdy) {
        // find the closest point in the existing points list and insert after.
        // the closest point minimizes pinch distance
        int index = -1;
        float minDst = Float.MAX_VALUE;
        //DONTFIXME: start at index 2 because origin must be 0,0
        for (int i = 2; i < points.length; i += 2) {
            float curX = points[i];
            float curY = points[i + 1];
            float dx = curX - bdx;
            float dy = curY - bdy;
            float dst = (float) Math.sqrt(dx * dx + dy * dy);
            if ((dst) < minDst) {
                index = i;
                minDst = dst;
            }
        }


        float[] newPoints = new float[points.length - 2];
        for (int i = 0; i < index; i += 2) {
            newPoints[i] = points[i];
            newPoints[i + 1] = points[i + 1];
        }

        // goodbye old pointo

        for (int i = index; i < newPoints.length; i += 2) {
            newPoints[i] = points[i + 2];
            newPoints[i + 1] = points[i + 1 + 2];
        }
        this.points = newPoints;
        this.initShapes(points);
        this.initBounds();
    }

    public void pinchMove(float bdx, float bdy) {
        // find the closest point in the existing points list and insert after.
        // the closest point minimizes pinch distance
        int index = -1;
        float minDst = Float.MAX_VALUE;
        //DONTFIXME: start at index 2 because origin must be 0,0
        for (int i = 2; i < points.length; i += 2) {
            float curX = points[i];
            float curY = points[i + 1];
            float dx = curX - bdx;
            float dy = curY - bdy;
            float dst = (float) Math.sqrt(dx * dx + dy * dy);
            if ((dst) < minDst) {
                index = i;
                minDst = dst;
            }
        }

        points[index] = bdx;
        points[index + 1] = bdy;

        this.initShapes(points);
        this.initBounds();
    }

    public float getModelX() {
        return this.x;
    }

    public float getModelY() {
        return this.y;
    }
}

