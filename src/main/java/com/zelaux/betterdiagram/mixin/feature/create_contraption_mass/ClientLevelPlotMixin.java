package com.zelaux.betterdiagram.mixin.feature.create_contraption_mass;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.zelaux.betterdiagram.feature.create_contraption_mass.ClientLevelPlotExt;
import dev.ryanhcode.sable.api.sublevel.KinematicContraption;
import dev.ryanhcode.sable.sublevel.plot.ClientLevelPlot;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Mixin(ClientLevelPlot.class)
public class ClientLevelPlotMixin implements ClientLevelPlotExt {

    private final List<KinematicContraption> bcd$contraptions =new ObjectArrayList<>();

    @Override
    @Unique
    public List<KinematicContraption> bcd$getContraptions() {
        return bcd$contraptions;
    }

}
