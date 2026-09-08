package dev.kuroba.l2hostilitytweaks.anchor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PatchNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("l2hostilitytweaks", "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void init() {
        CHANNEL.registerMessage(0, AnchorSyncPacket.class,
                AnchorSyncPacket::encode, AnchorSyncPacket::decode, AnchorSyncPacket::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    /** 全次元のアンカーを集めたパケットを作る */
    public static AnchorSyncPacket buildPacket(MinecraftServer server) {
        Map<ResourceLocation, int[]> map = new HashMap<>();
        for (ServerLevel level : server.getAllLevels()) {
            AnchorData data = AnchorData.get(level);
            if (data.present()) {
                map.put(level.dimension().location(), new int[]{data.x(), data.z()});
            }
        }
        return new AnchorSyncPacket(map);
    }

    public static void syncTo(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), buildPacket(player.server));
    }

    public static void syncAll(MinecraftServer server) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), buildPacket(server));
    }

    public static class AnchorSyncPacket {
        private final Map<ResourceLocation, int[]> anchors;

        public AnchorSyncPacket(Map<ResourceLocation, int[]> anchors) {
            this.anchors = anchors;
        }

        public static void encode(AnchorSyncPacket pkt, FriendlyByteBuf buf) {
            buf.writeVarInt(pkt.anchors.size());
            for (Map.Entry<ResourceLocation, int[]> e : pkt.anchors.entrySet()) {
                buf.writeResourceLocation(e.getKey());
                buf.writeVarInt(e.getValue()[0]);
                buf.writeVarInt(e.getValue()[1]);
            }
        }

        public static AnchorSyncPacket decode(FriendlyByteBuf buf) {
            int n = buf.readVarInt();
            Map<ResourceLocation, int[]> map = new HashMap<>();
            for (int i = 0; i < n; i++) {
                ResourceLocation dim = buf.readResourceLocation();
                int x = buf.readVarInt();
                int z = buf.readVarInt();
                map.put(dim, new int[]{x, z});
            }
            return new AnchorSyncPacket(map);
        }

        public static void handle(AnchorSyncPacket pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> ClientAnchorCache.setAll(pkt.anchors));
            ctx.get().setPacketHandled(true);
        }
    }
}
