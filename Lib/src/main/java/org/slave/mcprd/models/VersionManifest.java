package org.slave.mcprd.models;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

public record VersionManifest(VersionManifest.Latest latest, Version[] versions) {

    @RequiredArgsConstructor
    public static final class Adapter extends JsonAdapter<VersionManifest> {

        private final Moshi moshi;

        @Override
        public VersionManifest fromJson(final JsonReader reader) throws IOException {
            Latest latest = null;
			Version[] versions = null;

            reader.beginObject();
            while(reader.hasNext()) {
                switch (reader.nextName()) {
                    case "latest" -> latest = moshi.adapter(Latest.class).fromJson(reader);
                    case "versions" -> versions = moshi.adapter(Version[].class).fromJson(reader);
                }
            }
            reader.endObject();
            return new VersionManifest(latest, versions);
        }

        @Override
        public void toJson(final JsonWriter writer, final VersionManifest value) throws IOException {
			if (value == null) throw new NullPointerException("Cannot serialize null object!");
			writer.beginObject();

			writer.name("latest");
			moshi.adapter(Latest.class)
					.toJson(writer, value.latest());

			writer.name("versions");
			moshi.adapter(Version[].class)
					.toJson(writer, value.versions());

			writer.endObject();
        }

    }

    public record Latest(String release, String snapshot) {

        public static final class Adapter extends JsonAdapter<Latest> {

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
				if (value == null) throw new NullPointerException("Cannot serialize null object!");
				writer.beginObject();
				writer.name("release")
						.value(value.release());
				writer.name("snapshot")
						.value(value.snapshot());
				writer.endObject();
            }

        }

    }

    public record Version(String id, String type, String url, String time, String releaseTime, String sha1, int complianceLevel) {

        public static final String TYPE_RELEASE = "release", TYPE_SNAPSHOT = "snapshot", TYPE_OLD_BETA = "old_beta", TYPE_OLD_ALPHA = "old_alpha";

        @RequiredArgsConstructor
        public static final class Adapter extends JsonAdapter<Version> {

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
				if (value == null) throw new NullPointerException("Cannot serialize null object!");
				writer.beginObject();

				writer.name("id")
						.value(value.id());
				writer.name("type")
						.value(value.type());
				writer.name("url")
						.value(value.url());
				writer.name("time")
						.value(value.time());
				writer.name("releaseTime")
						.value(value.releaseTime());
				writer.name("sha1")
						.value(value.sha1());
				writer.name("complianceLevel")
						.value(value.complianceLevel());

				writer.endObject();
            }

        }

    }

}
