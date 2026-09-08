package dev.kuroba.l2hostilitytweaks.anchor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class AnchorData extends SavedData {
    private static final String KEY = "l2hpatch_anchor";

    private boolean present = false;
    private int x = 0;
    private int z = 0;

    public static AnchorData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(AnchorData::load, AnchorData::new, KEY);
    }

    private static AnchorData load(CompoundTag tag) {
        AnchorData data = new AnchorData();
        data.present = tag.getBoolean("present");
        data.x = tag.getInt("x");
        data.z = tag.getInt("z");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("present", present);
        tag.putInt("x", x);
        tag.putInt("z", z);
        return tag;
    }

    public boolean present() {
        return present;
    }

    public int x() {
        return x;
    }

    public int z() {
        return z;
    }

    public void set(int x, int z) {
        this.present = true;
        this.x = x;
        this.z = z;
        setDirty();
    }

    public void reset() {
        this.present = false;
        this.x = 0;
        this.z = 0;
        setDirty();
    }
}
