package com.zelaux.betterdiagram.feature.balloon_volume_calculator.widget;

import com.zelaux.betterdiagram.struct.math.BoundingBox2i;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2d;
import org.joml.Vector2d;

public abstract class OnGraphWidget extends AbstractSimiWidget {

    private final Matrix3x2d tmpMat=new Matrix3x2d();

    public OnGraphWidget(int x, int y, int width, int height, Component message, GraphBackground background) {
        super(x, y, width, height, message);
        this.background = background;
    }

    public OnGraphWidget(GraphBackground background, Component message) {
        super(0,0,0,0,message);
        this.background = background;
    }

    final GraphBackground background;
    private final Vector2d tmp = new Vector2d();

    public Matrix3x2d projectMatrixGrid2Draw(Matrix3x2d matrix) {
        return flipProjectMatrix(matrix, background.gridDimension, background.drawableRegion);
    }

    private Matrix3x2d flipProjectMatrix(Matrix3x2d matrix, BoundingBox2i from, BoundingBox2i top) {
        return matrix
            .translateLocal(from.minCorner(tmp).negate())
            .scaleLocal(top.width() / (float) from.width(), top.height() / (float) from.height())
            .scaleLocal(1, -1)
            .translateLocal(0, top.height())
            .translateLocal(top.minCorner(tmp));
    }
    public Vector2d projectToScreen(Vector2d position){
        return position.mulPosition(projectMatrixGrid2Draw(tmpMat.identity()));
    }
    public Vector2d projectToGrid(Vector2d position){
        return position.mulPosition(projectMatrixDraw2Grid(tmpMat.identity()));
    }
    public Matrix3x2d projectMatrixDraw2Grid(Matrix3x2d matrix) {
        return flipProjectMatrix(matrix, background.drawableRegion, background.gridDimension);
    }
}
