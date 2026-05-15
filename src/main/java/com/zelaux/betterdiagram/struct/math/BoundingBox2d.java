package com.zelaux.betterdiagram.struct.math;

import com.mojang.serialization.Codec;
import dev.ryanhcode.sable.companion.impl.SableCompanionUtil;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Position;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.joml.Vector2dc;

import java.util.List;

/**
 * A bounding box with JOML interaction.
 *
 * @since 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public final class BoundingBox2d implements BoundingBox2dc {

    public static final BoundingBox2d EMPTY = new BoundingBox2d(0, 0, 0, 0);
    public static final BoundingBox2d[] EMPTY_ARRAY = new BoundingBox2d[0];

    public static Codec<BoundingBox2d> CODEC = Codec.DOUBLE.listOf().comapFlatMap((list) -> SableCompanionUtil.fixedSize(list, 6).map(
            (iList) -> new BoundingBox2d(iList.getFirst(),
                iList.get(1),
                iList.get(2),
                iList.get(3))),
        (bb) -> List.of(bb.minX,
            bb.minY,
            bb.maxX,
            bb.maxY));

    public double minX;
    public double minY;
    public double maxX;
    public double maxY;

    public static BoundingBox2d box2d(final double minX, final double minY, final double maxX, final double maxY) {
        return new BoundingBox2d(minX, minY, maxX, maxY);
    }

    /**
     * Creates a new bounding box with the given values
     */
    public BoundingBox2d(final double minX, final double minY, final double maxX, final double maxY) {
        this.set(minX, minY, maxX, maxY);
    }

    /**
     * Creates a new bounding box with the given values
     */
    public BoundingBox2d(final BoundingBox2dc other) {
        this.set(other);
    }

    /**
     * Creates a new bounding box with the given values
     */
    public BoundingBox2d(final AABB other) {
        this.set(other.minX, other.minY, other.maxX, other.maxY);
    }

    /**
     * Creates a new bounding box with the given values
     */
    public BoundingBox2d(final BoundingBox other) {
        this.set(other.minX(), other.minY(), other.maxX(), other.maxY());
    }

    /**
     * Creates a new bounding box with the given values
     *
     * @deprecated Use {@link #BoundingBox2d(Position, Position)} instead
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Deprecated
    public BoundingBox2d(final Vec3 from, final Vec3 to) {
        this.set(from.x, from.y, to.x, to.y);
    }

    /**
     * Creates a new bounding box with the given values
     *
     * @since 1.2.0
     */
    public BoundingBox2d(final Position from, final Position to) {
        this.set(from.x(), from.y(), to.x(), to.y());
    }

    /**
     * Default constructor for an all-zero bounding box
     */
    public BoundingBox2d() {
        this.set(0, 0, 0, 0);
    }

    public static BoundingBox2d box2d(LayoutElement elem) {
        int x = elem.getX();
        int y = elem.getY();
        return new BoundingBox2d(x, y, elem.getWidth() + x, elem.getHeight() + y);
    }

    public static BoundingBox2d box2d(Screen screen) {
        return new BoundingBox2d(0, 0, screen.width, screen.height);
    }

    public static BoundingBox2d box2d(ScreenRectangle rectangle) {
        return new BoundingBox2d(rectangle.left(), rectangle.top(), rectangle.right(), rectangle.bottom());
    }

    /**
     * Sets the bounding box to the given values
     */
    @Contract(value = "_->this", mutates = "this")
    public BoundingBox2d set(final BoundingBox2dc other) {
        this.set(other.minX(), other.minY(), other.maxX(), other.maxY());
        return this;
    }


    /**
     * Sets the bounding box to the given values.
     * Automatically swaps the values to ensure min values are less than max values.
     */
    @Contract(value = "_,_,_,_,->this", mutates = "this")
    public BoundingBox2d set(final double minX, final double minY, final double maxX, final double maxY) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        return this;
    }

    /**
     * Sets the bounding box to the given values.
     * <br>
     * <strong>Does NOT automatically swap mins/maxes if swapped.</strong>
     */
    @Contract(value = "_,_,_,_,->this", mutates = "this")
    public BoundingBox2d setUnchecked(final double minX, final double minY, final double maxX, final double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        return this;
    }

    /**
     * Sets the bounding box to the given values.
     * <br>
     * <strong>Does NOT automatically swap mins/maxes if swapped.</strong>
     */
    @Contract(value = "_->this", mutates = "this")
    public BoundingBox2d setUnchecked(final BoundingBox2dc other) {
        this.minX = other.minX();
        this.minY = other.minY();
        this.maxX = other.maxX();
        this.maxY = other.maxY();
        return this;
    }

    /**
     * Expands this box to include the given point
     *
     * @param point the point to include
     * @return the result stored in dest
     */
    @Contract(value = "_->this", mutates = "this")
    public BoundingBox2d expandTo(final Vector2dc point) {
        return this.expandTo(point.x(), point.y());
    }

    /**
     * Expands this box to include the given point
     *
     * @param x the x value of the point
     * @param y the y value of the point
     * @return this
     */
    @Contract(value = "_,_,_->this", mutates = "this")
    public BoundingBox2d expandTo(final double x, final double y) {
        return this.expandTo(x, y, this);
    }

    /**
     * Expands this box to include the given box
     *
     * @param other the box to include
     * @return this
     */
    @Contract(value = "_->this", mutates = "this")
    public BoundingBox2d expandTo(final BoundingBox2dc other) {
        return this.expandTo(other, this);
    }

    /**
     * Expands this box by the given amount on all sides
     *
     * @param amount the amount to expand by
     * @return this
     */
    @Contract(value = "_->this", mutates = "this")
    public BoundingBox2d expand(final double amount) {
        return this.expand(amount, amount);
    }

    /**
     * Expands this box by the given amount on all sides
     *
     * @param amountX the amount to expand by in the x
     * @param amountY the amount to expand by in the y
     * @return this
     */
    @Contract(value = "_,_->this", mutates = "this")
    public BoundingBox2d expand(final double amountX, final double amountY) {
        return this.expand(amountX, amountY, this);
    }

    /**
     * Moves this box by the given amount on all sides
     *
     * @param amountX The amount to move by in the x
     * @param amountY The amount to move by in the y
     * @return this
     */
    @Contract(value = "_,_->this", mutates = "this")
    public BoundingBox2d move(final double amountX, final double amountY) {
        return this.move(amountX, amountY, this);
    }

    /**
     * Calculates the bounding box intersect between this box and the specified box.
     *
     * @param box The box to intersect with
     * @return this
     */
    @Contract(value = "_->this", mutates = "this")
    public BoundingBox2d intersect(final BoundingBox2dc box) {
        return this.intersect(box, this);
    }


    /**
     * @return the minimum x value of this box
     */
    @Override
    public double minX() {
        return this.minX;
    }

    /**
     * @return the minimum y value of this box
     */
    @Override
    public double minY() {
        return this.minY;
    }


    /**
     * @return the maximum x value of this box
     */
    @Override
    public double maxX() {
        return this.maxX;
    }

    /**
     * @return the maximum y value of this box
     */
    @Override
    public double maxY() {
        return this.maxY;
    }

}
