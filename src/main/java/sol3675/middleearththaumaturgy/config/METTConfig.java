package sol3675.middleearththaumaturgy.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * The donor's {@code MeetCfg}, trimmed to the options the port still honours.
 *
 * <p>The capacities are read when a jar is queried rather than at registration time, so a config
 * reload applies without restarting; a datapack entry for the same storage id still wins, because
 * {@code EssentiaStorageApi.capacity} checks the reloadable map first.
 */
public final class METTConfig {
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue COMPRESSED_JARS;
    private static final ForgeConfigSpec.BooleanValue EXTRALARGE_JAR;
    private static final ForgeConfigSpec.IntValue COMPRESSED_JAR_CAPACITY;
    private static final ForgeConfigSpec.IntValue X2_COMPRESSED_JAR_CAPACITY;
    private static final ForgeConfigSpec.IntValue EXTRALARGE_JAR_CAPACITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Compressed essentia jars").push("jars");
        COMPRESSED_JARS = builder.comment("Enable the compressed and x2 compressed jars")
                .define("compressedJars", true);
        EXTRALARGE_JAR = builder.comment("Enable the extralarge jar")
                .define("extralargeJar", true);
        COMPRESSED_JAR_CAPACITY = builder.comment("Capacity of the compressed jar")
                .defineInRange("compressedJarCapacity", 512, 1, 1_000_000_000);
        X2_COMPRESSED_JAR_CAPACITY = builder.comment("Capacity of the x2 compressed jar")
                .defineInRange("x2CompressedJarCapacity", 4096, 1, 1_000_000_000);
        EXTRALARGE_JAR_CAPACITY = builder.comment("Capacity of the extralarge jar")
                .defineInRange("extralargeJarCapacity", 1_000_000_000, 1, 1_000_000_000);
        builder.pop();
        SPEC = builder.build();
    }

    private METTConfig() {
    }

    public static int compressedJarCapacity() {
        return COMPRESSED_JAR_CAPACITY.get();
    }

    public static int x2CompressedJarCapacity() {
        return X2_COMPRESSED_JAR_CAPACITY.get();
    }

    public static int extralargeJarCapacity() {
        return EXTRALARGE_JAR_CAPACITY.get();
    }

    public static boolean compressedJars() {
        return COMPRESSED_JARS.get();
    }

    public static boolean extralargeJar() {
        return EXTRALARGE_JAR.get();
    }
}
