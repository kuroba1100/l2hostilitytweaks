package dev.kuroba.l2hostilitytweaks.anchor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Mixinハンドラから呼ばれる距離基準点の解決。未設定・機能OFFなら(0,0)＝素の挙動。 */
public class AnchorLookup {

    public static int anchorX(Level level) {
        int[] a = resolve(level);
        return a == null ? 0 : a[0];
    }

    public static int anchorZ(Level level) {
        int[] a = resolve(level);
        return a == null ? 0 : a[1];
    }

    private static int[] resolve(Level level) {
        if (level == null || !AnchorConfig.enabled()) {
            return null;
        }
        if (level instanceof ServerLevel serverLevel) {
            AnchorData data = AnchorData.get(serverLevel);
            return data.present() ? new int[]{data.x(), data.z()} : null;
        }
        return ClientAnchorCache.get(level.dimension().location());
    }
}
