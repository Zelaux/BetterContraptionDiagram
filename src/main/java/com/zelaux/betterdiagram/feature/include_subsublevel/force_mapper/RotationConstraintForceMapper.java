package com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper;

import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.ConstaintKind;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.LocalConstraint;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.constraint.RotaryConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.neoforged.neoforge.common.util.Lazy;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;

public class RotationConstraintForceMapper extends LocalConstraint<RotaryConstraintConfiguration> {
    Lazy<RotaryConstraintConfiguration> projectedConfiguration = Lazy.of(() -> {
        SubLevel a = originSubLevel();
        SubLevel b = toAttachSubLevel();
        SubLevel bfirst = toAttachGroup().all().getFirst();
        SubLevel afirst = originGroup().all().getFirst();
        var myConfig = configuration;
        if(bfirst == b && afirst == a) return myConfig;
        var aPos = VecUtil.projectForce(a.logicalPose(), afirst.logicalPose(), new QueuedForceGroup.PointForce(myConfig.pos1(), myConfig.normal1()));
        var bPos = VecUtil.projectForce(b.logicalPose(), bfirst.logicalPose(), new QueuedForceGroup.PointForce(myConfig.pos2(), myConfig.normal2()));
        return new RotaryConstraintConfiguration(
            aPos.point(),
            bPos.point(),
            aPos.force(),
            bPos.force()
        );
    });

    public RotationConstraintForceMapper(RotaryConstraintConfiguration configuration, SubLevel a, SubLevel b, SubLevelGroup groupA, SubLevelGroup groupB) {
        super(configuration, ConstaintKind.Rotary, a, b, groupA, groupB);
    }

    public static void mergeForces(
        AbstractForceMapper mapper,
        Forces origin, Forces toAttach,
        Vector3dc originConnectPos, Vector3dc toAttachConnectPos,
        Vector3dc originNormal, Vector3dc toAttachNormal
    ) {
        Vector3d centerAxis = new Vector3d();
        Vector3d tangentAxis = new Vector3d();
        Vector3dc perpendicularAxis = new Vector3d(toAttachNormal);
        Vector3d leftForce = new Vector3d();
        Pose3dc originPose = mapper.originGroup().first().logicalPose();
        Pose3dc toAttachPose = mapper.toAttachGroup().first().logicalPose();
        for(var entry : toAttach.object2ObjectEntrySet()) {
            ForceGroup forceGroup = entry.getKey();
            List<QueuedForceGroup.PointForce> forces = entry.getValue();
            for(QueuedForceGroup.PointForce force : forces) {

                centerAxis.set(force.point()).sub(toAttachConnectPos);
                tangentAxis.set(perpendicularAxis).cross(centerAxis).normalize();

                //double perpendicularProj = force.force().dot(perpendicularAxis);
                //double centerProj = force.force().dot(centerAxis);
                double tangentProj = force.force().dot(tangentAxis);


                if(CenterMassCalculator.equals(tangentProj, 0)) {
                    leftForce.set(force.force());
                } else {
                    force.force().fma(-tangentProj, force.force(), leftForce);
                    origin.addProjectedForce(forceGroup,toAttachPose,originPose,
                        new QueuedForceGroup.PointForce(force.point(), tangentAxis.mul(tangentProj, new Vector3d()))
                    );
                }
                if(false && !CenterMassCalculator.equals(leftForce.lengthSquared(), 0)){
                    origin.addForce(forceGroup, new QueuedForceGroup.PointForce(force.point(), new Vector3d(leftForce)));
                }

            }
        }
    }

    @Override
    public void mergeForces(Forces origin, Forces toAttach, SubLevelDependenciesData subLevelDependenciesData) {
        RotaryConstraintConfiguration configuration1 = projectedConfiguration.get();

        mergeForces(
            this, origin, toAttach,
            configuration1.pos1(), configuration1.pos2(),
            configuration1.normal1(), configuration1.normal2()

        );
    }
}
