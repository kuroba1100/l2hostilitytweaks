package dev.kuroba.l2hostilitytweaks;

import dev.kuroba.l2hostilitytweaks.anchor.AnchorCommands;
import dev.kuroba.l2hostilitytweaks.anchor.AnchorConfig;
import dev.kuroba.l2hostilitytweaks.anchor.PatchNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(L2HostilityTweaks.MODID)
public class L2HostilityTweaks {
    public static final String MODID = "l2hostilitytweaks";

    public L2HostilityTweaks() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AnchorConfig.SPEC);
        PatchNetwork.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AnchorCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PatchNetwork.syncTo(player);
        }
    }
}
