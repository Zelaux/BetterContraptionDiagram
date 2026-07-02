package com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper;

import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import com.zelaux.betterdiagram.index.BCDForceGroups;
import com.zelaux.betterdiagram.index.SomeConstants;
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

    @Override
    public void mergeForces(Forces origin, Forces toAttach, SubLevelDependenciesData subLevelDependenciesData) {
        //double sizeWithoutFirst = (clientRopeStrand.getPoints().size() - 2) * ServerRopeStrand.SEGMENT_LENGTH;

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
        if(lastI != ropeStat.equalySpacedIndexFromStart) {
            applyRopeForce(origin, originSegment, toAttach, toAttachSegment, points);
        }
        applyRopeMass(originSubLevel, originSegment, origin, segmentDirections, firstSegmentI, lastSegmentI, segmentStep, points);
        /**Считать верёвку как балку, при её максимальнос растяжении, то есть при макс растяжении, превращять в rotary
         *
         * Но что делать, если она например запуталась
         *
         * Может попробоать всётаки сделать через native На сервере, типо забират ьсилу если верёвка её выдаёт, думаю это реализуемо
         * */
    }

    private void applyRopeMass(SubLevel originSubLevel, Vector3d originSegment, Forces origin, List<Vector3d> segmentDirections, int firstSegmentI, int lastSegmentI, int segmentStep, ProjectedStrandPoints points) {
        Vector3dc GRAVITY = SomeConstants.gravityVector(originSubLevel.getLevel());
        Vector3d singlePointGravityForce = GRAVITY.mul(SomeConstants.ropePointMass, new Vector3d());
        if(!startIsOrigin()) singlePointGravityForce.negate();
        double totalPointMass = 0;

        for(int i = lastSegmentI; i != firstSegmentI - segmentStep; i -= segmentStep) {
            //Vector3d prev = segmentDirections.get(i+segmentStep);
            Vector3d current = segmentDirections.get(i);

            totalPointMass += Math.max(0, singlePointGravityForce.dot(current));
        }
        Vector3d mul = originSegment.mul(totalPointMass);
        //origin point
        if(!startIsOrigin()) singlePointGravityForce.negate();
        mul.add(VecUtil.unprojectDirectionToLocal(originSubLevel.logicalPose(), singlePointGravityForce));
        origin.addForce(BCDForceGroups.ROPE_MASS_FORCE, new QueuedForceGroup.PointForce(points.origin, mul));
    }

    private void applyRopeForce(Forces origin, Vector3d originSegment, Forces toAttach, Vector3d toAttachSegment, ProjectedStrandPoints points) {
        double sumOfProjected = 0;
        for(var forces : toAttach.values()) {
            for(QueuedForceGroup.PointForce force : forces) {
                sumOfProjected += force.force().dot(toAttachSegment);
            }
        }
        if(sumOfProjected > CenterMassCalculator.DELTA) {
            origin.addForce(BCDForceGroups.ROPE_FORCE, new QueuedForceGroup.PointForce(new Vector3d(points.origin), originSegment.mul(sumOfProjected, new Vector3d())));
        }
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
        for(int i = 0; i < lastI; i++) {
            Vector3d next = points.get(i + 1).position();
            Vector3d cur = points.get(i).position();
            double cos = tmp.set(next).sub(cur).normalize().dot(prev);
            directions.add(new Vector3d(tmp));
            if(i == 0) {
            } else if(!Runtime.equals(1, cos, COS_DELTA)) {
                singleDirected = false;
            }
            prev.set(tmp);
            double distance = next.distance(cur);
            lenStat[i] = distance;
            if(i > 0) {minDistance = Math.min(distance, minDistance);}
            maxDistance = Math.max(distance, maxDistance);

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
