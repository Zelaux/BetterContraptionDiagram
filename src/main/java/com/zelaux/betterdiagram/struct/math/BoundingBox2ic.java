package com.zelaux.betterdiagram.struct.math;


import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Contract;
import org.joml.Vector2i;
import org.joml.Vector2ic;

/**
 * A bounding box with JOML interaction.
 *
 * @since 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public sealed interface BoundingBox2ic permits BoundingBox2i {

    /**
     * @return if this box intersects with the given other box
     */
    default boolean intersects(final BoundingBox2ic other) {
        return this.intersects(other.minX(), other.minY(), other.maxX(), other.maxY());
    }


    /**
     * @return if this box intersects with the given other box
     */
    default boolean intersects(final int minX, final int minY, final int maxX, final int maxY) {
        return this.maxX() >= minX && this.maxY() >= minY && this.minX() <= maxX && this.minY() <= maxY ;
    }

    /**
     * @return if this box contains the given point
     */
    default boolean contains(final Vector2ic point) {
        return this.contains(point.x(), point.y());
    }

    /**
     * @return if this box contains the given point
     */
    default boolean contains(final int x, final int y) {
        return x >= this.minX() && x <= this.maxX() && y >= this.minY() && y <= this.maxY();
    }

    /**
     * @return if this box contains the given point
     */
    default boolean contains(final double x, final double y) {
        return x >= this.minX() && x <= this.maxX() && y >= this.minY() && y <= this.maxY();
    }

    /**
     * @return the minimum x value of this box
     */
    int minX();

    /**
     * @return the minimum y value of this box
     */
    int minY();


    /**
     * @return the maximum x value of this box
     */
    int maxX();

    /**
     * @return the maximum y value of this box
     */
    int maxY();


    /**
     * Expands this box to include the given point
     *
     * @param point the point to include
     * @return the result stored in dest
     */
    default BoundingBox2i expandTo(final Vector2ic point, final BoundingBox2i dest) {
        return this.expandTo(point.x(), point.y(),  dest);
    }

    /**
     * Expands this box to include the given point
     *
     * @param x the x value of the point
     * @param y the y value of the point
     * @return the result stored in dest
     */
    default BoundingBox2i expandTo(final int x, final int y, final BoundingBox2i dest) {
        dest.setUnchecked(this);
        dest.maxX = Math.max(dest.maxX, x);
        dest.maxY = Math.max(dest.maxY, y);
        dest.minX = Math.min(dest.minX, x);
        dest.minY = Math.min(dest.minY, y);
        return dest;
    }

    /**
     * Expands this box to include the given box
     *
     * @param other the box to include
     * @return the result stored in dest
     */
    default BoundingBox2i expandTo(final BoundingBox2ic other, final BoundingBox2i dest) {
        dest.setUnchecked(this);
        dest.maxX = Math.max(dest.maxX, other.maxX());
        dest.maxY = Math.max(dest.maxY, other.maxY());
        dest.minX = Math.min(dest.minX, other.minX());
        dest.minY = Math.min(dest.minY, other.minY());
        return dest;
    }

    /**
     * Expands this box by the given amount on all sides
     *
     * @param amount the amount to expand by
     * @param dest   the destination bounding box
     * @return the result stored in dest
     */
    default BoundingBox2i expand(final int amount, final BoundingBox2i dest) {
        return dest.setUnchecked(
            this.minX() - amount,
            this.minY() - amount,
            this.maxX() + amount,
            this.maxY() + amount);
    }

    /**
     * Expands this box by the given amount on all sides
     *
     * @param amountX the amount to expand by in the x
     * @param amountY the amount to expand by in the y
     * @param dest    the destination bounding box
     * @return the result stored in dest
     */
    default BoundingBox2i expand(final int amountX, final int amountY, final BoundingBox2i dest) {
        return dest.setUnchecked(
            this.minX() - amountX,
            this.minY() - amountY,
            this.maxX() + amountX,
            this.maxY() + amountY);
    }

    /**
     * Moves this box by the given amount on all sides
     *
     * @param amountX The amount to move by in the x
     * @param amountY The amount to move by in the y
     * @param dest    The destination bounding box
     * @return The result stored in dest
     */
    default BoundingBox2i move(final int amountX, final int amountY, final BoundingBox2i dest) {
        return dest.setUnchecked(
            this.minX() + amountX,
            this.minY() + amountY,
            this.maxX() + amountX,
            this.maxY() + amountY);
    }

    /**
     * Calculates the bounding box intersect between this box and the specified box.
     *
     * @param box  The box to intersect with
     * @param dest The destination bounding box
     * @return The result stored in dest
     */
    default BoundingBox2i intersect(final BoundingBox2ic box, final BoundingBox2i dest) {
        return dest.setUnchecked(
            Math.max(this.minX(), box.minX()),
            Math.max(this.minY(), box.minY()),
            Math.min(this.maxX(), box.maxX()),
            Math.min(this.maxY(), box.maxY())
        );
    }





    /**
     * @return the center of this box
     */
    default Vector2i center() {
        return this.center(new Vector2i());
    }

    /**
     * @return the center of this box stored in dest
     */
    default Vector2i center(final Vector2i dest) {
        return dest.set((this.minX() + this.maxX()) / 2, (this.minY() + this.maxY()) / 2);
    }

    /**
     * @return the center of this box
     */
    default Vector2i size() {
        return this.size(new Vector2i());
    }

    /**
     * @return the side length vector of this box stored in dest
     */
    default Vector2i size(final Vector2i dest) {
        return dest.set(this.maxX() - this.minX(), this.maxY() - this.minY());
    }


    /**
     * @return the encompassing X range
     * @since 1.1.0
     */
    @Contract(pure = true)
    default int width() {
        return this.maxX() - this.minX() + 1;
    }

    /**
     * @return the encompassing Y range
     * @since 1.1.0
     */
    @Contract(pure = true)
    default int height() {
        return this.maxY() - this.minY() + 1;
    }

    /**
     * @return the volume of this box
     */
    @Contract(pure = true)
    default int volume() {
        return (this.maxX() - this.minX()) * (this.maxY() - this.minY());
    }

}

