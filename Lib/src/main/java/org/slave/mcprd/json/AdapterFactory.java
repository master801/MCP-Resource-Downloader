package org.slave.mcprd.json;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import org.slave.mcprd.models.Assets;
import org.slave.mcprd.models.Resources;
import org.slave.mcprd.models.Rule;
import org.slave.mcprd.models.Version;
import org.slave.mcprd.models.VersionManifest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

public final class AdapterFactory implements JsonAdapter.Factory {

    @Override
    public JsonAdapter<?> create(final Type type, final Set<? extends Annotation> annotations, final Moshi moshi) {
        if (type == VersionManifest.class) return new VersionManifest.Adapter(moshi);
        else if (type == VersionManifest.Latest.class) return new VersionManifest.Latest.Adapter(moshi);
        else if (type == VersionManifest.Version.class) return new VersionManifest.Version.Adapter(moshi);

        if (type == Version.class) return new Version.Adapter(moshi);
        else if (type == Version.Arguments.class) return new Version.Arguments.Adapter(moshi);
        else if (type == Version.Arguments.Argument.class) return new Version.Arguments.Argument.Adapter(moshi);
        else if (type == Version.Arguments.Argument.Value.class) return new Version.Arguments.Argument.Value.Adapter();
        else if (type == Version.Assets.class) return new Version.Assets.Adapter();
        else if (type == Version.AssetIndex.class) return new Version.AssetIndex.Adapter();
        else if (type == Version.Downloads.class) return new Version.Downloads.Adapter(moshi);
        else if (type == Version.Downloads.Download.class) return new Version.Downloads.Download.Adapter();
        else if (type == Version.JavaVersion.class) return new Version.JavaVersion.Adapter();
        else if (type == Version.Library.class) return new Version.Library.Adapter(moshi);
        else if (type == Version.Library.Extract.class) return new Version.Library.Extract.Adapter();
        else if (type == Version.Library.Downloads.class) return new Version.Library.Downloads.Adapter(moshi);
        else if (type == Version.Library.Downloads.Artifact.class) return new Version.Library.Downloads.Artifact.Adapter();
        else if (type == Version.Library.Downloads.Classifiers.class) return new Version.Library.Downloads.Classifiers.Adapter(moshi);
        else if (type == Version.Library.Natives.class) return new Version.Library.Natives.Adapter();
        else if (type == Rule.class) return new Rule.Adapter(moshi);
        else if (type == Rule.Action.class) return new Rule.Action.Adapter();
        else if (type == Rule.OS.class) return new Rule.OS.Adapter();
        else if (type == Version.Logging.class) return new Version.Logging.Adapter(moshi);
        else if (type == Version.Logging.Client.class) return new Version.Logging.Client.Adapter(moshi);
        else if (type == Version.Logging.Client.File.class) return new Version.Logging.Client.File.Adapter();

        if (type == Resources.class) return new Resources.Adapter(moshi);
        else if (type == Resources.ResourceObject.class) return new Resources.ResourceObject.Adapter();

        if (type == Assets.class) return new Assets.Adapter(moshi);
        else if (type == Assets.Asset.class) return new Assets.Asset.Adapter();
        return null;
    }

}
