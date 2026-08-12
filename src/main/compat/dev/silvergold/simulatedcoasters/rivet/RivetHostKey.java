package dev.silvergold.simulatedcoasters.rivet;

import com.zelaux.betterdiagram.annotations.SilentDelete;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@SilentDelete
public record RivetHostKey(@Nullable UUID hostSubLevelId, BlockPos cell) {
    public RivetHostKey {
        cell = cell.immutable();
    }

    public static RivetHostKey of(Level level, BlockPos cell) {
        SubLevel sub = Sable.HELPER.getContaining(level, cell);
        if(sub != null && !sub.isRemoved()) {
            return new RivetHostKey(sub.getUniqueId(), cell);
        }
        return new RivetHostKey(null, cell);
    }

    public static boolean isRivetSubLevel(SubLevel sub) {
        return false;
    }

    //public static boolean isRivetSubLevel(SubLevel sub) {
    //    return sub.getPlot().getEmbeddedLevelAccessor().getBlockState(BlockPos.ZERO).is((Block)SimulatedCoastersBlocks.RIVET.get());
    //}

    public static RivetHostKey world(BlockPos cell) {
        return new RivetHostKey(null, cell.immutable());
    }
}
