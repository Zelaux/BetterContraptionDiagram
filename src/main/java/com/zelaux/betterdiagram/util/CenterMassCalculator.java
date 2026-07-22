package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.BAccessors;
import com.zelaux.betterdiagram.extend.ClientData;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.feature.balance.BalanceForce;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.feature.include_subsublevel.struct.Forces;
import com.zelaux.betterdiagram.index.BForceGroups;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.neoforged.neoforge.common.util.strategy.IdentityStrategy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Runtime;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.Set;

public class CenterMassCalculator {
    public static float DELTA = 0.0001f;
    public static final Set<ForceGroup> FORCE_GROUP_BLACKLIST;

    static {
        var forceGroupBlacklist = new ObjectOpenCustomHashSet<ForceGroup>(IdentityStrategy.IDENTITY);
        //forceGroupBlacklist.add(ForceGroups.GRAVITY.get());
        forceGroupBlacklist.add(BForceGroups.MERGED_FORCE_GROUP.group);
        forceGroupBlacklist.add(BForceGroups.CONTRAPTION_GRAVITY.group);

        FORCE_GROUP_BLACKLIST = forceGroupBlacklist;
    }

    public static @NotNull Vector3dc centerOfMass(SubLevel subLevel) {
        return subLevel.logicalPose().rotationPoint();
    }


    public static Vector3dc expectedCenterOfMass(DiagramScreen screen) {
        return expectedCenterOfMass((BAccessors.accessors(screen).bcd$clientData()), screen.subLevel);
    }

    @NotNull
    public static Vector3dc expectedCenterOfMass(WithClientData clientData, ClientSubLevel subLevel) {
        BCDData data = clientData.bcdDataOrTryDefault();
        if(data == null || data.eCOM() == null) return centerOfMass(subLevel);
        return data.eCOM();
    }

    public static void expectedCenterOfMass(ClientData clientData, Vector3d newValue) {
        BCDData data = clientData.bcdDataOrTryDefault();
        if(data == null) return;
        clientData.bcd$updateData(
            newValue == null ? data.withECOM(null) : data.withECOM(new Vector3d(newValue))
        );
    }

    public static boolean equals(double v1, double v2) {
        return Runtime.equals(v1, v2, DELTA);
    }

    public static boolean equals3(double v1, double v2) {
        return Runtime.equals(v1, v2, 0.001f);
    }

    public static boolean equals2(double v1, double v2) {
        return Runtime.equals(v1, v2, 0.01f);
    }

    public static boolean equals(Vector3dc COM1, Vector3dc COM2) {
        if(COM1 == null) return COM2 == null;
        return COM1.equals(COM2, DELTA);
    }


    public static Vector3d locateMergedCenterOfMass(Vector3d COM, SubLevelDependenciesData dependenciesData, @NotNull BCDData data) {
        if(dependenciesData == null || !data.shouldMergedForcesWithConnectedSublevels) return COM;
        SubLevelGroup levelGroup = dependenciesData.constraintTree.subLevelGroup;
        Forces forces = dependenciesData.collectFixedForces(levelGroup);
        if(forces == null) return COM;
        List<QueuedForceGroup.PointForce> pointForces = forces.get(ForceGroups.GRAVITY.get());
        if(pointForces == null) return COM;

        return COM.set(pointForces.getFirst().point());
    }

    public static @NotNull BCDData checkCache(BCDData data, BalanceForce balanceForce, boolean[] shouldUpdate) {
        Cache cache = data.cache;
        if(cache == null || !cache.isApproxEquals(data.eCOM(), balanceForce.displacement(), balanceForce.mass())) {
            data = data.withCache(Cache.create(data.eCOM(), balanceForce.displacement(), balanceForce.mass()));
            shouldUpdate[0] = true;
        } else {
            shouldUpdate[0] = false;
        }
        return data;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Cache {
        @Nullable
        public final Vector3dc expectedCOM;
        @NotNull
        public final Vector3dc actualCOM;
        public double mass;


        @Contract(value = "_,_,_->new")
        public static Cache create(Vector3dc expectedCOM, Vector3dc currentCOM, double mass) {
            return new Cache(expectedCOM, new Vector3d(currentCOM), mass);
        }

        private boolean isApproxEquals(Vector3dc expectedCOM, Vector3dc currentCOM, double mass) {
            return (expectedCOM == this.expectedCOM || this.expectedCOM != null && this.expectedCOM.equals(expectedCOM, DELTA)) && actualCOM.equals(currentCOM, DELTA) && Runtime.equals(mass, this.mass, DELTA);
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof Cache cache)) return false;
            return isApproxEquals(cache.expectedCOM, cache.actualCOM, cache.mass);
        }
    }
}
