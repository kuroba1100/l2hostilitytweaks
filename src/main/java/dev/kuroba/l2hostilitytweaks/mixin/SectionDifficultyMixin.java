package dev.kuroba.l2hostilitytweaks.mixin;

import dev.kuroba.l2hostilitytweaks.anchor.AnchorLookup;
import dev.xkmc.l2hostility.content.logic.MobDifficultyCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * L2Hostilityの距離難易度の基準点を原点(0,0)から可変アンカーに差し替える。
 * 距離計算は modifyInstanceInternal（実計算）と getSectionDifficultyDetail（表示）の
 * sqrt(x^2+z^2) のみで、両メソッド内のBlockPos.getX/getZは距離計算にしか使われていない
 * （2026-07-19 バイトコード確認済み）。アンカー未設定・機能OFF時は0を引くだけ＝完全に素の挙動。
 */
@Mixin(targets = "dev.xkmc.l2hostility.content.capability.chunk.SectionDifficulty", remap = false)
public abstract class SectionDifficultyMixin {

    @Redirect(method = "modifyInstanceInternal",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;m_123341_()I"))
    private int l2ht$relX(BlockPos pos, Level level, BlockPos posArg, MobDifficultyCollector collector) {
        return pos.getX() - AnchorLookup.anchorX(level);
    }

    @Redirect(method = "modifyInstanceInternal",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;m_123343_()I"))
    private int l2ht$relZ(BlockPos pos, Level level, BlockPos posArg, MobDifficultyCollector collector) {
        return pos.getZ() - AnchorLookup.anchorZ(level);
    }

    @Redirect(method = "getSectionDifficultyDetail",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;m_123341_()I"))
    private int l2ht$relXDetail(BlockPos pos, Player player) {
        return pos.getX() - AnchorLookup.anchorX(player.level());
    }

    @Redirect(method = "getSectionDifficultyDetail",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;m_123343_()I"))
    private int l2ht$relZDetail(BlockPos pos, Player player) {
        return pos.getZ() - AnchorLookup.anchorZ(player.level());
    }
}
