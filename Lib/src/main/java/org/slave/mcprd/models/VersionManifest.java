package org.slave.mcprd.models;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public record VersionManifest(VersionManifest.Latest latest, Version[] versions) {

    @RequiredArgsConstructor
    public static final class Adapter extends JsonAdapter<VersionManifest> {

        private final Moshi moshi;

        @Override
        public VersionManifest fromJson(final JsonReader reader) throws IOException {
            reader.setLenient(true);

            Latest latest = null;
            List<Version> versions = new ArrayList<>();

            reader.beginObject();
            while(reader.hasNext()) {
                switch (reader.nextName()) {
                    case "latest" -> latest = moshi.adapter(Latest.class).fromJson(reader);
                    case "versions" -> {
                        reader.beginArray();
                        while(reader.hasNext()) {
                            versions.add(
                                    moshi.adapter(Version.class).fromJson(reader)
                            );
                        }
                        reader.endArray();
                    }
                }
            }
            reader.endObject();
            return new VersionManifest(latest, versions.toArray(new Version[0]));
        }

        @Override
        public void toJson(final JsonWriter writer, final VersionManifest value) throws IOException {
        }

    }

    public record Latest(String release, String snapshot) {

        @RequiredArgsConstructor
        public static final class Adapter extends JsonAdapter<Latest> {

            private final Moshi moshi;

            @Override
            public Latest fromJson(final JsonReader reader) throws IOException {
                String release = null, snapshot = null;
                reader.beginObject();
                while(reader.hasNext()) {
                    switch(reader.nextName()) {
                        case "release" -> release = reader.nextString();
                        case "snapshot" -> snapshot = reader.nextString();
                    }
                }
                reader.endObject();
                return new Latest(release, snapshot);
            }

            @Override
            public void toJson(final JsonWriter writer, final Latest value) throws IOException {
            }

        }

    }

    public record Version(String id, String type, String url, String time, String releaseTime, String sha1, int complianceLevel) {

        public static final String TYPE_RELEASE = "release", TYPE_SNAPSHOT = "snapshot";

        @RequiredArgsConstructor
        public static final class Adapter extends JsonAdapter<Version> {

            private final Moshi moshi;

            @Override
            public Version fromJson(final JsonReader reader) throws IOException {
                String id = null, type = null, url = null, time = null, releaseTime = null, sha1 = null;
                int complianceLevel = -1;
                reader.beginObject();
                while(reader.hasNext()) {
                    switch(reader.nextName()) {
                        case "id" -> id = reader.nextString();
                        case "type" -> type = reader.nextString();
                        case "url" -> url = reader.nextString();
                        case "time" -> time = reader.nextString();
                        case "releaseTime" -> releaseTime = reader.nextString();
                        case "sha1" -> sha1 = reader.nextString();
                        case "complianceLevel" -> complianceLevel = reader.nextInt();
                    }
                }
                reader.endObject();
                return new Version(id, type, url, time, releaseTime, sha1, complianceLevel);
            }

            @Override
            public void toJson(final JsonWriter writer, final Version value) throws IOException {
            }

        }

    }

}
