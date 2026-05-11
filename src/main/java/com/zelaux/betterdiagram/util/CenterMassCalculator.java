package com.zelaux.betterdiagram.util;

import com.zelaux.betterdiagram.Config;
import com.zelaux.betterdiagram.extend.DataKeys;
import com.zelaux.betterdiagram.extend.DiagramScreenAccessors;
import com.zelaux.betterdiagram.extend.DiagramStickyNoteAccessors;
import com.zelaux.betterdiagram.extend.WithClientData;
import com.zelaux.betterdiagram.struct.MassStack;
import com.zelaux.betterdiagram.struct.Weights;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramStickyNote;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import org.jetbrains.annotations.NotNull;
import org.joml.Runtime;
import org.joml.Vector3d;

import java.util.Objects;

import static com.zelaux.betterdiagram.util.VecUtil.minVec3d;

public class CenterMassCalculator {
    public static final WithClientData.Key<Data> cacheKey = new WithClientData.Key<>();
    public static final float DELTA = 0.00001f;

    public static Weights @NotNull [] makeMassStacks() {
        return new Weights[]{new Weights(), new Weights(), new Weights()};
    }

    public static Vector3d centerOfMass(ClientSubLevel subLevel) {
        return new Vector3d(subLevel.logicalPose().rotationPoint())
            .sub(minVec3d(subLevel.getPlot().getBoundingBox()));
    }

    public static Weights[] recalculateStacks(DiagramScreen screen) {
        var accessors = accessors(screen);
        DiagramDataPacket diagramDataPacket = accessors.betterContraptionDiagram$serverData();
        return recalculateStacks(( accessors.betterContraptionDiagram$clientData()), screen.subLevel, diagramDataPacket == null ? 0 : diagramDataPacket.mass());
    }

    public static Weights[] recalculateStacks(WithClientData clientData, ClientSubLevel subLevel, double mass) {

        Vector3d expectedCOM = expectedCenterOfMass(clientData, subLevel);
        Vector3d currentCOM = centerOfMass(subLevel);
        if(expectedCOM.equals(currentCOM, DELTA) || mass == 0) {
            clientData.betterContraptionDiagram$deleteClientData(DataKeys.MASS_STACKS);
            clientData.betterContraptionDiagram$deleteClientData(cacheKey);
            return null;
        }
        Data data = clientData.betterContraptionDiagram$getClientData(cacheKey);
        var stacks = clientData.betterContraptionDiagram$getClientData(DataKeys.MASS_STACKS, CenterMassCalculator::makeMassStacks);

        if(data != null && data.isApproxEquals(expectedCOM, currentCOM, mass)) {
            return stacks;
        }


        clientData.betterContraptionDiagram$putClientData(cacheKey, Data.update(data, expectedCOM, currentCOM, mass));


        stacks[0].clear();
        stacks[1].clear();
        stacks[2].clear();

        Vector3d diff = expectedCOM.sub(currentCOM, new Vector3d());
        diff.mul(mass);

        diff.mul(4);
        //diff.round();

        //diff.div(4);

        addStacks(stacks[0], VecUtil.X_V, expectedCOM, diff.x);
        addStacks(stacks[1], VecUtil.Y_V, expectedCOM, diff.y);
        addStacks(stacks[2], VecUtil.Z_V, expectedCOM, diff.z);

        return stacks;
    }

    private static void addStacks(Weights weights, Vector3d uni, Vector3d expectedCOM, double value0) {
        weights.totalWeight=value0;
        long value= (long) Math.floor(value0);
        final int maxIterations = Config.MAX_ITERATION.getAsInt();
        final int maxFixDistance = Config.MAX_FIX_DISTANCE.getAsInt();

        long sig = value0 < 0 ? -1 : 1;
        if(equals(Math.abs(value0-value),1)) value += sig;
        else if(!Runtime.equals(value0,value,0.0001f)){
            double v1 = (value0-value)/4f;

            int[] scales={1,2,4,8};
            //0.25 * x = 1*N+v1
            //0.25 * x -1*N = +v1
            //0.25 * (x -4*N) = +v1
            long sigV1 = v1 < 0 ? -1 : 1;
            v1=Math.abs(v1);
            double dst0 = v1 / 0.25;

            smallStack:
            {
                /*for(int scale : scales) {
                    double dst = dst0 * scale;
                    if(dst > maxFixDistance) break;
                    if(Runtime.equals(dst, (int) dst, 0.001f)) {
                        weights.smallStacks.add(
                            new MassStack(
                                uni.mul(dst * sig, new Vector3d()).add(expectedCOM),
                                (v1 * 4 * scale) / 4f)
                        );
                        break smallStack;
                    }
                }*/

                var unit = uni.mul(sigV1, new Vector3d());
                for(double dst = dst0;dst<maxFixDistance && v1<Math.abs(value0);dst+=4,v1+=1){
                    weights.smallStacks.add(new MassStack(new Vector3d(unit).mul(dst).add(expectedCOM), 0.25));
                }
                //value--;
            }

        }
        if(value == 0) return;
        long dst = 1;
        //double sigHalf = value < 0 ? -0.5 : 0.5;
        value *= sig;
        long v = value;
        int iteration = 0;
        while(v > 0) {
            iteration++;
            if(iteration > maxIterations) break;
            weights.stacks.add(
                new MassStack(uni.mul(dst * sig, new Vector3d()).add(expectedCOM), v / 4f)
            );
            do {
                dst++;
                if(dst>maxFixDistance)return;
            } while(value % dst != 0 && dst < value);

            v = value / dst;
        }
    }

    public static Vector3d expectedCenterOfMass(DiagramScreen screen) {
        return expectedCenterOfMass((accessors(screen).betterContraptionDiagram$clientData()), screen.subLevel);
    }

    public static DiagramScreenAccessors accessors(DiagramScreen screen) {return (DiagramScreenAccessors) screen;}
    public static DiagramStickyNoteAccessors accessors(DiagramStickyNote screen) {return (DiagramStickyNoteAccessors) screen;}

    public static Vector3d expectedCenterOfMass(WithClientData clientData, ClientSubLevel subLevel) {
        return clientData.betterContraptionDiagram$getClientData(DataKeys.EXPECTED_CENTER_OF_MASS, () ->
            centerOfMass(subLevel)
        );
    }

    public static boolean equals(double v1, double v2){
        return Runtime.equals(v1,v2,DELTA);
    }
    public static boolean equals(Vector3d COM1, Vector3d COM2) {
        if(COM1 == null) return COM2 == null;
        return COM1.equals(COM2, DELTA);
    }

    public static class Data {
        public final Vector3d expectedCOM = new Vector3d();
        public final Vector3d actualCOM = new Vector3d();
        public double mass;

        public Data() {

        }

        public Vector3d expectedCOM() {return expectedCOM;}

        public Vector3d actualCOM() {return actualCOM;}

        public double mass() {return mass;}

        public Data set(Vector3d expectedCOM, Vector3d actualCOM, double mass) {
            this.expectedCOM.set(expectedCOM);
            this.actualCOM.set(actualCOM);
            this.mass = mass;
            return this;
        }

        public static Data update(Data data, Vector3d expectedCOM, Vector3d currentCOM, double mass) {

            return Objects.requireNonNullElseGet(data, Data::new).set(expectedCOM, currentCOM, mass);
        }

        private boolean isApproxEquals(Vector3d expectedCOM, Vector3d currentCOM, double mass) {
            return this.expectedCOM.equals(expectedCOM, DELTA) && actualCOM.equals(currentCOM, DELTA) && Runtime.equals(mass, this.mass, DELTA);
        }
    }
}
