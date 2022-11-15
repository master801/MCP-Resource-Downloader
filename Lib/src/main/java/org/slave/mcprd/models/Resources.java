package org.slave.mcprd.models;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public record Resources(boolean map_to_resources, Map<String, ResourceObject> objects, boolean virtual) {

    @RequiredArgsConstructor
    public static final class Adapter extends JsonAdapter<Resources> {

        private final Moshi moshi;

        @Override
        public Resources fromJson(final JsonReader reader) throws IOException {
            boolean map_to_resources = false;
            Map<String, ResourceObject> objects = new HashMap<>();
            boolean virtual = false;

            reader.beginObject();
            while(reader.hasNext()) {
                switch(reader.nextName()) {
                    case "map_to_resources" -> map_to_resources = reader.nextBoolean();
                    case "objects" -> {
                        reader.beginObject();
                        while(reader.hasNext()) {
                            objects.put(
                                    reader.nextName(), moshi.adapter(ResourceObject.class).fromJson(reader)
                            );
                        }
                        reader.endObject();
                    }
                    case "virtual" -> virtual = reader.nextBoolean();
                }
            }
            reader.endObject();
            return new Resources(map_to_resources, objects, virtual);
        }

        @Override
        public void toJson(final JsonWriter writer, final Resources value) throws IOException {
        }

    }

    public record ResourceObject(String hash, int size) {

        public static final class Adapter extends JsonAdapter<ResourceObject> {

            @Override
            public ResourceObject fromJson(final JsonReader reader) throws IOException {
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
                return new ResourceObject(hash, size);
            }

            @Override
            public void toJson(final JsonWriter writer, final ResourceObject value) throws IOException {
            }

        }

    }

}
