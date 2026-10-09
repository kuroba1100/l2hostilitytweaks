package dev.kuroba.l2hostilitytweaks.mixin;

import dev.xkmc.l2hostility.content.capability.player.PlayerDifficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(targets = "dev.xkmc.l2hostility.content.logic.PlayerFinder", remap = false)
public abstract class PlayerFinderMixin {

    private static final double SEARCH_RANGE_SQR = 128.0 * 128.0;

    @Inject(method = "getNearestPlayer", at = @At("HEAD"), cancellable = true)
    private static void l2ht$highestDifficulty(Level level, LivingEntity entity, CallbackInfoReturnable<Player> cir) {
        Player highest = null;
        int highestLevel = Integer.MIN_VALUE;
        for (Player player : level.players()) {
            if (player.distanceToSqr(entity) > SEARCH_RANGE_SQR || !player.isAlive()) continue;
            Optional<PlayerDifficulty> cap = player.getCapability(PlayerDifficulty.CAPABILITY).resolve();
            if (cap.isEmpty()) continue;
            int difficulty = cap.get().getLevel().getLevel();
            if (difficulty > highestLevel) {
                highest = player;
                highestLevel = difficulty;
            }
        }
        cir.setReturnValue(highest);
    }
}
