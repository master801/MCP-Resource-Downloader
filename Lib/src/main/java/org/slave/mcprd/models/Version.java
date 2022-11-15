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
        String assets,
        int complianceLevel,
        Version.Downloads downloads,
        String id,
        JavaVersion javaVersion,
        Library[] libraries,
        String mainClass,
        String minecraftArguments,
        String minimumLauncherVersion,
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
            String assets = null;
            int complianceLevel = -1;
            Version.Downloads downloads = null;
            String id = null;
            JavaVersion javaVersion = null;
            Library[] libraries = null;
            String mainClass = null, minecraftArguments = null, minimumLauncherVersion = null, releaseTime = null, time = null, type = null;

            reader.beginObject();
            while(reader.hasNext()) {
                switch (reader.nextName()) {
                    case "assetIndex" -> assetIndex = moshi.adapter(AssetIndex.class).fromJson(reader);
                    case "assets" -> assets = reader.nextString();
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
                    case "mainClass" -> mainClass = reader.nextString();
                    case "minecraftArguments" -> minecraftArguments = reader.nextString();
                    case "minimumLauncherVersion" -> minimumLauncherVersion = reader.nextString();
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
                        case "rules" -> {
                            List<Version.Library.Rule> i = new ArrayList<>();
                            reader.beginArray();
                            while(reader.hasNext()) {
                                i.add(
                                        moshi.adapter(Library.Rule.class).fromJson(reader)
                                );
                            }
                            reader.endArray();
                            rules = i.toArray(new Rule[0]);
                        }
                        case "natives" -> natives = moshi.adapter(Library.Natives.class).fromJson(reader);
                    }
                }
                reader.endObject();
                return new Library(downloads, extract, name, rules, natives);
            }

            @Override
            public void toJson(final JsonWriter writer, final Library value) throws IOException {
            }

        }

        public record Downloads(Library.Downloads.Artifact artifact, Classifiers classifiers) {

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
                    }

                }

            }

            public record Classifiers(Library.Downloads.Artifact natives_linux, Library.Downloads.Artifact natives_osx, Library.Downloads.Artifact natives_windows) {

                @RequiredArgsConstructor
                public static final class Adapter extends JsonAdapter<Classifiers> {

                    private final Moshi moshi;

                    @Override
                    public Classifiers fromJson(final JsonReader reader) throws IOException {
                        Library.Downloads.Artifact natives_linux = null, natives_osx = null, natives_windows = null;

                        reader.beginObject();
                        while(reader.hasNext()) {
                            switch(reader.nextName()) {
                                case "natives-linux" -> natives_linux = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                                case "natives-osx" -> natives_osx = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                                case "natives-windows" -> natives_windows = moshi.adapter(Library.Downloads.Artifact.class).fromJson(reader);
                            }
                        }
                        reader.endObject();
                        return new Classifiers(natives_linux, natives_osx, natives_windows);
                    }

                    @Override
                    public void toJson(final JsonWriter writer, final Classifiers value) throws IOException {
                    }

                }

            }

        }

        public record Extract(String[] exclude) {

            public static final class Adapter extends JsonAdapter<Extract> {

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
                }

            }

        }

        public record Rule(String action, OS os) {

            @RequiredArgsConstructor
            public static final class Adapter extends JsonAdapter<Version.Library.Rule> {

                private final Moshi moshi;

                @Override
                public Rule fromJson(final JsonReader reader) throws IOException {
                    String action = null;
                    OS os = null;

                    reader.beginObject();
                    while(reader.hasNext()) {
                        switch(reader.nextName()) {
                            case "action" -> action = reader.nextString();
                            case "os" -> os = moshi.adapter(Version.Library.Rule.OS.class).fromJson(reader);
                        }
                    }
                    reader.endObject();
                    return new Rule(action, os);
                }

                @Override
                public void toJson(final JsonWriter writer, final Rule value) throws IOException {
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
                    }

                }

            }

        }

    }

}
