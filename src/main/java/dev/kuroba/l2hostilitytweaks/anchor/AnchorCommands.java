package dev.kuroba.l2hostilitytweaks.anchor;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.concurrent.ThreadLocalRandom;

/** /hostility anchor set|reset|get — 本家L2Hのコマンドツリー(/hostility)に合流する */
public class AnchorCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hostility")
                .then(Commands.literal("anchor")
                        .then(Commands.literal("set")
                                .executes(ctx -> set(ctx.getSource(), BlockPos.containing(ctx.getSource().getPosition())))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> set(ctx.getSource(), BlockPosArgument.getBlockPos(ctx, "pos")))))
                        .then(Commands.literal("reset")
                                .requires(s -> s.hasPermission(2))
                                .executes(ctx -> reset(ctx.getSource())))
                        .then(Commands.literal("get")
                                .executes(ctx -> get(ctx.getSource())))
                        .then(Commands.literal("rtp")
                                .executes(ctx -> rtp(ctx.getSource())))));
    }

    /**
     * アンカー未設定の次元でのみ使える片道ランダムテレポート。
     * 条件: 地表(MOTION_BLOCKING_NO_LEAVES)着地・足元が固体（水/溶岩リロール）・
     * 地表YがrtpMaxY以下（浮島/飛行構造物/薄空気圏の山頂を除外）。
     */
    private static int rtp(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("rtp requires a player"));
            return 0;
        }
        ServerLevel level = source.getLevel();
        if (level.dimensionType().hasCeiling()) {
            source.sendFailure(Component.literal("rtp is not available in dimensions with a ceiling"));
            return 0;
        }
        if (AnchorData.get(level).present()) {
            source.sendFailure(Component.literal(
                    "The anchor for " + level.dimension().location() + " is already set - rtp is permanently disabled here"));
            return 0;
        }
        int half = AnchorConfig.RTP_RANGE.get() / 2;
        int maxY = AnchorConfig.RTP_MAX_Y.get();
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < 32; attempt++) {
            int x = rand.nextInt(-half, half + 1);
            int z = rand.nextInt(-half, half + 1);
            level.getChunk(x >> 4, z >> 4);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y > maxY || y <= level.getMinBuildHeight()) {
                continue;
            }
            BlockPos ground = new BlockPos(x, y - 1, z);
            BlockState groundState = level.getBlockState(ground);
            if (!groundState.getFluidState().isEmpty()) {
                continue;
            }
            if (!level.getBlockState(ground.above()).getFluidState().isEmpty()) {
                continue;
            }
            player.stopRiding();
            player.teleportTo(level, x + 0.5, y, z + 0.5, player.getYRot(), player.getXRot());
            final int fx = x, fy = y, fz = z;
            source.sendSuccess(() -> Component.literal(
                    "Teleported to (" + fx + ", " + fy + ", " + fz + ")"), true);
            return 1;
        }
        source.sendFailure(Component.literal("Could not find a valid landing spot, try again"));
        return 0;
    }

    private static int set(CommandSourceStack source, BlockPos pos) {
        ServerLevel level = source.getLevel();
        if (AnchorData.get(level).present()) {
            source.sendFailure(Component.literal(
                    "The anchor for " + level.dimension().location() + " is already set - it can only be set once"
                            + " (admins can /hostility anchor reset)"));
            return 0;
        }
        AnchorData.get(level).set(pos.getX(), pos.getZ());
        PatchNetwork.syncAll(source.getServer());
        ServerPlayer player = source.getPlayer();
        String spawnNote = "";
        if (player != null) {
            player.setRespawnPosition(level.dimension(), pos, player.getYRot(), true, false);
            spawnNote = " (spawn point fixed here)";
        }
        final String note = spawnNote;
        source.sendSuccess(() -> Component.literal(
                "Difficulty anchor for " + level.dimension().location() + " set to (" + pos.getX() + ", " + pos.getZ() + ")" + note
                        + (AnchorConfig.enabled() ? "" : " [WARNING: enableAnchor is false in l2hostilitytweaks-common.toml]")), true);
        return 1;
    }

    private static int reset(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        AnchorData.get(level).reset();
        PatchNetwork.syncAll(source.getServer());
        source.sendSuccess(() -> Component.literal(
                "Difficulty anchor for " + level.dimension().location() + " reset to origin (0, 0)"), true);
        return 1;
    }

    private static int get(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        AnchorData data = AnchorData.get(level);
        String state = data.present() ? "(" + data.x() + ", " + data.z() + ")" : "not set (origin 0,0)";
        source.sendSuccess(() -> Component.literal(
                "Difficulty anchor for " + level.dimension().location() + ": " + state
                        + " | enableAnchor=" + AnchorConfig.enabled()), false);
        return 1;
    }
}
