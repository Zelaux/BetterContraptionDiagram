package com.zelaux.betterdiagram.mixin.feature.rope_data;

import com.zelaux.betterdiagram.feature.rope_data.IRopeData;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import org.spongepowered.asm.mixin.*;

@Mixin(ClientRopeStrand.class)
public class RopeData_ClientRopeStrand implements IRopeData {
    @Unique
    private double bcd$extension_goal=-1;

    @Override
    public double bcd$extension_goal() {
        return bcd$extension_goal;
    }

    @Override
    public void bcd$extension_goal(double extension_goal) {
        this.bcd$extension_goal = extension_goal;
    }
}
