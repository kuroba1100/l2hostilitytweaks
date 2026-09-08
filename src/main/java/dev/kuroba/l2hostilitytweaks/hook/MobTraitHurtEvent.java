package dev.kuroba.l2hostilitytweaks.hook;

import dev.xkmc.l2hostility.content.traits.base.MobTrait;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

/**
 * MobTrait.onHurtByOthers が走った直後に流れる。
 * amountBefore がそのTraitに触られる前の値なので、setAmount で戻せばその減算だけを打ち消せる。
 * Traitの他の挙動（蓄積・付与・反射など）はそのまま通る。
 */
public class MobTraitHurtEvent extends Event {

    private final MobTrait trait;
    private final int level;
    private final LivingEntity entity;
    private final LivingHurtEvent hurtEvent;
    private final float amountBefore;

    public MobTraitHurtEvent(MobTrait trait, int level, LivingEntity entity,
                             LivingHurtEvent hurtEvent, float amountBefore) {
        this.trait = trait;
        this.level = level;
        this.entity = entity;
        this.hurtEvent = hurtEvent;
        this.amountBefore = amountBefore;
    }

    public MobTrait getTrait() {
        return trait;
    }

    public String getTraitId() {
        return trait.getRegistryName() == null ? "" : trait.getRegistryName().toString();
    }

    public int getLevel() {
        return level;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public LivingHurtEvent getHurtEvent() {
        return hurtEvent;
    }

    public float getAmountBefore() {
        return amountBefore;
    }

    public float getAmountAfter() {
        return hurtEvent.getAmount();
    }

    /** そのTraitによる減算を無かったことにする。 */
    public void restoreAmount() {
        hurtEvent.setAmount(amountBefore);
    }
}
