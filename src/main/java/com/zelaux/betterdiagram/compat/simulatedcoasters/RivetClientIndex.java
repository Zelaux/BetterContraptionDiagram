package com.zelaux.betterdiagram.compat.simulatedcoasters;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.silvergold.simulatedcoasters.rivet.RivetAnchorKey;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class RivetClientIndex {
    public static boolean remove(BlockEntity self, @Nullable RivetAnchorKey anchorKey, Function<ILevelPlot, Object2ObjectMap<RivetAnchorKey, WeakReference<BlockEntity>>> rivetsGetter, UUID sublevelUuid) {
        if(anchorKey == null) return false;
        Level level = self.getLevel();
        if(level == null) return false;
        SubLevelContainer container = SubLevelContainer.getContainer(level);
        if(container == null) return false;
        SubLevel subLevel = container.getSubLevel(sublevelUuid);
        if(subLevel != null && subLevel.getPlot() instanceof ILevelPlot plot) {
            Object2ObjectMap<RivetAnchorKey, WeakReference<BlockEntity>> map = rivetsGetter.apply(plot);
            WeakReference<BlockEntity> removed = map.remove(anchorKey);
            if(removed != null) {
                BlockEntity blockEntity = removed.get();
                if(blockEntity != self) {
                    map.put(anchorKey, removed);
                    return false;
                }
            }

        }
        return true;

    }

    public static boolean add(BlockEntity self, @Nullable RivetAnchorKey anchorKey, Function<ILevelPlot, Object2ObjectMap<RivetAnchorKey, WeakReference<BlockEntity>>> rivetsGetter, UUID uuid) {
        if(anchorKey == null) return false;
        Level level = self.getLevel();
        if(level == null) return false;
        SubLevelContainer container = SubLevelContainer.getContainer(level);
        if(container == null) return false;
        SubLevel subLevel = container.getSubLevel(uuid);
        if(subLevel != null && subLevel.getPlot() instanceof ILevelPlot plot) {
            Object2ObjectMap<RivetAnchorKey, WeakReference<BlockEntity>> map = rivetsGetter.apply(plot);
            WeakReference<BlockEntity> old = map.put(anchorKey, new WeakReference<>(self));
            if(old != null) {
                BlockEntity oldEntity = old.get();
                if(oldEntity != null && !oldEntity.getBlockPos().equals(self.getBlockPos())) {
                    if(oldEntity instanceof IRivetEntity rivet) {
                        rivet.bcd$prevKey(null);
                    }
                }
            }
            return true;
        }
        return false;

    }

    @FunctionalInterface
    interface Handle {
        boolean handle(BlockEntity self, @Nullable RivetAnchorKey anchorKey,
                       Function<ILevelPlot, Object2ObjectMap<RivetAnchorKey, WeakReference<BlockEntity>>> rivetsGetter,
                       UUID sublevelUuid);
    }

    public static RivetAnchorKey bindHost_tail(BlockEntity self, RivetAnchorKey rivetHostKey, RivetAnchorKey bcd$prevSubLevel) {
        return rebind(self, rivetHostKey, bcd$prevSubLevel);
    }

    public static RivetAnchorKey bindHost_head(BlockEntity self, RivetAnchorKey rivetHostKey, RivetAnchorKey bcd$prevSubLevel) {
        return bcd$prevSubLevel;
    }

    public static RivetAnchorKey loadAdditional(BlockEntity self, RivetAnchorKey newAnchorKey, RivetAnchorKey oldAnchorKey) {
        return rebind(self, newAnchorKey, oldAnchorKey);
    }

    private static @Nullable RivetAnchorKey rebind(BlockEntity self, @Nullable RivetAnchorKey newAnchorKey, @Nullable RivetAnchorKey oldAnchorKey) {
        Level level = self.getLevel();
        if(level == null) return null;
        process(self, oldAnchorKey, RivetClientIndex::remove);
        if(self instanceof IRivetEntity rivet) {
            SubLevel subLevel = Sable.HELPER.getContaining(self);
            rivet.bcd$locatedSublevel(subLevel == null ? null : subLevel.getUniqueId());
        }
        if(process(self, newAnchorKey, RivetClientIndex::add)) {
            return newAnchorKey;
        }
        return null;
    }

    private static boolean process(BlockEntity self, RivetAnchorKey anchorKey, Handle handle) {
        IRivetEntity rivet = (IRivetEntity) self;
        boolean v = false;
        if(anchorKey != null) {
            v = handle.handle(self, anchorKey, ILevelPlot::bcd$connectedRivets, anchorKey.host().hostSubLevelId());
        }
        UUID uuid = rivet.bcd$locatedSublevel();
        if(uuid != null) handle.handle(self, anchorKey, ILevelPlot::bcd$locatedInRivets, uuid);
        return v;
    }

    public static RivetAnchorKey setRemoved(BlockEntity self, RivetAnchorKey rivetAnchorKey, RivetAnchorKey bcd$prevKey) {
        process(self, bcd$prevKey, (self1, anchorKey, rivetsGetter, sublevelUuid) -> remove(self1, anchorKey, rivetsGetter, sublevelUuid));
        if(self instanceof IRivetEntity rivet) rivet.bcd$locatedSublevel(null);
        return null;
    }


    public static boolean hasRivets(SubLevel subLevel) {
        if(subLevel.getPlot() instanceof ILevelPlot plot) {
            return !plot.bcd$connectedRivets().isEmpty() || !plot.bcd$locatedInRivets().isEmpty();
        }
        return false;
    }

    public static void locatedRivets(SubLevel subLevel, Consumer<IRivetEntity> consumer) {
        if(!(subLevel.getPlot() instanceof ILevelPlot plot)) return;
        forEachRivet(plot.bcd$locatedInRivets(), consumer);
    }

    public static void connectedRivets(SubLevel subLevel, Consumer<IRivetEntity> consumer) {
        if(!(subLevel.getPlot() instanceof ILevelPlot plot)) return;
        forEachRivet(plot.bcd$connectedRivets(), consumer);
    }

    private static void forEachRivet(Object2ObjectMap<RivetAnchorKey, WeakReference<BlockEntity>> map, Consumer<IRivetEntity> consumer) {
        for(ObjectIterator<Map.Entry<RivetAnchorKey, WeakReference<BlockEntity>>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<RivetAnchorKey, WeakReference<BlockEntity>> entry = iterator.next();
            if(!(entry.getValue().get() instanceof IRivetEntity rivet)) {
                iterator.remove();
                continue;
            }
            consumer.accept(rivet);
        }
    }

    public interface ILevelPlot {
        Object2ObjectMap<RivetAnchorKey, WeakReference<BlockEntity>> bcd$connectedRivets();

        Object2ObjectMap<RivetAnchorKey, WeakReference<BlockEntity>> bcd$locatedInRivets();
    }

    public interface IRivetEntity extends IBlockEntityExtension {

        UUID bcd$locatedSublevel();

        void bcd$locatedSublevel(UUID bcd$oldSublevel);

        RivetAnchorKey bcd$prevKey();

        void bcd$prevKey(RivetAnchorKey bcd$prevKey);

        RivetAnchorKey anchorKey();
    }
}
