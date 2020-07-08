package net.sleeplessdev.smarthud.util.interpolation;


import net.minecraft.util.math.vector.Vector2f;

/**
 * Derived from: https://github.com/codesoup/android-cubic-bezier-interpolator
 * <p>
 * Creates a cubic bezier curve based on two vectors, and allows simple
 * interpolation of a 0..1 time value.
 */

public final class CubicBezierInterpolator implements Interpolator {
    private final Vector2f start;
    private final Vector2f end;

    // Calculation storage to avoid unnecessary instantiation
    private Vector2f a = new Vector2f(0,0);
    private Vector2f b = new Vector2f(0,0);
    private Vector2f c = new Vector2f(0,0);

    public CubicBezierInterpolator(
        final Vector2f start, final Vector2f end
    ) throws IllegalArgumentException {
        if (start.x < 0 || start.x > 1) {
            throw new IllegalArgumentException("start X value must be in the range [0, 1]");
        }

        if (end.x < 0 || end.x > 1) {
            throw new IllegalArgumentException("end X value must be in the range [0, 1]");
        }

        this.start = start;
        this.end = end;
    }

    public CubicBezierInterpolator(final float startX, final float startY, final float endX, final float endY) {
        this(new Vector2f(startX, startY), new Vector2f(endX, endY));
    }

    public CubicBezierInterpolator(final double startX, final double startY, final double endX, final double endY) {
        this((float) startX, (float) startY, (float) endX, (float) endY);
    }

    @Override
    public float interpolate(final float time) {
        return getBezierCoordinateY(getXForTime(time));
    }

    protected float getBezierCoordinateY(final float time) {
        c = new Vector2f(c.x,3 * start.y);
        b = new Vector2f(b.x,3 * (end.y - start.y) - c.y);
        a = new Vector2f(a.x,1 - c.y - b.y);

        return time * (c.y + time * (b.y + time * a.y));
    }

    protected float getXForTime(final float time) {
        float x = time;
        float z;

        for (int i = 1; i < 14; i++) {
            z = getBezierCoordinateX(x) - time;
            if (Math.abs(z) < 1e-3) {
                break;
            }
            x -= z / getSlope(x);
        }

        return x;
    }

    private float getSlope(final float t) {
        return c.x + t * (2 * b.x + 3 * a.x * t);
    }

    private float getBezierCoordinateX(final float time) {
        c = new Vector2f(3 * start.x,c.y);
        b = new Vector2f(3 * (end.x - start.x) - c.x,b.y);
        a = new Vector2f(1 - c.x - b.x,a.y);

        return time * (c.x + time * (b.x + time * a.x));
    }
}
