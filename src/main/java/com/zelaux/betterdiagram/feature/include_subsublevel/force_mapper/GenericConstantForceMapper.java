package com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper;

import com.zelaux.betterdiagram.extend.accessors.ProjectionAccessor;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ConstaintKind;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.LocalConstraint;
import com.zelaux.betterdiagram.feature.include_subsublevel.math.ConstraintCalculator;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import com.zelaux.betterdiagram.gui.screen.diagram_ext.CustomRenderedForce_Vector3d;
import com.zelaux.betterdiagram.index.BForceGroups;
import com.zelaux.betterdiagram.struct.BCDForceGroup;
import com.zelaux.betterdiagram.util.MergedPointForceCalculator;
import com.zelaux.betterdiagram.util.VecUtil;
import com.zelaux.betterdiagram.util.mock.MockGenericConstraintHandle;
import com.zelaux.betterdiagram.util.ui.GuiGraphicsUtil;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.ForceClusterFinder;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import org.apache.commons.lang3.DoubleRange;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;

public class GenericConstantForceMapper extends LocalConstraint<GenericConstraintConfiguration> {


    public BCDForceGroup forceGroup;
    public ConstraintCalculator calculator;
    private MergedPointForceCalculator merger = new MergedPointForceCalculator();
    public Map<ConstraintJointAxis, DoubleRange> ranges;

    public GenericConstantForceMapper(SubLevelInfo info, MockGenericConstraintHandle handle) {
        this(info, handle.config, handle.ranges);
    }

    public GenericConstantForceMapper(SubLevelInfo info, GenericConstraintConfiguration config, @Nullable Map<ConstraintJointAxis, DoubleRange> ranges) {
        this(info,config,ranges,BForceGroups.LINKAGE_FORCE);
    }

    public GenericConstantForceMapper(SubLevelInfo info, GenericConstraintConfiguration config, @Nullable Map<ConstraintJointAxis, DoubleRange> ranges, BCDForceGroup forceGroup) {
        super(info, config, ConstaintKind.Generic);
        this.ranges = ranges;
        calculator = ConstraintCalculator.INSTANCE;
        this.forceGroup = forceGroup;
    }

    @Override
    public LocalConstraint<GenericConstraintConfiguration> flip() {
        var config = (GenericConstraintConfiguration) flipConfig(configuration);

        return new GenericConstantForceMapper(
            sublevelInfo().flipped(),
            config,
            ranges
        );
    }

    @Override
    public void mergeForces(Forces origin, Forces toAttach, SubLevelDependenciesData subLevelDependenciesData) {
        SubLevel bSublevel = toAttachGroup().first();
        SubLevel aSublevel = originGroup().first();

        Pose3d aPose = aSublevel.logicalPose();
        Pose3d bPose = bSublevel.logicalPose();

        merger.begin(bPose.rotationPoint());
        toAttach.forEachForce(merger::add);
        merger.endPlusCOM();


        Vector3d force = new GenericForceRenderer(aPose, bPose, configuration);
        Vector3d point = new Vector3d();

        if(configuration.lockedAxes().size()==ConstraintJointAxis.ALL.length){//fixed
            VecUtil.projectForce(aPose,bPose,
                point.set(merger.displacement),
                force.set(merger.totalForce)
            );
        }else {
            calculateRealGeneric(aPose, bPose, force, point);
        }
        origin.addForce(forceGroup,
            new QueuedForceGroup.PointForce(point, force)
        );
    }

    private void calculateRealGeneric(Pose3d aPose, Pose3d bPose, Vector3d force, Vector3d point) {
        GenericConstraintConfiguration config = configuration;

        calculator.generic(
            aPose,
            bPose,
            config.pos1(),
            config.pos2(),
            config.orientation1(),
            config.orientation2(),
            new QueuedForceGroup.PointForce(merger.displacement, merger.totalForce),
            config.lockedAxes(),
            ranges
        );


        calculator.getForceAndPoint(aPose, force, point);
    }

    @AllArgsConstructor

    public static class GenericForceRenderer extends CustomRenderedForce_Vector3d {
        Pose3d aPose;
        Pose3d bPose;
        GenericConstraintConfiguration config;
        private final Vector2d tmp2 = new Vector2d();

        public GenericForceRenderer() {}

        @Override
        public void onHover(GuiGraphics graphics, ProjectionAccessor projectionAccessor, int mouseX, int mouseY, List<FormattedText> tooltipLines, ForceClusterFinder.Cluster pointForce, ForceGroup forceGroup) {
            if(aPose == null) return;
            Vector3d tmp3 = new Vector3d();
            Vector2d screen1 = projectionAccessor.bcd$getScreenCoords(tmp3.set(config.pos1()));

            //tmp3.set(config.pos2());
            //bPose.transformPosition(tmp3);
            aPose.transformPositionInverse(tmp3.set(bPose.position()));
            Vector2d screen2 = projectionAccessor.bcd$getScreenCoords(tmp3);

            float maxLineLen = projectionAccessor.bcd$viewportRadius() * 0.5f * 0.25f;
            float segmentSize = maxLineLen / 10;

            Vector2d direction = tmp2;
            double dst = screen2.sub(screen1, direction).length();
            direction.mul(1/dst);
            int amountOfSegments = (int) (dst / segmentSize);
            int color = forceGroup.color() | 0xff_000000;
            float progress = 0;

            for(int i = 0; i < amountOfSegments; i += 2) {
                double x1 = screen1.x + direction.x * progress;
                double y1 = screen1.y + direction.y * progress;
                progress += segmentSize;
                double x2 = screen1.x + direction.x * progress;
                double y2 = screen1.y + direction.y * progress;
                progress += segmentSize;
                GuiGraphicsUtil.renderLine(
                    graphics,
                    x1, y1, x2, y2,
                    color
                );
            }

        }

        @Override
        public void onRender(GuiGraphics graphics, ForceClusterFinder.Cluster pointForce, ForceGroup forceGroup, int mouseX, int mouseY, double maxArrowLength, ProjectionAccessor projectionAccessor, boolean isDot) {
            if(aPose == null) return;
        }
    }
}
