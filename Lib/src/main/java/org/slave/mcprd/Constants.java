package org.slave.mcprd;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public final class Constants {

    public static final String URL_VERSION_MANIFEST_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    public static final String URL_RESOURCE = "https://resources.download.minecraft.net/%s/%s";//(first 2 chars of hash), (full hash)

    public static final Boolean DEBUG;

    public static final String OS_NAME, OS_VERSION, OS_ARCH;

    public record Maven(String group, String name, String version) {

        public static Maven from(final String maven) {
            String[] split = maven.split(":", 3);
            return new Maven(split[0], split[1], split[2]);
        }

        public static String to(@NotNull final Maven maven) {
            return String.format("%s:%s:%s", maven.group(), maven.name(), maven.version());
        }

    }

    static {
        DEBUG = Boolean.valueOf(
                System.getProperty("org.slave.mcprd.debug", Boolean.FALSE.toString())
        );

        OS_NAME = System.getProperty("os.name");
        OS_VERSION = System.getProperty("os.version");
        OS_ARCH = System.getProperty("os.arch");
    }

}
