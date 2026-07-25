package com.zelaux.betterdiagram.feature.balloon_volume_calculator;

import com.zelaux.betterdiagram.data.BCDData;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.feature.balance.BalanceModel;
import com.zelaux.betterdiagram.feature.include_subsublevel.SubLevelDependenciesData;
import com.zelaux.betterdiagram.util.VecUtil;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

public class BalloonVolumeCalculator {
    private static final Vector3d tmpPos = new Vector3d();
    WithClientData clientData;
    SubLevelDependenciesData dependenciesData;
    Balloons balloons;
    DiagramEntity diagram;

    public BalloonVolumeCalculator(SubLevelDependenciesData dependenciesData, Balloons balloons, DiagramEntity diagram) {
        set(dependenciesData, balloons, diagram);
    }

    public BalloonVolumeCalculator(SubLevelDependenciesData dependenciesData,  DiagramEntity diagram) {
        set(dependenciesData,  diagram);
    }

    public void set(SubLevelDependenciesData dependenciesData,  DiagramEntity diagram) {
        set(dependenciesData,new Balloons(dependenciesData),diagram);
    }
    public void set(SubLevelDependenciesData dependenciesData, Balloons balloons, DiagramEntity diagram) {
        this.dependenciesData = dependenciesData;
        this.balloons = balloons;
        this.diagram = diagram;
        this.clientData = (WithClientData) diagram;
    }

    public float calculateVolume(Level level, LiftingGasType gasType, float y) {
        BCDData bcdData = clientData.bcdDataNotNull_readOnly();
        float searchY = bcdData.balloonCalc.calculationMode().locateWorldY(Minecraft.getInstance().player, dependenciesData, balloons, diagram);
        float balloonY = (float) tryLocateBalloonsCenter(balloons);
        //double mass = packet == null ? 0 : packet.mass();
        double mass = BalanceModel.mergedBalancePointOrCOM(dependenciesData,clientData).mass();
        if(Double.isNaN(balloonY)) return calculateVolumeRaw(level, gasType, y, mass);
        return calculateVolumeRaw(level, gasType, y + (balloonY - searchY), mass);

    }

    public static float calculateVolumeRaw(Level level, LiftingGasType gasType, float y, double mass) {
        double pressure = DimensionPhysicsData.getAirPressure(level, tmpPos.set(0, y, 0));
        if(pressure==0 || level.getMaxBuildHeight()<y)return -1;
        //force = gravity * totalLift * pressure
        //double totalLift = force / pressure / gravityScalar;
        double totalLift = mass / pressure;
        //totalLift = volume * getLiftStrength
        return/*volume = */ (float) (totalLift / gasType.getLiftStrength());
    }

    public static double tryLocateBalloonsCenter(Iterable<BalloonEntry> balloons) {
        double commonY = 0;
        int amountY = 0;

        Vector3d pos = tmpPos;
        for(BalloonEntry balloon : balloons) {
            var gasCenterInfo = balloon.gasCenterInfo;
            if(gasCenterInfo == null) return Double.NaN;
            gasCenterInfo.getGasCenterInto(pos);
            VecUtil.projectToWorld(balloon.subLevel.logicalPose(), pos);
            commonY += pos.y;
            amountY++;
        }
        if(amountY == 0) return Double.NaN;
        commonY /= amountY;
        return commonY;
    }

    public double balloonsY() {
        var balloons = this.balloons.asList();
        if(balloons==null || balloons.isEmpty())return Double.NaN;
        return tryLocateBalloonsCenter(balloons);
    }
}
