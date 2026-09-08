package dev.kuroba.l2hostilitytweaks.anchor;

import net.minecraftforge.common.ForgeConfigSpec;

public class AnchorConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ANCHOR;
    public static final ForgeConfigSpec.IntValue RTP_RANGE;
    public static final ForgeConfigSpec.IntValue RTP_MAX_Y;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        ENABLE_ANCHOR = b
                .comment("Enable difficulty anchor override.",
                        "When true, L2Hostility distance-based difficulty is measured from the anchor",
                        "set via /hostility anchor set (per dimension) instead of the world origin (0,0).",
                        "When false (default), behavior is identical to vanilla L2Hostility.")
                .define("enableAnchor", false);
        RTP_RANGE = b
                .comment("Side length of the square area (centered on origin) for /hostility anchor rtp.",
                        "rtp is only usable while the dimension's anchor is NOT set; setting the anchor",
                        "permanently disables rtp for that dimension.")
                .defineInRange("rtpRange", 10000, 100, 1000000);
        RTP_MAX_Y = b
                .comment("Reject rtp landing spots whose surface is above this Y.",
                        "Filters out floating islands / airship structures / thin-air peaks.")
                .defineInRange("rtpMaxY", 160, 0, 320);
        SPEC = b.build();
    }

    public static boolean enabled() {
        try {
            return ENABLE_ANCHOR.get();
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
