package dev.kuroba.l2hostilitytweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.kuroba.l2hostilitytweaks.hook.MobTraitHurtEvent;
import dev.xkmc.l2hostility.content.traits.base.MobTrait;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MobTrait.class, remap = false)
public abstract class MobTraitMixin {

    @WrapMethod(method = "onHurtByOthers", remap = false)
    private void l2ht$exposeHurt(int level, LivingEntity entity, LivingHurtEvent hurtEvent,
                                 Operation<Void> original) {
        float before = hurtEvent.getAmount();
        original.call(level, entity, hurtEvent);

        if (hurtEvent.getAmount() != before) {
            MobTrait self = (MobTrait) (Object) this;
            MinecraftForge.EVENT_BUS.post(new MobTraitHurtEvent(self, level, entity, hurtEvent, before));
        }
    }
}
