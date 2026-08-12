package dev.silvergold.simulatedcoasters.track.cart;

import com.zelaux.betterdiagram.annotations.SilentDelete;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
//import dev.silvergold.simulatedcoasters.client.cart.CoasterCartTrainLinkClientState;
//import dev.silvergold.simulatedcoasters.track.cart.CoasterCartResolve;
//import dev.silvergold.simulatedcoasters.track.cart.CoasterCartTrainLinkData;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

@SilentDelete
public final class CoasterCartTrainLinkSync {
    private CoasterCartTrainLinkSync() {
    }

    public record CartTrainLinkSyncPayload(ResourceKey<Level> dimension, UUID cartId, List<LinkEntry> links) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<CartTrainLinkSyncPayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"simulatedcoasters", (String)"cart_train_link_sync"));
        private static final StreamCodec<RegistryFriendlyByteBuf, LinkEntry> LINK_CODEC = StreamCodec.composite((StreamCodec)UUIDUtil.STREAM_CODEC, LinkEntry::partnerId, (StreamCodec)ByteBufCodecs.DOUBLE, LinkEntry::centerDistance, LinkEntry::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, CartTrainLinkSyncPayload> STREAM_CODEC = StreamCodec.composite((StreamCodec)ResourceKey.streamCodec((ResourceKey)Registries.DIMENSION), CartTrainLinkSyncPayload::dimension, (StreamCodec)UUIDUtil.STREAM_CODEC, CartTrainLinkSyncPayload::cartId, (StreamCodec)LINK_CODEC.apply(ByteBufCodecs.list()), CartTrainLinkSyncPayload::links, CartTrainLinkSyncPayload::new);

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record LinkEntry(UUID partnerId, double centerDistance) {
     /*   static LinkEntry from(CoasterCartTrainLinkData.LinkRecord record) {
            return new LinkEntry(record.partnerId(), record.centerDistance());
        }*/
    }
}
