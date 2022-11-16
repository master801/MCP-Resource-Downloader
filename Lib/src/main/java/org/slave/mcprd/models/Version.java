package org.slave.mcprd.models;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public record Version(
        AssetIndex assetIndex,
        Version.Assets assets,
        int complianceLevel,
        Version.Downloads downloads,
        String id,
        JavaVersion javaVersion,
        Library[] libraries,
        Logging logging,
        String mainClass,
        String minecraftArguments,
        int minimumLauncherVersion,
        String releaseTime,
        String time,
        String type
) {

    @RequiredArgsConstructor
    public static final class Adapter extends JsonAdapter<Version> {

        private final Moshi moshi;

        @Override
        public Version fromJson(final JsonReader reader) throws IOException {
            AssetIndex assetIndex = null;
            Version.Assets assets = null;
            int complianceLevel = -1;
            Version.Downloads downloads = null;
            String id = null;
            JavaVersion javaVersion = null;
            Library[] libraries = null;
            Version.Logging logging = null;
            String mainClass = null, minecraftArguments = null;
            int minimumLauncherVersion = -1;
            String releaseTime = null, time = null, type = null;

            reader.beginObject();
            while(reader.hasNext()) {
                switch (reader.nextName()) {
                    case "assetIndex" -> assetIndex = moshi.adapter(AssetIndex.class).fromJson(reader);
                    case "assets" -> assets = moshi.adapter(Version.Assets.class).fromJson(reader);
                    case "complianceLevel" -> complianceLevel = reader.nextInt();
                    case "downloads" -> downloads = moshi.adapter(Downloads.class).fromJson(reader);
                    case "id" -> id = reader.nextString();
                    case "javaVersion" -> javaVersion = moshi.adapter(JavaVersion.class).fromJson(reader);
                    case "libraries" -> {
                        List<Library> i = new ArrayList<>();
                        reader.beginArray();
                        while (reader.hasNext()) {
                            i.add(
                                    moshi.adapter(Library.class).fromJson(reader)
                            );
                        }
                        reader.endArray();
                        libraries = i.toArray(new Library[0]);
                    }
                    case "logging" -> logging = moshi.adapter(Version.Logging.class).fromJson(reader);
                    case "mainClass" -> mainClass = reader.nextString();
                    case "minecraftArguments" -> minecraftArguments = reader.nextString();
                    case "minimumLauncherVersion" -> minimumLauncherVersion = reader.nextInt();
                    case "releaseTime" -> releaseTime = reader.nextString();
                    case "time" -> time = reader.nextString();
                    case "type" -> type = reader.nextString();
                }
            }
            reader.endObject();
            return new Version(
                    assetIndex,
                    assets,
                    complianceLevel,
                    downloads,
                    id,
                    javaVersion,
                    libraries,
                    logging,
                    mainClass,
                    minecraftArguments,
                    minimumLauncherVersion,
                    releaseTime,
                    time,
                    type
            );
        }

        @Override
        public void toJson(final JsonWriter writer, final Version value) throws IOException {
            if (value == null) throw new NullPointerException("Cannot serialize null object!");
            writer.beginObject();

            writer.name("assetIndex");
            moshi.adapter(Version.AssetIndex.class)
                    .toJson(writer, value.assetIndex());

            writer.name("assets");
            moshi.adapter(Version.Assets.class)
                    .toJson(writer, value.assets());

            writer.name("downloads");
            moshi.adapter(Version.Downloads.class)
                    .toJson(writer, value.downloads());

            writer.name("id")
                    .value(value.id());

            writer.name("libraries");
            moshi.adapter(Version.Library[].class)
                    .toJson(writer, value.libraries());

            if (value.logging() != null) {
                writer.name("logging");
                moshi.adapter(Version.Logging.class)
                        .toJson(writer, value.logging());
            }

            writer.name("mainClass")
                    .value(value.mainClass());

            writer.name("minecraftArguments")
                    .value(value.minecraftArguments());

            writer.name("minimumLauncherVersion")
                    .value(value.minimumLauncherVersion());

            writer.name("releaseTime")
                    .value(value.releaseTime());

            writer.name("time")
                    .value(value.time());

            writer.name("type")
                    .value(value.type());

            writer.endObject();
        }
    }

    public record AssetIndex(String id, String sha1, int size, int totalSize, String url) {

        public static final class Adapter extends JsonAdapter<AssetIndex> {

            @Override
            public AssetIndex fromJson(final JsonReader reader) throws IOException {
                String id = null, sha1 = null, url = null;
                int size = -1, totalSize = -1;

                reader.beginObject();
                while(reader.hasNext()) {
                    switch(reader.nextName()) {
                        case "id" -> id = reader.nextString();
                        case "sha1" -> sha1 = reader.nextString();
                        case "size" -> size = reader.nextInt();
                        case "totalSize" -> totalSize = reader.nextInt();
                        case "url" -> url = reader.nextString();
                    }
                }
                reader.endObject();
                return new AssetIndex(id, sha1, size, totalSize, url);
            }

            @Override
            public void toJson(final JsonWriter writer, final AssetIndex value) throws IOException {
                if (value == null) throw new NullPointerException("Cannot serialize null object!");
                writer.beginObject();

                writer.name("id")
                        .value(value.id());
                writer.name("sha1")
                        .value(value.sha1());
                writer.name("size")
                        .value(value.size());
                writer.name("totalSize")
                        .value(value.totalSize());
                writer.name("url")
                        .value(value.url());

                writer.endObject();
            }

        }

    }

    @RequiredArgsConstructor
    public enum Assets {

        PRE_1_6("pre-1.6"),//1.5.2 and lower

        LEGACY("legacy"),//1.6

        _1_7_10("1.7.10"),

        ;

        public final String assets;

        public static final class Adapter extends JsonAdapter<Version.Assets> {//For the lulz

            @Override
            public Assets fromJson(final JsonReader reader) throws IOException {
                String assetsString = reader.nextString();
                for(Version.Assets assets : Version.Assets.values()) {
                    if (assets.assets.equals(assetsString)) return assets;
                }
                return null;
            }

            @Override
            public void toJson(final JsonWriter writer, final Assets value) throws IOException {
                if (value == null) throw new NullPointerException("Cannot serialize null object!");
                writer.value(value.assets);
            }

        }

    }

    public record Downloads(Download client, Download server, Download windows_server) {

        @RequiredArgsConstructor
        public static final class Adapter extends JsonAdapter<Version.Downloads> {

            private final Moshi moshi;

            @Override
            public Downloads fromJson(final JsonReader reader) throws IOException {
                Downloads.Download client = null, server = null, windows_server = null;
                reader.beginObject();
                while(reader.hasNext()) {
                    switch(reader.nextName()) {
                        case "client" -> client = moshi.adapter(Downloads.Download.class).fromJson(reader);
                        case "server" -> server = moshi.adapter(Downloads.Download.class).fromJson(reader);
                        case "windows_server" -> windows_server = moshi.adapter(Downloads.Download.class).fromJson(reader);
                    }
                }
                reader.endObject();
                return new Downloads(client, server, windows_server);
            }

            @Override
            public void toJson(final JsonWriter writer, final Downloads value) throws IOException {
                if (value == null) throw new NullPointerException("Cannot serialize null object!");

                writer.beginObject();

                writer.name("client");
                moshi.adapter(Downloads.Download.class)
                        .toJson(writer, value.client());

                writer.name("server");
                moshi.adapter(Downloads.Download.class)
                        .toJson(writer, value.server());

                writer.name("windows_server");
                moshi.adapter(Downloads.Download.class)
                        .toJson(writer, value.windows_server());

                writer.endObject();
            }

        }

        public record Download(String sha1, int size, String url) {

            public static final class Adapter extends JsonAdapter<Download> {

                @Override
                public Download fromJson(final JsonReader reader) throws IOException {
                    String sha1 = null, url = null;
                    int size = -1;

                    reader.beginObject();
                    while(reader.hasNext()) {
                        switch(reader.nextName()) {
                            case "sha1" -> sha1 = reader.nextString();
                            case "size" -> size = reader.nextInt();
                            case "url" -> url = reader.nextString();
                        }
                    }
                    reader.endObject();
                    return new Download(sha1, size, url);
                }

                @Override
                public void toJson(final JsonWriter writer, final Download value) throws IOException {
                    if (value == null) throw new NullPointerException("Cannot serialize null object!");
                    writer.beginObject();

                    writer.name("sha1")
                            .value(value.sha1());
                    writer.name("size")
                            .value(value.size());
                    writer.name("url")
                            .value(value.url());

                    writer.endObject();
                }

            }

        }

    }

    public record JavaVersion(String component, int majorVersion) {

        public static final class Adapter extends JsonAdapter<JavaVersion> {

            @Override
            public JavaVersion fromJson(final JsonReader reader) throws IOException {
                String component = null;
                int majorVersion = -1;

                reader.beginObject();
                while(reader.hasNext()) {
                    switch(reader.nextName()) {
                        case "component" -> component = reader.nextString();
                        case "majorVersion" -> majorVersion = reader.nextInt();
                    }
                }
                reader.endObject();
                return new JavaVersion(component, majorVersion);
            }

            @Override
            public void toJson(final JsonWriter writer, final JavaVersion value) throws IOException {
                if (value == null) throw new NullPointerException("Cannot serialize null object!");
                writer.beginObject();

                writer.name("component")
                        .value(value.component());
                writer.name("majorVersion")
                        .value(value.majorVersion());

                writer.endObject();
            }

        }

    }

    public record Library(Library.Downloads downloads, Extract extract, String name, Library.Rule[] rules, Natives natives) {

        @RequiredArgsConstructor
        public static final class Adapter extends JsonAdapter<Library> {

            private final Moshi moshi;

            @Override
            public Library fromJson(final JsonReader reader) throws IOException {
                Library.Downloads downloads = null;
                Extract extract = null;
                String name = null;
                Version.Library.Rule[] rules = null;
                Natives natives = null;

                reader.beginObject();
                while(reader.hasNext()) {
                    switch(reader.nextName()) {
                        case "downloads" -> downloads = moshi.adapter(Library.Downloads.class).fromJson(reader);
                        case "extract" -> extract = moshi.adapter(Library.Extract.class).fromJson(reader);
                        case "name" -> name = reader.nextString();
                        case "rules" -> rules = moshi.adapter(Library.Rule[].class).fromJson(reader);
                        case "natives" -> natives = moshi.adapter(Library.Natives.class).fromJson(reader);
                    }
                }
                reader.endObject();
                return new Library(downloads, extract, name, rules, natives);
            }

            @Override
            public void toJson(final JsonWriter writer, final Library value) throws IOException {
                if (value == null) throw new NullPointerException("Cannot serialize null object!");
                writer.beginObject();

                if (value.downloads() != null) {
                    writer.name("downloads");
                    moshi.adapter(Library.Downloads.class)
                            .toJson(writer, value.downloads());
                }

                if (value.extract() != null) {
                    writer.name("extract");
                    moshi.adapter(Library.Extract.class)
                            .toJson(writer, value.extract());
                }

                writer.name("name")
                        .value(value.name());

                if (value.natives() != null) {
                    writer.name("natives");
                    moshi.adapter(Library.Natives.class)
                            .toJson(writer, value.natives());
                }

                if (value.rules() != null) {
                    writer.name("rules");
                    writer.beginArray();
                    for(int i = 0; i < value.rules().length; ++i) {
                        moshi.adapter(Library.Rule.class)
                                .toJson(writer, value.rules()[i]);
                    }
                    writer.endArray();
                }

                writer.endObject();
            }

        }

        public record Downloads(Library.Downloads.Artifact artifact, Version.Library.Downloads.Classifiers classifiers) {

            @RequiredArgsConstructor
            public static final class Adapter extends JsonAdapter<Library.Downloads> {

                private final Moshi moshi;

                @Override
                public Downloads fromJson(final JsonReader reader) throws IOException {
                    Artifact artifact = null;
                    Classifiers classifiers = null;

                    reader.beginObject();
                    while(reader.hasNext()) {
                        switch(reader.nextName()) {
                            case "artifact" -> artifact = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                            case "classifiers" -> classifiers = moshi.adapter(Classifiers.class).fromJson(reader);
                        }
                    }
                    reader.endObject();
                    return new Downloads(artifact, classifiers);
                }

                @Override
                public void toJson(final JsonWriter writer, final Downloads value) throws IOException {
                    if (value == null) throw new NullPointerException("Cannot serialize null object!");
                    writer.beginObject();

                    if (value.artifact() != null) {
                        writer.name("artifact");
                        moshi.adapter(Library.Downloads.Artifact.class)
                                .toJson(writer, value.artifact());
                    }
                    if (value.classifiers() != null) {
                        writer.name("classifiers");
                        moshi.adapter(Library.Downloads.Classifiers.class)
                                .toJson(writer, value.classifiers());
                    }

                    writer.endObject();
                }

            }

            public record Artifact(String path, String sha1, int size, String url) {

                public static final class Adapter extends JsonAdapter<Artifact> {

                    @Override
                    public Artifact fromJson(final JsonReader reader) throws IOException {
                        String path = null, sha1 = null, url = null;
                        int size = -1;
                        reader.beginObject();
                        while(reader.hasNext()) {
                            switch(reader.nextName()) {
                                case "path" -> path = reader.nextString();
                                case "sha1" -> sha1 = reader.nextString();
                                case "size" -> size = reader.nextInt();
                                case "url" -> url = reader.nextString();
                            }
                        }
                        reader.endObject();
                        return new Artifact(path, sha1, size, url);
                    }

                    @Override
                    public void toJson(final JsonWriter writer, final Artifact value) throws IOException {
                        if (value == null) throw new NullPointerException("Cannot serialize null object!");
                        writer.beginObject();

                        writer.name("path")
                                .value(value.path());

                        writer.name("sha1")
                                .value(value.sha1());

                        writer.name("size")
                                .value(value.size());

                        writer.name("url")
                                .value(value.url());

                        writer.endObject();
                    }

                }

            }

            public record Classifiers(Library.Downloads.Artifact natives_linux, Library.Downloads.Artifact natives_osx, Library.Downloads.Artifact natives_windows, Library.Downloads.Artifact natives_windows_32, Library.Downloads.Artifact natives_windows_64) {

                @RequiredArgsConstructor
                public static final class Adapter extends JsonAdapter<Classifiers> {

                    private final Moshi moshi;

                    @Override
                    public Classifiers fromJson(final JsonReader reader) throws IOException {
                        Library.Downloads.Artifact natives_linux = null, natives_osx = null;
                        Library.Downloads.Artifact natives_windows = null, natives_windows_32 = null, natives_windows_64 = null;

                        reader.beginObject();
                        while(reader.hasNext()) {
                            switch(reader.nextName()) {
                                case "natives-linux" -> natives_linux = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                                case "natives-osx" -> natives_osx = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                                case "natives-windows" -> natives_windows = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                                case "natives-windows-32" -> natives_windows_32 = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                                case "natives-windows-64" -> natives_windows_64 = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                            }
                        }
                        reader.endObject();
                        return new Classifiers(natives_linux, natives_osx, natives_windows, natives_windows_32, natives_windows_64);
                    }

                    @Override
                    public void toJson(final JsonWriter writer, final Classifiers value) throws IOException {
                        if (value == null) throw new NullPointerException("Cannot serialize null object!");

                        writer.beginObject();

                        if (value.natives_linux() != null) {
                            writer.name("natives-linux");
                            moshi.adapter(Library.Downloads.Artifact.class)
                                    .toJson(writer, value.natives_linux());
                        }

                        if (value.natives_osx() != null) {
                            writer.name("natives-osx");
                            moshi.adapter(Library.Downloads.Artifact.class)
                                    .toJson(writer, value.natives_osx());
                        }

                        if (value.natives_windows() != null) {
                            writer.name("natives-windows");
                            moshi.adapter(Library.Downloads.Artifact.class)
                                    .toJson(writer, value.natives_windows());
                        }
                        if (value.natives_windows_32() != null) {
                            writer.name("natives-windows-32");
                            moshi.adapter(Library.Downloads.Artifact.class)
                                    .toJson(writer, value.natives_windows_32());
                        }
                        if (value.natives_windows_64() != null) {
                            writer.name("natives-windows-64");
                            moshi.adapter(Library.Downloads.Artifact.class)
                                    .toJson(writer, value.natives_windows_64());
                        }

                        writer.endObject();
                    }

                }

            }

        }

        public record Extract(String[] exclude) {

            public static final class Adapter extends JsonAdapter<Extract> {

                @SuppressWarnings("SwitchStatementWithTooFewBranches")
                @Override
                public Extract fromJson(final JsonReader reader) throws IOException {
                    String[] exclude = null;
                    reader.beginObject();
                    while(reader.hasNext()) {
                        switch(reader.nextName()) {
                            case "exclude" -> {
                                List<String> i = new ArrayList<>();
                                reader.beginArray();
                                while(reader.hasNext()) i.add(reader.nextString());
                                reader.endArray();
                                exclude = i.toArray(new String[0]);
                            }
                        }
                    }
                    reader.endObject();
                    return new Extract(exclude);
                }

                @Override
                public void toJson(final JsonWriter writer, final Extract value) throws IOException {
                    if (value == null) throw new NullPointerException("Cannot serialize null object!");
                    writer.beginObject();

                    writer.name("exclude");
                    writer.beginArray();
                    for(String i : value.exclude()) writer.value(i);
                    writer.endArray();

                    writer.endObject();
                }

            }

        }

        public record Natives(String linux, String osx, String windows) {

            public static final class Adapter extends JsonAdapter<Natives> {

                @Override
                public Natives fromJson(final JsonReader reader) throws IOException {
                    String linux = null, osx = null, windows = null;
                    reader.beginObject();
                    while(reader.hasNext()) {
                        switch(reader.nextName()) {
                            case "linux" -> linux = reader.nextString();
                            case "osx" -> osx = reader.nextString();
                            case "windows" -> windows = reader.nextString();
                        }
                    }
                    reader.endObject();
                    return new Natives(linux, osx, windows);
                }

                @Override
                public void toJson(final JsonWriter writer, final Natives value) throws IOException {
                    if (value == null) throw new NullPointerException("Cannot serialize null object!");
                    writer.beginObject();

                    writer.name("linux").value(value.linux());
                    writer.name("osx").value(value.osx());
                    writer.name("windows").value(value.windows());

                    writer.endObject();
                }

            }

        }

        public record Rule(Version.Library.Rule.Action action, OS os) {

            @RequiredArgsConstructor
            public static final class Adapter extends JsonAdapter<Version.Library.Rule> {

                private final Moshi moshi;

                @Override
                public Rule fromJson(final JsonReader reader) throws IOException {
                    Version.Library.Rule.Action action = null;
                    OS os = null;

                    reader.beginObject();
                    while(reader.hasNext()) {
                        switch(reader.nextName()) {
                            case "action" -> action = moshi.adapter(Version.Library.Rule.Action.class).fromJson(reader);
                            case "os" -> os = moshi.adapter(Version.Library.Rule.OS.class).fromJson(reader);
                        }
                    }
                    reader.endObject();
                    return new Rule(action, os);
                }

                @Override
                public void toJson(final JsonWriter writer, final Rule value) throws IOException {
                    if (value == null) throw new NullPointerException("Cannot serialize null object!");
                    writer.beginObject();

                    writer.name("action");
                    moshi.adapter(Version.Library.Rule.Action.class).toJson(writer, value.action());

                    if (value.os() != null) {//May be missing
                        writer.name("os");
                        moshi.adapter(Version.Library.Rule.OS.class).toJson(writer, value.os());
                    }

                    writer.endObject();
                }

            }

            @RequiredArgsConstructor
            public enum Action {

                ALLOW("allow"),

                DISALLOW("disallow");

                public final String action;

                public static final class Adapter extends JsonAdapter<Version.Library.Rule.Action> {

                    @Override
                    public Version.Library.Rule.Action fromJson(final JsonReader reader) throws IOException {
                        String action = reader.nextString();
                        for(Version.Library.Rule.Action i : Version.Library.Rule.Action.values()) {
                            if (i.action.equals(action)) return i;
                        }
                        return null;
                    }

                    @Override
                    public void toJson(final JsonWriter writer, final Version.Library.Rule.Action value) throws IOException {
                        if (value == null) throw new NullPointerException("Cannot serialize null object!");
                        writer.value(value.action);
                    }

                }

            }

            public record OS(String name, String version) {

                public static final class Adapter extends JsonAdapter<Version.Library.Rule.OS> {

                    @Override
                    public OS fromJson(final JsonReader reader) throws IOException {
                        String name = null, version = null;

                        reader.beginObject();
                        while(reader.hasNext()) {
                            switch(reader.nextName()) {
                                case "name" -> name = reader.nextString();
                                case "version" -> version = reader.nextString();
                            }
                        }
                        reader.endObject();
                        return new OS(name, version);
                    }

                    @Override
                    public void toJson(final JsonWriter writer, final OS value) throws IOException {
                        if (value == null) throw new NullPointerException("Cannot serialize null object!");
                        writer.beginObject();

                        writer.name("name")
                                .value(value.name());

                        writer.name("version")
                                .value(value.version());

                        writer.endObject();
                    }

                }

            }

        }

    }

    public record Logging(Client client) {

        @RequiredArgsConstructor
        public static final class Adapter extends JsonAdapter<Logging> {

            private final Moshi moshi;

            @SuppressWarnings("SwitchStatementWithTooFewBranches")
            @Override
            public Logging fromJson(final JsonReader reader) throws IOException {
                Version.Logging.Client client = null;

                reader.beginObject();
                while(reader.hasNext()) {
                    switch(reader.nextName()) {
                        case "client" -> client = moshi.adapter(Version.Logging.Client.class).fromJson(reader);
                    }
                }
                reader.endObject();

                return new Version.Logging(client);
            }

            @Override
            public void toJson(final JsonWriter writer, final Logging value) throws IOException {
                if (value == null) throw new NullPointerException("Cannot serialize null object!");
                writer.beginObject();

                writer.name("client");
                moshi.adapter(Version.Logging.Client.class)
                        .toJson(writer, value.client);

                writer.endObject();
            }

        }

        public record Client(String argument, Version.Logging.Client.File file, String type) {

            @RequiredArgsConstructor
            public static final class Adapter extends JsonAdapter<Client> {

                private final Moshi moshi;

                @Override
                public Client fromJson(final JsonReader reader) throws IOException {
                    String argument = null;
                    Version.Logging.Client.File file = null;
                    String type = null;

                    reader.beginObject();
                    while(reader.hasNext()) {
                        switch(reader.nextName()) {
                            case "argument" -> argument = reader.nextString();
                            case "file" -> file = moshi.adapter(Version.Logging.Client.File.class).fromJson(reader);
                            case "type" -> type = reader.nextString();
                        }
                    }
                    reader.endObject();

                    return new Version.Logging.Client(argument, file, type);
                }

                @Override
                public void toJson(final JsonWriter writer, final Client value) throws IOException {
                    if (value == null) throw new NullPointerException("Cannot serialize null object!");
                    writer.beginObject();

                    writer.name("argument")
                            .value(value.argument());

                    writer.name("file");
                    moshi.adapter(Version.Logging.Client.File.class)
                            .toJson(writer, value.file());

                    writer.name("type")
                            .value(value.type());

                    writer.endObject();
                }

            }

            public record File(String id, String sha1, int size, String url) {

                public static final class Adapter extends JsonAdapter<Client.File> {

                    @Override
                    public File fromJson(final JsonReader reader) throws IOException {
                        String id = null, sha1 = null;
                        int size = -1;
                        String url = null;

                        reader.beginObject();
                        while(reader.hasNext()) {
                            switch(reader.nextName()) {
                                case "id" -> id = reader.nextString();
                                case "sha1" -> sha1 = reader.nextString();
                                case "size" -> size = reader.nextInt();
                                case "url" -> url = reader.nextString();
                            }
                        }
                        reader.endObject();

                        return new Version.Logging.Client.File(id, sha1, size, url);
                    }

                    @Override
                    public void toJson(final JsonWriter writer, final Version.Logging.Client.File value) throws IOException {
                        if (value == null) throw new NullPointerException("Cannot serialize null object!");
                        writer.beginObject()
                                .name("id")
                                .value(value.id())

                                .name("sha1")
                                .value(value.sha1())

                                .name("size")
                                .value(value.size())

                                .name("url")
                                .value(value.url())
                        .endObject();
                    }

                }

            }

        }

    }

}
