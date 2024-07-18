package org.slave.mcprd.models;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.slave.mcprd.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public record Version(
        Version.Arguments arguments,
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
            Version.Arguments arguments = null;
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
                    case "arguments" -> arguments = moshi.adapter(Version.Arguments.class).fromJson(reader);
                    case "assetIndex" -> assetIndex = moshi.adapter(AssetIndex.class).fromJson(reader);
                    case "assets" -> assets = moshi.adapter(Version.Assets.class).fromJson(reader);
                    case "complianceLevel" -> complianceLevel = reader.nextInt();
                    case "downloads" -> downloads = moshi.adapter(Downloads.class).fromJson(reader);
                    case "id" -> id = reader.nextString();
                    case "javaVersion" -> javaVersion = moshi.adapter(JavaVersion.class).fromJson(reader);
                    case "libraries" -> libraries = moshi.adapter(Library[].class).fromJson(reader);
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
                    arguments,
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

            if (value.arguments() != null) {//1.18+
                writer.name("arguments");
                moshi.adapter(Version.Arguments.class)
                        .toJson(writer, value.arguments());
            }

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

            if (value.minecraftArguments() != null) {//may be null [for 1.16+]
                writer.name("minecraftArguments")
                        .value(value.minecraftArguments());
            }

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

    public record Arguments(Version.Arguments.Argument[] game, Version.Arguments.Argument[] jvm) {

        @RequiredArgsConstructor
        public static final class Adapter extends JsonAdapter<Arguments> {

            private final Moshi moshi;

            @Override
            public Arguments fromJson(final JsonReader reader) throws IOException {
                reader.beginObject();

                Version.Arguments.Argument[] game = null;
                Version.Arguments.Argument[] jvm = null;
                while(reader.hasNext()) {
                    switch(reader.nextName()) {
                        case "game" -> game = moshi.adapter(Version.Arguments.Argument[].class).fromJson(reader);
                        case "jvm" -> jvm = moshi.adapter(Version.Arguments.Argument[].class).fromJson(reader);
                    }
                }

                reader.endObject();
                return new Arguments(game, jvm);
            }

            @Override
            public void toJson(final JsonWriter writer, final Arguments value) throws IOException {
                if (value == null) throw new NullPointerException("Cannot serialize null object!");
                writer.beginObject();

                writer.name("game");
                moshi.adapter(Version.Arguments.Argument[].class).toJson(writer, value.game());

                writer.name("jvm");
                moshi.adapter(Version.Arguments.Argument[].class).toJson(writer, value.jvm());

                writer.endObject();
            }

        }

        public static final class Argument {

            @Getter
            private String argument;

            @Getter
            private Rule[] rules;
            @Getter
            private Version.Arguments.Argument.Value value;

            public Argument(final String argument) {
                this.argument = argument;
            }

            public Argument(final Rule[] rules, final Version.Arguments.Argument.Value value) {
                this.rules = rules;
                this.value = value;
            }

            @RequiredArgsConstructor
            public static final class Adapter extends JsonAdapter<Version.Arguments.Argument> {

                private final Moshi moshi;

                @Override
                public Argument fromJson(final JsonReader reader) throws IOException {
                    Argument argument;
                    switch(reader.peek()) {
                        case STRING -> argument = new Argument(reader.nextString());
                        case BEGIN_OBJECT -> {
                            Rule[] rules = null;
                            Version.Arguments.Argument.Value value = null;
                            reader.beginObject();
                            while(reader.hasNext()) {
                                switch(reader.nextName()) {
                                    case "rules" -> rules = moshi.adapter(Rule[].class).fromJson(reader);
                                    case "value" -> value = moshi.adapter(Version.Arguments.Argument.Value.class).fromJson(reader);
                                }
                            }
                            reader.endObject();
                            if (rules != null && value != null) {
                                argument = new Version.Arguments.Argument(rules, value);
                            } else {
                                throw new RuntimeException("Failed to deserialize \"rules\" object!");
                            }
                        }
                        default -> throw new RuntimeException("Found unexpected argument while parsing!");
                    }
                    return argument;
                }

                @Override
                public void toJson(final JsonWriter writer, final Argument value) throws IOException {
                    if (value == null) throw new NullPointerException("Cannot serialize null object!");
                    if (value.getArgument() != null) {
                        writer.value(value.getArgument());
                    } else if (value.getRules() != null && value.getValue() != null) {
                        writer.beginObject();

                        writer.name("rules");
                        moshi.adapter(Rule[].class)
                                .toJson(writer, value.getRules());

                        writer.name("value");
                        moshi.adapter(Version.Arguments.Argument.Value.class)
                                .toJson(writer, value.getValue());

                        writer.endObject();
                    }
                }

            }

            public static final class Value {

                @Getter
                private String value;


                @Getter
                private String[] valueArray;

                public Value(final String value) {
                    this.value = value;
                }

                public Value(final String[] valueArray) {
                    this.valueArray = valueArray;
                }

                public static final class Adapter extends JsonAdapter<Version.Arguments.Argument.Value> {

                    @Override
                    public Value fromJson(final JsonReader reader) throws IOException {
                        Value value = null;
                        switch(reader.peek()) {
                            case STRING -> value = new Value(reader.nextString());
                            case BEGIN_ARRAY -> {
                                List<String> values = new ArrayList<>();

                                reader.beginArray();
                                while(reader.hasNext()) values.add(reader.nextString());
                                reader.endArray();

                                value = new Value(values.toArray(new String[0]));
                            }
                        }
                        if (value == null) throw new RuntimeException("Failed to deserialize value in rule!");
                        return value;
                    }

                    @Override
                    public void toJson(final JsonWriter writer, final Value value) throws IOException {
                        if (value == null) throw new NullPointerException("Cannot serialize null object!");
                        if (value.getValue() != null) {
                            writer.value(value.getValue());
                        } else if (value.getValueArray() != null) {
                            writer.beginArray();
                            for(String i : value.getValueArray()) writer.value(i);
                            writer.endArray();
                        }
                    }

                }

            }

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

        NEWER(null);//Anything past 1.6

        public final String assets;

        public static final class Adapter extends JsonAdapter<Version.Assets> {//For the lulz

            @Override
            public Assets fromJson(final JsonReader reader) throws IOException {
                String assetsString = reader.nextString();
                for(Version.Assets assets : Version.Assets.values()) {
                    if (assets.assets != null && assets.assets.equals(assetsString)) return assets;
                }
                return Assets.NEWER;
            }

            @Override
            public void toJson(final JsonWriter writer, final Assets value) throws IOException {
                if (value == null) throw new NullPointerException("Cannot serialize null object!");
                writer.value(value.assets);
            }

        }

    }

    public record Downloads(Version.Downloads.Download client, @Nullable Version.Downloads.Download client_mappings, Version.Downloads.Download server, @Nullable Version.Downloads.Download windows_server, @Nullable Version.Downloads.Download server_mappings) {

        @RequiredArgsConstructor
        public static final class Adapter extends JsonAdapter<Version.Downloads> {

            private final Moshi moshi;

            @Override
            public Downloads fromJson(final JsonReader reader) throws IOException {
                Version.Downloads.Download client = null;
                Version.Downloads.Download client_mappings = null;
                Downloads.Download server = null, windows_server = null;
                Version.Downloads.Download server_mappings = null;
                reader.beginObject();
                while(reader.hasNext()) {
                    switch(reader.nextName()) {
                        case "client" -> client = moshi.adapter(Downloads.Download.class).fromJson(reader);
                        case "client_mappings" -> client_mappings = moshi.adapter(Downloads.Download.class).fromJson(reader);
                        case "server" -> server = moshi.adapter(Downloads.Download.class).fromJson(reader);
                        case "windows_server" -> windows_server = moshi.adapter(Downloads.Download.class).fromJson(reader);
                        case "server_mappings" -> server_mappings = moshi.adapter(Downloads.Download.class).fromJson(reader);
                    }
                }
                reader.endObject();
                return new Downloads(client, client_mappings, server, windows_server, server_mappings);
            }

            @Override
            public void toJson(final JsonWriter writer, final Downloads value) throws IOException {
                if (value == null) throw new NullPointerException("Cannot serialize null object!");

                writer.beginObject();

                writer.name("client");
                moshi.adapter(Downloads.Download.class)
                        .toJson(writer, value.client());

                if (value.client_mappings() != null) {
                    writer.name("client_mappings");
                    moshi.adapter(Downloads.Download.class)
                            .toJson(writer, value.client_mappings());
                }

                writer.name("server");
                moshi.adapter(Downloads.Download.class)
                        .toJson(writer, value.server());

                if (value.windows_server() != null) {
                    writer.name("windows_server");
                    moshi.adapter(Downloads.Download.class)
                            .toJson(writer, value.windows_server());
                }

                if (value.server_mappings() != null) {
                    writer.name("server_mappings");
                    moshi.adapter(Downloads.Download.class)
                            .toJson(writer, value.server_mappings());
                }

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

    public record Library(Library.Downloads downloads, Extract extract, Constants.Maven name, Rule[] rules, Natives natives) {

        public String[] getVersionSplit() {
            return name().version().split("-", 3);
        }

        public boolean isNightly() {//Fucking LWJGL -.-;
            String[] versionSplit = getVersionSplit();
            return versionSplit.length > 1 && versionSplit[1].equals("nightly");
        }

        @RequiredArgsConstructor
        public static final class Adapter extends JsonAdapter<Library> {

            private final Moshi moshi;

            @Override
            public Library fromJson(final JsonReader reader) throws IOException {
                Library.Downloads downloads = null;
                Extract extract = null;
                Constants.Maven name = null;
                Rule[] rules = null;
                Natives natives = null;

                reader.beginObject();
                while(reader.hasNext()) {
                    switch(reader.nextName()) {
                        case "downloads" -> downloads = moshi.adapter(Library.Downloads.class).fromJson(reader);
                        case "extract" -> extract = moshi.adapter(Library.Extract.class).fromJson(reader);
                        case "name" -> name = Constants.Maven.from(reader.nextString());
                        case "rules" -> rules = moshi.adapter(Rule[].class).fromJson(reader);
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
                        .value(Constants.Maven.to(value.name()));

                if (value.natives() != null) {
                    writer.name("natives");
                    moshi.adapter(Library.Natives.class)
                            .toJson(writer, value.natives());
                }

                if (value.rules() != null) {
                    writer.name("rules");
                    writer.beginArray();
                    for(int i = 0; i < value.rules().length; ++i) {
                        moshi.adapter(Rule.class)
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

            public record Classifiers(Library.Downloads.Artifact natives_linux, Library.Downloads.Artifact natives_osx, Library.Downloads.Artifact natives_macos, Library.Downloads.Artifact natives_windows, Library.Downloads.Artifact natives_windows_32, Library.Downloads.Artifact natives_windows_64, Library.Downloads.Artifact sources, Library.Downloads.Artifact javadoc) {

                @RequiredArgsConstructor
                public static final class Adapter extends JsonAdapter<Classifiers> {

                    private final Moshi moshi;

                    @Override
                    public Classifiers fromJson(final JsonReader reader) throws IOException {
                        Library.Downloads.Artifact natives_linux = null;
                        Library.Downloads.Artifact natives_osx = null, natives_macos = null;
                        Library.Downloads.Artifact natives_windows = null, natives_windows_32 = null, natives_windows_64 = null;
                        Library.Downloads.Artifact sources = null, javadoc = null;

                        reader.beginObject();
                        while(reader.hasNext()) {
                            switch(reader.nextName()) {
                                case "natives-linux" -> natives_linux = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                                case "natives-osx" -> natives_osx = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                                case "natives-macos" -> natives_macos = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);//WTF
                                case "natives-windows" -> natives_windows = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                                case "natives-windows-32" -> natives_windows_32 = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                                case "natives-windows-64" -> natives_windows_64 = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                                case "sources" -> sources = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);//WTF
                                case "javadoc" -> javadoc = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);//WTF
                            }
                        }
                        reader.endObject();
                        return new Classifiers(natives_linux, natives_osx, natives_macos, natives_windows, natives_windows_32, natives_windows_64, sources, javadoc);
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
                        if (value.natives_macos() != null) {
                            writer.name("natives-macos");
                            moshi.adapter(Library.Downloads.Artifact.class)
                                    .toJson(writer, value.natives_macos());
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

                        if (value.sources() != null) {
                            writer.name("sources");
                            moshi.adapter(Library.Downloads.Artifact.class)
                                    .toJson(writer, value.sources());
                        }
                        if (value.javadoc() != null) {
                            writer.name("javadoc");
                            moshi.adapter(Library.Downloads.Artifact.class)
                                    .toJson(writer, value.javadoc());
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
