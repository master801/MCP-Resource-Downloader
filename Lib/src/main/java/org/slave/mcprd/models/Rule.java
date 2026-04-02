package org.slave.mcprd.models;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonReader.Token;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public record Rule(@NotNull Rule.Action action, @Nullable OS os, @Nullable Map<String, Object> features) {

    @RequiredArgsConstructor
    public static final class Adapter extends JsonAdapter<Rule> {

        private final Moshi moshi;

        @SuppressWarnings("unchecked")
        @Override
        public org.slave.mcprd.models.Rule fromJson(final JsonReader reader) throws IOException {
            Rule.Action action = null;
            @Nullable OS os = null;
            @Nullable Map<String, Object> features = null;

            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "action" -> action = moshi.adapter(Rule.Action.class).fromJson(reader);
                    case "os" -> os = moshi.adapter(Rule.OS.class).fromJson(reader);
                    case "features" -> {
                        if (reader.peek() == Token.BEGIN_OBJECT) {
                            //We already check if it's an object... no need to warn us...
                            features = (Map<String, Object>)reader.readJsonValue();
                        } else {
                            throw new JsonDataException("Unexpected token while deserializing \"features\" in \"rule\"!");
                        }
                    }
                }
            }
            reader.endObject();

            if (action == null) throw new RuntimeException("Failed to serialize \"rule\"!");

            return new Rule(action, os, features);
        }

        @Override
        public void toJson(final JsonWriter writer, final Rule value) throws IOException {
            if (value == null) throw new NullPointerException("Cannot serialize null object!");
            writer.beginObject();

            writer.name("action");
            moshi.adapter(Rule.Action.class).toJson(writer, value.action());

            if (value.os() != null) {//May be null
                writer.name("os");
                moshi.adapter(Rule.OS.class).toJson(writer, value.os());
            }

            if (value.features() != null) {//May be null
                writer.name("features");
                moshi.adapter(Map.class).toJson(writer, value.features());
            }

            writer.endObject();
        }

    }

    @RequiredArgsConstructor
    public enum Action {

        ALLOW("allow", true),

        DISALLOW("disallow", false);

        public final String action;
        public final boolean value;

        public static final class Adapter extends JsonAdapter<Rule.Action> {

            @Override
            public Rule.Action fromJson(final JsonReader reader) throws IOException {
                String action = reader.nextString();
                for (Rule.Action i : values()) {
                    if (i.action.equals(action)) return i;
                }
                return null;
            }

            @Override
            public void toJson(final JsonWriter writer, final Rule.Action value) throws IOException {
                if (value == null) throw new NullPointerException("Cannot serialize null object!");
                writer.value(value.action);
            }

        }

    }

    public record OS(@Nullable String name, @Nullable String version, @Nullable String arch, @Nullable Rule.OS.VersionRange versionRange) {

		/**
		 * Introduced in Minecraft 26.1
		 * @param min
		 * @param max
		 */
		public record VersionRange(@Nullable String min, @Nullable String max) {

			@RequiredArgsConstructor
			public static final class Adapter extends JsonAdapter<Rule.OS.VersionRange> {

				@Override
				public VersionRange fromJson(final JsonReader jsonReader) throws IOException {
					String min = null;
					String max = null;

					jsonReader.beginObject();
					while(jsonReader.hasNext()) {
						switch(jsonReader.nextName()) {
							case "min" -> min = jsonReader.nextString();
							case "max" -> max = jsonReader.nextString();
						}
					}
					jsonReader.endObject();
					return new Rule.OS.VersionRange(min, max);
				}

				@Override
				public void toJson(final JsonWriter jsonWriter, final VersionRange versionRange) throws IOException {
					if (versionRange == null) throw new NullPointerException("Cannot serialize null object!");
					jsonWriter.beginObject();

					if (versionRange.min() != null) jsonWriter.name("min").value(versionRange.min());
					if (versionRange.max() != null) jsonWriter.name("max").value(versionRange.max());

					jsonWriter.endObject();
				}

			}

		}

		@RequiredArgsConstructor
        public static final class Adapter extends JsonAdapter<Rule.OS> {

			private final Moshi moshi;

            @Override
            public OS fromJson(final JsonReader reader) throws IOException {
                String name = null, version = null;
                String arch = null;
				Rule.OS.VersionRange versionRange = null;

                reader.beginObject();
                while (reader.hasNext()) {
                    switch (reader.nextName()) {
                        case "name" -> name = reader.nextString();
						case "versionRange" -> versionRange = moshi.adapter(Rule.OS.VersionRange.class).fromJson(reader);
                        case "version" -> version = reader.nextString();
                        case "arch" -> arch = reader.nextString();
                    }
                }
                reader.endObject();
                return new OS(name, version, arch, versionRange);
            }

            @Override
            public void toJson(final JsonWriter writer, final OS value) throws IOException {
                if (value == null) throw new NullPointerException("Cannot serialize null object!");
                writer.beginObject();


				if (value.name() != null) {
                    writer.name("name")
                            .value(value.name());
                }

				if (value.versionRange() != null) {
					writer.name("versionRange");
					moshi.adapter(VersionRange.class).toJson(writer, value.versionRange());
				}

                if (value.version() != null) {
                    writer.name("version")
                            .value(value.version());
                }

                if (value.arch() != null) {
                    writer.name("arch")
                            .value(value.arch());
                }

                writer.endObject();
            }

        }

    }

}
