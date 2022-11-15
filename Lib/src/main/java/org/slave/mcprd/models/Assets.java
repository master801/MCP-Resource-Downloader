package org.slave.mcprd.models;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public record Assets(boolean map_to_resources, Map<String, Asset> objects, boolean virtual) {

    @RequiredArgsConstructor
    public static final class Adapter extends JsonAdapter<Assets> {

        private final Moshi moshi;

        @Override
        public Assets fromJson(final JsonReader reader) throws IOException {
            boolean map_to_resources = false;
            Map<String, Asset> objects = null;
            boolean virtual = false;

            reader.beginObject();
            while(reader.hasNext()) {
                switch(reader.nextName()) {
                    case "map_to_resources" -> map_to_resources = reader.nextBoolean();
                    case "objects" -> {
                        objects = new HashMap<>();
                        reader.beginObject();
                        while(reader.hasNext()) {
                            objects.put(
                                    reader.nextName(),
                                    moshi.adapter(Assets.Asset.class)
                                            .fromJson(reader)
                            );
                        }
                        reader.endObject();
                    }
                    case "virtual" -> virtual = reader.nextBoolean();
                }
            }
            reader.endObject();
            return new Assets(map_to_resources, objects, virtual);
        }

        @Override
        public void toJson(final JsonWriter writer, final Assets value) throws IOException {
        }

    }

    public record Asset(String hash, int size) {

        public static final class Adapter extends JsonAdapter<Asset> {

            @Override
            public Asset fromJson(final JsonReader reader) throws IOException {
                String hash = null;
                int size = -1;

                reader.beginObject();
                while(reader.hasNext()) {
                    switch(reader.nextName()) {
                        case "hash" -> hash = reader.nextString();
                        case "size" -> size = reader.nextInt();
                    }
                }
                reader.endObject();
                return new Asset(hash, size);
            }

            @Override
            public void toJson(final JsonWriter writer, final Asset value) throws IOException {
            }

        }

    }

}
