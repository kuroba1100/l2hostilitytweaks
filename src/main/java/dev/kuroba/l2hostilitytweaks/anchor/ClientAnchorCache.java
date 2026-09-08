package dev.kuroba.l2hostilitytweaks.anchor;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/** クライアント側のアンカー写し（サーバーからの同期パケットで更新）。 */
public class ClientAnchorCache {
    private static final Map<ResourceLocation, int[]> ANCHORS = new HashMap<>();

    public static synchronized void setAll(Map<ResourceLocation, int[]> anchors) {
        ANCHORS.clear();
        ANCHORS.putAll(anchors);
    }

    public static synchronized int[] get(ResourceLocation dim) {
        return ANCHORS.get(dim);
    }
}
