package org.slave.mcprd;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public final class Constants {

    public static final String URL_VERSION_MANIFEST_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    public static final String URL_RESOURCE = "https://resources.download.minecraft.net/%s/%s";//(first 2 chars of hash), (full hash)

    public record Maven(String group, String name, String version) {

        public static Maven from(final String maven) {
            String[] split = maven.split(":", 3);
            return new Maven(split[0], split[1], split[2]);
        }

        public static String to(@NotNull final Maven maven) {
            return String.format("%s:%s:%s", maven.group(), maven.name(), maven.version());
        }

    }

}
