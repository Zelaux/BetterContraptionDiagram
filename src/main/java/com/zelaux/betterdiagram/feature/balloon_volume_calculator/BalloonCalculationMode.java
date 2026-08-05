package com.zelaux.betterdiagram.feature.balloon_volume_calculator;

import com.mojang.serialization.Codec;
import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.feature.balance.CenterMassMerger;
import com.zelaux.betterdiagram.feature.client_mass_tracker.MassTrackerLevelPlotI;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.feature.include_subsublevel.connector.SubLevelGroup;
import com.zelaux.betterdiagram.network.features.ModFeature;
import com.zelaux.betterdiagram.util.RaycastHelper;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.api.physics.mass.MassTracker;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.Optional;

@Getter
public enum BalloonCalculationMode {
    bottomOfSublevel("bottom_of_sublevel", (player, dependenciesData, balloons, diagram, sink) -> {
        BCDData data = ((WithClientData) diagram).bcdDataNotNull_readOnly();
        double y = Double.MAX_VALUE;
        for(SubLevel level : dependenciesData.mergedMainGroup(data)) {
            y = Math.min(y, level.boundingBox().minY());
        }
        return sink.worldY(y);
    }),
    topOfSublevel("top_of_sublevel", (player, dependenciesData, balloons, diagram, sink) -> {
        BCDData data = ((WithClientData) diagram).bcdDataNotNull_readOnly();
        double y = -Double.MAX_VALUE;
        for(SubLevel level : dependenciesData.mergedMainGroup(data)) {
            y = Math.max(y, level.boundingBox().maxY());
        }
        return sink.worldY(y);
    }),
    COMOfSublevel("com_of_sublevel", (player, dependenciesData, balloons, diagram, sink) -> {
        BCDData data = ((WithClientData) diagram).bcdDataNotNull_readOnly();
        DiagramDataPacket forces = dependenciesData.mergedForces(data);
        SubLevel mainSubLevel = dependenciesData.mainSubLevel();
        if(forces != null) {
            List<QueuedForceGroup.PointForce> list = forces.forces().get(ForceGroups.GRAVITY.get());
            if(list != null && !list.isEmpty()) {
                return sink.subLevel(list.getFirst().point(), mainSubLevel);
            }
        }
        SubLevelGroup subLevels = dependenciesData.mergedMainGroup(data);
        if(!ModFeature.SHARE_MASS_TRACKER.supportsOnServer() || subLevels.single()) {
            return sink.worldY(mainSubLevel.logicalPose().position().y);
        }

        CenterMassMerger merger = new CenterMassMerger();
        for(SubLevel level : subLevels) {
            MassTracker massTracker = ((MassTrackerLevelPlotI) level.getPlot()).bcd$massTracker();
            merger.add(level.logicalPose().position(), massTracker.getMass());
        }
        merger.finish();
        return sink.worldY(merger.centerOfMass.y);
    }),
    centerOfSublevel("center_of_sublevel", (player, dependenciesData, balloons, diagram, sink) -> {
        BCDData data = ((WithClientData) diagram).bcdDataNotNull_readOnly();
        SubLevelGroup subLevels = dependenciesData.mergedMainGroup(data);
        Vector3d sum = new Vector3d();
        for(SubLevel level : subLevels) {
            sum.add(level.boundingBox().center(sink.tmp));
        }

        return sink.worldY(sum.y / subLevels.amount());
    }),

    diagramPosition("diagram_position", (player, dependenciesData, balloons, diagram, sink) -> {
        SubLevel subLevel = Sable.HELPER.getContaining(diagram);
        Vector3d pos = JOMLConversion.toJOML(diagram.getPosition(AnimationTickHolder.getPartialTicks()), sink.tmp);
        return sink.subLevel(pos, subLevel);
    }),
    playerPosition("player_position", (player, dependenciesData, balloons, diagram, sink) ->
        sink.worldY(player.getY())),

    centerOfBalloons("center_of_balloons",
        (player, dependenciesData, balloons, diagram, sink) ->
            sink.worldY(BalloonVolumeCalculator.tryLocateBalloonsCenter(balloons))),
    aboveTheGround("above_the_ground",
        (player, dependenciesData, balloons, diagram, sink) ->
            hit(diagram, sink,VecUtil.NY_V, false, dependenciesData, bottomOfSublevel, diagram.level().getMinBuildHeight())
    ),
    aboveTheGroundOrLiquid("above_the_ground_or_liquid",
        (player, dependenciesData, balloons, diagram, sink) ->
            hit(diagram, sink,VecUtil.NY_V, true, dependenciesData, bottomOfSublevel, diagram.level().getMinBuildHeight())),

    ;

    private static YValue hit(DiagramEntity diagram, CalcSink sink, Vector3d set, boolean hitLiquid, SubLevelDependenciesData dependenciesData, BalloonCalculationMode edgeOfSublevel, int onMiss) {
        Level level = diagram.level();
        HitResult hitResult = RaycastHelper.getHitResult(
            level,
            Sable.HELPER.projectOutOfSubLevel(level,diagram.position()),
            set,
            (float) Math.max(Math.abs(diagram.getY() - level.getMinBuildHeight()), diagram.getY() - level.getMaxBuildHeight()),
            diagram,
            hitLiquid,
            false,
            false);

        double v = hitResult.getType() == HitResult.Type.MISS ? onMiss : hitResult.getLocation().y+Math.copySign(1,set.y);

        var worldY = edgeOfSublevel.locate.locateY(null, dependenciesData, null, diagram,sink);
        worldY.y-=v;
        return worldY;
    }


    public static final Codec<Optional<BalloonCalculationMode>> CODEC = Codec.STRING.xmap(
        x -> Optional.of(CalcSink.map.get(x)),
        x -> x.map(BalloonCalculationMode::identifier).orElse("\n")
    );
    public final static BalloonCalculationMode[] all = values();
    public final int id = ordinal();
    private final String identifier;
    private final LocalLocateY locate;


    BalloonCalculationMode(String identifier, LocalLocateY locate) {
        this.locate = locate;
        this.identifier = identifier;
        CalcSink.map.put(identifier, this);
    }

    public Component displayName() {
        return Component.translatable("balloon_calculation_mode." + identifier);
    }

    public float locateWorldY(Player player, SubLevelDependenciesData dependenciesData, Balloons balloons, DiagramEntity diagram) {
        return (float) locate.locateY(player, dependenciesData, balloons, diagram, LocalLocateY.sink).y;
    }

    private interface LocalLocateY {
        CalcSink sink = new CalcSink();

        YValue locateY(Player player, SubLevelDependenciesData dependenciesData, Balloons balloons, DiagramEntity diagram, CalcSink sink);
    }

    public static class YValue {
        double y;

        public YValue set(double y) {
            this.y = y;
            return this;
        }
    }

    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static class CalcSink {
        public static final Object2ObjectOpenHashMap<String, BalloonCalculationMode> map = new Object2ObjectOpenHashMap<>();
        Vector3d tmp = new Vector3d();
        Vector3d tmp2 = new Vector3d();

        YValue tmpYValue = new YValue();

        YValue subLevel(Vector3dc pos, SubLevel subLevel) {
            if(subLevel == null) return tmpYValue.set(pos.y());
            return tmpYValue.set(
                VecUtil.projectToWorld(subLevel.logicalPose(), tmp2.set(pos))
                    .y);
        }

        YValue worldY(double y) {
            return tmpYValue.set(y);
        }

    }
}

