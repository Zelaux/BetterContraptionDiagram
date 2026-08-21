package com.zelaux.betterdiagram.mixin.feature.create_contraption_mass;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.zelaux.betterdiagram.feature.create_contraption_mass.ClientLevelPlotExt;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.KinematicContraption;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/**
 * @see dev.ryanhcode.sable.neoforge.mixin.compatibility.create.contraptions.AbstractContraptionEntityMixin
 *
 */
@Mixin(value = AbstractContraptionEntity.class, priority = 2000)
public abstract class AbstractContraptionEntityMixin extends Entity implements KinematicContraption {

    @Unique
    private boolean bcd$client$sable$initialized = false;

    public AbstractContraptionEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Dynamic
    @Shadow
    private void sable$buildProperties() {}

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"), remap = false)
    private void sable$contraptionInitialize(final CallbackInfo ci) {
        if(!this.bcd$client$sable$initialized && this.level() instanceof ClientLevel) {
            this.sable$buildProperties();
            this.bcd$client$sable$initialized = true;
            bcd$sable$addToPlot();
        }
    }

    private void bcd$sable$addToPlot() {
        final SubLevel subLevel = Sable.HELPER.getContaining(this);

        if(subLevel instanceof final ClientSubLevel clientSubLevel) {

            ((ClientLevelPlotExt) clientSubLevel.getPlot()).addContraption(this);
        }
    }

    @Unique
    private void bcd$sable$removeFromPlot() {
        final SubLevel subLevel = Sable.HELPER.getContaining(this);

        if(subLevel instanceof final ClientSubLevel clientSubLevel) {
            ((ClientLevelPlotExt) clientSubLevel.getPlot()).removeContraption(this);
        }
    }

    /**
     * @see dev.ryanhcode.sable.neoforge.mixin.compatibility.create.contraptions.AbstractContraptionEntityMixin#setRemoved(RemovalReason)
     */
    @Inject(method = "setRemoved", at = @At("RETURN"))
    public void setRemoved(final RemovalReason removalReason, final CallbackInfo ci) {
        if(this.level() instanceof ClientLevel) {
            this.bcd$sable$removeFromPlot();
        }
    }
}
