package org.slave.mcprd.models;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
            if (value == null) throw new NullPointerException("Cannot serialize a null object!");
            writer.beginObject();
            //Mojang's JSON has map_to_resources or virtual. Either may be missing - IDK why.
            if (value.map_to_resources()) {
                writer.name("map_to_resources").value(true);
            }

            writer.name("objects")
                    .beginObject();
            for(Entry<String, Asset> entry : value.objects().entrySet()) {
                writer.name(entry.getKey());
                moshi.adapter(Assets.Asset.class)
                        .toJson(writer, entry.getValue());
            }
            writer.endObject();

            if (value.virtual()) {
                writer.name("virtual").value(true);
            }
            writer.endObject();
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
                if (value == null) throw new NullPointerException("Cannot serialize a null object!");
                writer.beginObject()
                        .name("hash").value(value.hash())
                        .name("size").value(value.size())
                        .endObject();
            }

        }

    }

}
