package com.zelaux.betterdiagram.feature.include_subsublevel.force_mapper;

import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import com.zelaux.betterdiagram.index.BForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import org.joml.Vector3d;

/**
 * @see ClientRopeStrand
 * @see ServerRopeStrand
 *
 */
@Getter
public class GroupOfRopesForceMapper extends AbstractForceMapper {
    final ObjectList<RopeForceMapper> mergedRopes = new ObjectArrayList<>();

    public GroupOfRopesForceMapper(SubLevelInfo info) {
        super(info);
    }

    @Override
    public AbstractForceMapper tryMergeWith(AbstractForceMapper other) {
        if(this.toAttachGroup() != other.toAttachGroup() || originGroup() != other.originGroup()) {
            return null;
        }
        if(other instanceof RopeForceMapper otherRopeMapper) {
            mergedRopes.add(otherRopeMapper);
            return this;
        } else if(other instanceof GroupOfRopesForceMapper group) {
            mergedRopes.addAll(group.mergedRopes);
            return this;
        } else return null;
    }

    @Override
    public void mergeForces(Forces origin, Forces toAttach, SubLevelDependenciesData subLevelDependenciesData) {
        var pointForces = new ObjectArrayList<QueuedForceGroup.PointForce>();
        for(var mapper : mergedRopes) {
            var ropeForces = mapper.calculateForces(origin, toAttach, subLevelDependenciesData);
            if(ropeForces.ropeForce() != null) {
                pointForces.add(ropeForces.ropeForce());
            }
            if(ropeForces.ropeMassForce() != null) {
                origin.addForce(BForceGroups.ROPE_MASS_FORCE, ropeForces.ropeMassForce());
            }
        }
        for(QueuedForceGroup.PointForce pointForce : pointForces) {
            ((Vector3d) pointForce.force()).div(pointForces.size());
            origin.addForce(BForceGroups.ROPE_FORCE, pointForce);
        }
    }
}
