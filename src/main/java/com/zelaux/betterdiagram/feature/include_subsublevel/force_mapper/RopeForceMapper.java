package com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper;

import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import com.zelaux.betterdiagram.feature.rope_data.IRopeData;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import com.zelaux.betterdiagram.index.SomeConstants;
import com.zelaux.betterdiagram.network.rope.RopeDataRequest;
import com.zelaux.betterdiagram.util.CenterMassCalculator;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopePoint;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.joml.Runtime;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;

import static com.zelaux.betterdiagram.util.VecUtil.projectPos;

/**
 * @see ClientRopeStrand
 * @see ServerRopeStrand
 *
 */
@Getter
public class RopeForceMapper extends AbstractForceMapper {
    private static final double COS_DELTA = 0.01;
    final boolean startIsOrigin;
    private final ClientRopeStrand clientRopeStrand;

    public RopeForceMapper(SubLevelInfo info, ClientRopeStrand clientRopeStrand, boolean startIsOrigin) {
        super(info);
        this.startIsOrigin = startIsOrigin;
        this.clientRopeStrand = clientRopeStrand;
    }

    //todo test how it works with ropes with different lengths
    @Override
    public AbstractForceMapper tryMergeWith(AbstractForceMapper other) {
        if(this.toAttachGroup() != other.toAttachGroup() || originGroup() != other.originGroup()) {
            return null;
        }
        if(other instanceof RopeForceMapper otherRopeMapper) {
            var mapper = new GroupOfRopesForceMapper(sublevelInfo());
            mapper.mergedRopes.add(this);
            mapper.mergedRopes.add(otherRopeMapper);
            return mapper;
        } else if(other instanceof GroupOfRopesForceMapper group) {
            return group.tryMergeWith(this);
        }
        return null;
    }

    @Override
    public void mergeForces(Forces origin, Forces toAttach, SubLevelDependenciesData subLevelDependenciesData) {
        RopeForces ropeForces = calculateForces(origin, toAttach, subLevelDependenciesData);
        if(ropeForces.ropeForce != null) {
            origin.addForce(BCDForceGroups.ROPE_FORCE, ropeForces.ropeForce);
        }
        if(ropeForces.ropeMassForce != null) {
            origin.addForce(BCDForceGroups.ROPE_MASS_FORCE, ropeForces.ropeMassForce);
        }
    }

    public RopeForces calculateForces(Forces origin, Forces toAttach, SubLevelDependenciesData subLevelDependenciesData) {
        //double sizeWithoutFirst = (clientRopeStrand.getPoints().size() - 2) * ServerRopeStrand.SEGMENT_LENGTH;

        RopeDataRequest.requestRope(clientRopeStrand);
        var ropeStat = calcRopeStat(clientRopeStrand);
        ProjectedStrandPoints points = projectedStrandPoints();
        List<Vector3d> segmentDirections = ropeStat.segmentDirections;
        int lastI = segmentDirections.size() - 1;
        int firstSegmentI = 0, lastSegmentI = ropeStat.equalySpacedIndexFromStart, segmentStep = 1;
        if(!startIsOrigin()) {
            firstSegmentI = ropeStat.equalySpacedIndexFromEnd;
            lastSegmentI = 0;
            segmentStep = -1;
        }
        Vector3d originSegment = segmentDirections.get(firstSegmentI);
        Vector3d toAttachSegment = segmentDirections.get(lastSegmentI);
        SubLevel originSubLevel = originGroup().first();
        SubLevel toAttachSubLevel = toAttachGroup().first();
        originSegment = VecUtil.unprojectDirectionToLocal(originSubLevel.logicalPose(), new Vector3d(originSegment));
        toAttachSegment = VecUtil.unprojectDirectionToLocal(toAttachSubLevel.logicalPose(), new Vector3d(toAttachSegment));
        if(!startIsOrigin()) {
            originSegment.negate();
            toAttachSegment.negate();
        }

        QueuedForceGroup.PointForce ropeForce = null;
        if(lastI == ropeStat.equalySpacedIndexFromStart) {
            ropeForce = applyRopeForce(origin, originSegment, toAttach, toAttachSegment, points);
        }

        QueuedForceGroup.PointForce ropeMassForce = applyRopeMass(originSubLevel, originSegment, origin, segmentDirections, firstSegmentI, lastSegmentI, segmentStep, points);
        return new RopeForces(
            ropeForce,
            ropeMassForce
        );
    }

    public record RopeForces(
        @Nullable
        QueuedForceGroup.PointForce ropeForce,
        @Nullable
        QueuedForceGroup.PointForce ropeMassForce
    ) {}

    private QueuedForceGroup.PointForce applyRopeMass(SubLevel originSubLevel, Vector3d originSegment, Forces origin, List<Vector3d> segmentDirections, int firstSegmentI, int lastSegmentI, int segmentStep, ProjectedStrandPoints points) {
        Vector3dc GRAVITY = SomeConstants.gravityVector(originSubLevel.getLevel());
        Vector3d singlePointGravityForce = GRAVITY.mul(SomeConstants.ropePointMass, new Vector3d());
        if(!startIsOrigin()) singlePointGravityForce.negate();
        double totalPointMass = 0;

        for(int i = lastSegmentI; i != firstSegmentI - segmentStep; i -= segmentStep) {
            //Vector3d prev = segmentDirections.get(i+segmentStep);
            Vector3d current = segmentDirections.get(i);

            totalPointMass += Math.max(0, singlePointGravityForce.dot(current));
        }
        totalPointMass += Math.max(0, singlePointGravityForce.dot(segmentDirections.get(firstSegmentI)));
        if(CenterMassCalculator.equals3(totalPointMass, 0)) return null;
        Vector3d mul = originSegment.mul(totalPointMass);
        //origin point
        if(!startIsOrigin()) singlePointGravityForce.negate();
        //mul.add(VecUtil.unprojectDirectionToLocal(originSubLevel.logicalPose(), singlePointGravityForce));
        return new QueuedForceGroup.PointForce(points.origin, mul);
    }

    private QueuedForceGroup.PointForce applyRopeForce(Forces origin, Vector3d originSegment, Forces toAttach, Vector3d toAttachSegment, ProjectedStrandPoints points) {
        double sumOfProjected = 0;
        for(var forces : toAttach.values()) {
            for(QueuedForceGroup.PointForce force : forces) {
                sumOfProjected += force.force().dot(toAttachSegment);
            }
        }
        if(sumOfProjected > CenterMassCalculator.DELTA) {
            QueuedForceGroup.PointForce pointForce = new QueuedForceGroup.PointForce(new Vector3d(points.origin), originSegment.mul(sumOfProjected, new Vector3d()));
            return pointForce;
        }
        return null;
    }

    private static RopeStat calcRopeStat(ClientRopeStrand clientRopeStrand1) {
        double totalExtension = 0;
        ObjectArrayList<ClientRopePoint> points = clientRopeStrand1.getPoints();
        double[] lenStat = new double[points.size() - 1];

        boolean singleDirected = true;
        int equalySpacedIndex = -1, equalySpacedIndex2 = -1;
        int lastI = points.size() - 1;
        Vector3d prev = new Vector3d(), tmp = new Vector3d();
        var directions = new ObjectArrayList<Vector3d>();
        double minDistance = Double.MAX_VALUE, maxDistance = 0;
        boolean wasEqualy = true;
        double extensionGoal = ((IRopeData) clientRopeStrand1).bcd$extension_goal();
        if(extensionGoal >= 0) {
            double length = tmp.set(points.get(1).position()).sub(points.get(0).position()).length();
            totalExtension = 0;
            minDistance = length / extensionGoal;
        }
        for(int i = 0; i < lastI; i++) {
            Vector3d next = points.get(i + 1).position();
            Vector3d cur = points.get(i).position();
            double cos = tmp.set(next).sub(cur).normalize().dot(prev);
            directions.add(new Vector3d(tmp));
            if(i == 0) {
            } else if(!Runtime.equals(0, cos, COS_DELTA)) {
                singleDirected = false;
            }
            prev.set(tmp);
            double distance = next.distance(cur);
            lenStat[i] = distance;
            if(i > 0) {minDistance = Math.min(distance, minDistance);
                maxDistance = Math.max(distance, maxDistance);
            }else if(extensionGoal>0){
                maxDistance = Math.max(distance/extensionGoal, maxDistance);

            }

            totalExtension += distance;

            boolean equaly = minDistance == Double.MAX_VALUE || maxDistance > 0 && Runtime.equals(maxDistance / minDistance, 1, 0.2);
            if(!equaly && wasEqualy && equalySpacedIndex == -1) {
                equalySpacedIndex = i;
            }
            if(equaly && !wasEqualy) {
                equalySpacedIndex2 = i;
            }
            wasEqualy = equaly;
        }

        return new RopeStat(
            totalExtension,
            lenStat,
            equalySpacedIndex < 0 ? lastI - 1 : equalySpacedIndex,
            equalySpacedIndex2 < 0 ? lastI - 1 : equalySpacedIndex2,
            singleDirected,
            maxDistance,
            minDistance,
            directions
        );
    }

    private ProjectedStrandPoints projectedStrandPoints() {
        SubLevel start, end;
        SubLevelGroup startGroup, endGroup;

        var startAttachment = JOMLConversion.toJOML(clientRopeStrand().startAttachment);
        var endAttachment = JOMLConversion.toJOML(clientRopeStrand().endAttachment);
        int originI = 0;
        if(startIsOrigin()) {
            start = originSubLevel();
            startGroup = originGroup();

            end = toAttachSubLevel();
            endGroup = toAttachGroup();
        } else {
            start = toAttachSubLevel();
            startGroup = toAttachGroup();

            end = originSubLevel();
            endGroup = originGroup();
            originI = 1;
        }
        Vector3d[] tmp = {
            projectPos(startGroup.first().logicalPose(), start.logicalPose(), startAttachment),
            projectPos(endGroup.first().logicalPose(), end.logicalPose(), endAttachment),
        };
        return new ProjectedStrandPoints(
            tmp[0], tmp[1], tmp[originI], tmp[1 - originI]
        );

    }

    record ProjectedStrandPoints(Vector3d start, Vector3d end, Vector3d origin, Vector3d toAttach) {}

    private record RopeStat(
        double totalExtension,
        double[] lenStat,
        int equalySpacedIndexFromStart, int equalySpacedIndexFromEnd,
        boolean singleDirected,
        double maxDistance, double minDistance,
        List<Vector3d> segmentDirections
    ) {}
}
