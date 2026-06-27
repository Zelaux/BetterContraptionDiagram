package com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper;

import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import com.zelaux.betterdiagram.index.ForceGroups;
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
        ;
    }

    @Override
    public void mergeForces(Forces origin, Forces toAttach) {
        //double sizeWithoutFirst = (clientRopeStrand.getPoints().size() - 2) * ServerRopeStrand.SEGMENT_LENGTH;

        var ropeStat = calcRopeStat(clientRopeStrand);
        ProjectedStrandPoints points = projectedStrandPoints();
        if(!ropeStat.equalySpaced) return;
        Vector3d originSegment = ropeStat.firstSegmentDirection;
        Vector3d toAttachSegment = ropeStat.lastSegmentDirection;
        if(!startIsOrigin()) {
            Vector3d t = originSegment;
            originSegment = toAttachSegment.negate();
            toAttachSegment = t.negate();
        }
        SubLevel originSubLevel = originGroup().first();
        SubLevel toAttachSubLevel = toAttachGroup().first();
        originSegment = VecUtil.unprojectDirectionToLocal(originSubLevel.logicalPose(), originSegment);
        toAttachSegment = VecUtil.unprojectDirectionToLocal(toAttachSubLevel.logicalPose(), toAttachSegment);


        double sumOfProjected = 0;
        for(var forces : toAttach.values()) {
            for(QueuedForceGroup.PointForce force : forces) {
                sumOfProjected += force.force().dot(toAttachSegment);
            }
        }
        if(sumOfProjected < CenterMassCalculator.DELTA) return;
        origin.addForce(ForceGroups.ROPE_FORCE, new QueuedForceGroup.PointForce(points.origin, originSegment.mul(sumOfProjected)));
        /**Считать верёвку как балку, при её максимальнос растяжении, то есть при макс растяжении, превращять в rotary
         *
         * Но что делать, если она например запуталась
         *
         * Может попробоать всётаки сделать через native На сервере, типо забират ьсилу если верёвка её выдаёт, думаю это реализуемо
         * */
    }

    private static RopeStat calcRopeStat(ClientRopeStrand clientRopeStrand1) {
        double totalExtension = 0;
        ObjectArrayList<ClientRopePoint> points = clientRopeStrand1.getPoints();
        double[] lenStat = new double[points.size() - 1];

        boolean singleDirected = true;
        int lastI = points.size() - 1;
        Vector3d prev = new Vector3d(), tmp = new Vector3d(),firstSegment=new Vector3d();
        double minDistance=Double.MAX_VALUE,maxDistance=0;
        for(int i = 0; i < lastI; i++) {
            Vector3d next = points.get(i + 1).position();
            Vector3d cur = points.get(i).position();
            double cos = tmp.set(next).sub(cur).normalize().dot(prev);
            if(i == 0) {
                firstSegment.set(tmp);
            } else if(!Runtime.equals(1, cos, COS_DELTA)) {
                singleDirected = false;
            }
            prev.set(tmp);
            double distance = next.distance(cur);
            lenStat[i] = distance;
            if(i>0)
                minDistance=Math.min(distance,minDistance);
            maxDistance=Math.max(distance,maxDistance);

            totalExtension += distance;
        }

        boolean equaly = maxDistance>0 && Runtime.equals(maxDistance/minDistance,1,0.2);

        return new RopeStat(
            totalExtension,
            lenStat,
            equaly,
            singleDirected,
            maxDistance,
            minDistance,
            firstSegment,
            prev
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
        boolean equalySpaced,
        boolean singleDirected,
        double maxDistance, double minDistance, Vector3d firstSegmentDirection,
        Vector3d lastSegmentDirection
    ) {}
}
