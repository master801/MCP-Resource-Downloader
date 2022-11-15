package org.slave.mcprd;

import com.squareup.moshi.Moshi;
import org.jetbrains.annotations.NotNull;
import org.slave.mcprd.json.AdapterFactory;
import org.slave.mcprd.models.Assets;
import org.slave.mcprd.models.Assets.Asset;
import org.slave.mcprd.models.Version;
import org.slave.mcprd.models.VersionManifest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class MCPRD {

    public final Moshi moshi;

    private VersionManifest versionManifest = null;
    private Version version = null;

    public MCPRD() {
        moshi = new Moshi.Builder()
                .add(new AdapterFactory())
                .build();
    }

    public void download(final String mcpDir, final String mcVersion, final boolean ignoreMCP, final boolean dlJars, final boolean clientOnly, final boolean serverOnly, final boolean dlLibraries, final boolean dlNatives, final boolean dlResources, final boolean overwrite) throws RuntimeException, IOException {
        VersionManifest versionManifest;
        try {
            versionManifest = getVersionManifest();
        } catch(IOException e) {
            throw new RuntimeException("Failed to get \"version_manifest_v2.json\"!", e);
        }

        VersionManifest.Version manifestVersion = null;
        for(VersionManifest.Version i : versionManifest.versions()) {
            if(i.id().equals(mcVersion)) {
                manifestVersion = i;
                break;
            }
        }

        if (manifestVersion != null) {
            if (version == null) {
                try {
                    version = getVersion(manifestVersion);
                } catch(IOException e) {
                    throw new RuntimeException("Failed to get version!", e);
                }
            }
            if (version != null) {
                File dirMCP = new File(mcpDir);
                if (!dirMCP.exists()) throw new FileNotFoundException(String.format("MCP Directory \"%s\" does not exist!", mcpDir));
                if (!dirMCP.isDirectory()) throw new FileNotFoundException(String.format("Selected path \"%s\" is not a directory!", mcpDir));
                if (!ignoreMCP && !new File(dirMCP, "docs/README-MCP.TXT").isFile()) throw new FileNotFoundException(String.format("Selected path \"%s\" is not not a valid MCP directory!", mcpDir));

                File dirJars = new File(dirMCP, "jars");
                File dirJarsBin = new File(dirJars, "bin");
                File dirJarsResources = new File(dirJars, "resources");
                File dirNatives = new File(dirJarsBin, "natives");

                if (!dirJars.exists()) {
                    if (!dirJars.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirJars.getPath()));
                }
                if (!dirJarsBin.exists()) {
                    if (!dirJarsBin.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirJarsBin.getPath()));
                }
                if (!dirNatives.exists()) {
                    if (!dirNatives.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirNatives.getPath()));
                }

                if (dlJars) {
                    System.out.println("Downloading jar files...");
                    downloadMinecraftJars(dirJars, dirJarsBin, version, clientOnly, serverOnly, overwrite);
                    System.out.println("Done downloading jar files!\n");
                }
                if (dlLibraries) {
                    System.out.println("Downloading library files...");
                    downloadLibraries(dirJarsBin, version.libraries(), overwrite);
                    System.out.println("Done downloading library files!\n");
                }
                if (dlNatives) {
                    System.out.println("Downloading native files...");
                    downloadAndExtractNatives(dirNatives, version, overwrite);
                    System.out.println("Done downloading native files!\n");
                }
                if (dlResources) {
                    if (!dirJarsResources.exists()) {
                        if (!dirJarsResources.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirJarsResources.getPath()));
                    }

                    System.out.println("Downloading resource files...");
                    downloadResources(dirJarsResources, version, overwrite);
                    System.out.println("Done downloading resource files!\n");
                }

                //TODO Support 1.6
            }
        }
    }

    /**
     * Download client.jar and server.jar
     * @param version
     */
    public void downloadMinecraftJars(final File dirJars, final File dirJarsBin, @NotNull final Version version, final boolean client, final boolean server, final boolean overwrite) {
        URL url;
        File dest;

        int status = 0;
        if (client) {
            try {
                url = new URL(version.downloads().client().url());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            dest = new File(dirJarsBin, "minecraft.jar");
            System.out.println("Downloading client jar...");
            downloadFile(url, dest, overwrite);
            System.out.println("Done downloading client jar");
            status += 1;
        }
        if (server) {
            try {
                url = new URL(version.downloads().client().url());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            dest = new File(dirJars, "minecraft_server.jar");
            System.out.println("Downloading server jar...");
            downloadFile(url, dest, overwrite);
            System.out.println("Done downloading server jar");
            status += 1;
        }
        if (status == 0) System.out.println("Didn't download any jar files?!");
    }

    public void downloadAndExtractNatives(@NotNull File dirNatives, @NotNull final Version version, final boolean overwrite) {
        List<Version.Library> natives = downloadNatives(dirNatives, version.libraries(), overwrite);
        extractNatives(dirNatives, natives, overwrite);
    }

    private List<Version.Library> downloadNatives(@NotNull File dirNatives, @NotNull final Version.Library[] libraries, final boolean overwrite) {
        List<Version.Library> natives = new ArrayList<>();
        for(Version.Library library : libraries) {
            String[] maven = library.name().split(":", 3);//group, artifact, version
            if (maven[2].contains("nightly")) continue;//Don't know why they have 2 different versions...
            switch(maven[0]) {
                case "org.lwjgl.lwjgl" -> {
                    switch(maven[1]) {
                        case "lwjgl-platform" -> natives.add(library);
                    }
                }
                case "net.java.jinput" -> {
                    switch(maven[1]) {
                        case "jinput-platform" -> natives.add(library);//WTF duplicate entry in 1.2.5?!!
                    }
                }
            }
        }

        for(Version.Library library : natives) {
            downloadNative(dirNatives, library.downloads().classifiers().natives_linux(), overwrite);
            downloadNative(dirNatives, library.downloads().classifiers().natives_osx(), overwrite);
            downloadNative(dirNatives, library.downloads().classifiers().natives_windows(), overwrite);
        }
        return natives;
    }

    private void downloadNative(@NotNull final File dirNatives, @NotNull final Version.Library.Downloads.Artifact artifact, final boolean overwrite) {
        URL url;
        try {
            url = new URL(artifact.url());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        downloadFile(
                url,
                new File(dirNatives, artifact.path().substring(artifact.path().lastIndexOf('/'))),
                overwrite
        );
    }

    private void extractNatives(@NotNull final File dirNatives, @NotNull final List<Version.Library> nativeFiles, final boolean overwrite) {
        for(Version.Library _native : nativeFiles) {
            extractNative(dirNatives, _native, _native.downloads().classifiers().natives_windows(), overwrite);
            extractNative(dirNatives, _native, _native.downloads().classifiers().natives_linux(), overwrite);
            extractNative(dirNatives, _native, _native.downloads().classifiers().natives_osx(), overwrite);
        }
    }

    private void extractNative(@NotNull final File dirNatives, @NotNull final Version.Library library, @NotNull final Version.Library.Downloads.Artifact nativeArtifact, final boolean overwrite) {
        File nativeFile = new File(dirNatives, nativeArtifact.path().substring(nativeArtifact.path().lastIndexOf('/')));
        if (!nativeFile.exists()) throw new RuntimeException(new FileNotFoundException(String.format("Could not find downloaded native \"%s\"", nativeFile.getPath())));

        System.out.println(String.format("Extracting native jar \"%s\" to \"%s\"", nativeFile.getName(), dirNatives.getPath()));

        JarFile nativeJar;
        try {
            nativeJar = new JarFile(nativeFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Enumeration<JarEntry> enumeration = nativeJar.entries();
        while(enumeration.hasMoreElements()) {
            JarEntry jarEntry = enumeration.nextElement();
            boolean exclude = false;
            if (library.extract() != null && library.extract().exclude() != null) {
                for(String i : library.extract().exclude()) {
                    if (jarEntry.getName().startsWith(i)) {
                        exclude = true;
                        break;
                    }
                }
            }
            if (exclude) {
                System.out.println(String.format("Not extracting excluded entry \"%s\"...", jarEntry.getName()));
                continue;
            }

            InputStream inputStream;
            try {
                inputStream = nativeJar.getInputStream(jarEntry);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            File extractFile = new File(dirNatives, jarEntry.getName());
            if (!extractFile.getParentFile().exists()) {//This shouldn't happen for natives, but just in case...
                if (!extractFile.getParentFile().mkdirs()) throw new RuntimeException(String.format("Failed to create file directory \"%s\"!", extractFile.getParentFile().getPath()));
            }
            if (extractFile.exists() && !overwrite) {
                System.out.println(String.format("Native \"%s\" already exists. Skipping...", extractFile.getPath()));
                continue;
            }

            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(extractFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                fileOutputStream.getChannel().transferFrom(Channels.newChannel(inputStream), 0, Long.MAX_VALUE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(String.format("Extracted native \"%s\"", extractFile.getPath()));
        }

        try {
            nativeJar.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Done extracting native jar\n");
    }

    /**
     * Download libraries
     *
     * @param libraries
     */
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public void downloadLibraries(@NotNull File dirBin, @NotNull final Version.Library[] libraries, final boolean overwrite) {//jinput, lwjgl, lwjgl_util
        List<Version.Library> toDownload = new ArrayList<>();
        for(Version.Library library : libraries) {
            String[] maven = library.name().split(":", 3);//group, artifact, version
            if (maven[2].contains("nightly")) continue;//Don't know why they have 2 different versions...
            switch(maven[0]) {
                case "org.lwjgl.lwjgl" -> {
                    switch(maven[1]) {
                        case "lwjgl", "lwjgl_util" -> toDownload.add(library);
                    }
                }
                case "net.java.jinput" -> {
                    switch(maven[1]) {
                        case "jinput" -> toDownload.add(library);
                    }
                }
            }
        }

        for(Version.Library library : toDownload) {
            String[] maven = library.name().split(":", 3);
            try {
                System.out.println(String.format("Downloading library \"%s\"...", library.name()));
                downloadFile(
                        new URL(library.downloads().artifact().url()),
                        new File(dirBin, String.format("%s.jar", maven[1])),
                        overwrite
                );
                System.out.println(String.format("Done downloading library \"%s\"!", library.name()));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void downloadResources(@NotNull final File dirJarsResources, @NotNull final Version version, final boolean overwrite) {
        Assets assets;
        try {
            assets = getAssets(version);
        } catch(IOException e) {
            throw new RuntimeException("Failed to get assets index!", e);
        }
        if (assets == null) {
            System.out.println("Failed to get asset index!");
            return;
        }

        System.out.println("Downloading asset files...");
        for(Entry<String, Asset> entry : assets.objects().entrySet()) {
            File fileResource = new File(dirJarsResources, entry.getKey());
            if (!fileResource.getParentFile().exists()) {
                if (!fileResource.getParentFile().mkdirs()) throw new RuntimeException(String.format("Failed to create file directory \"%s\"!", fileResource.getParentFile().getPath()));
            }
            downloadResource(fileResource, entry.getValue(), overwrite);
        }
        System.out.println("Done downloading asset files!");
    }

    private Assets getAssets(@NotNull final Version version) throws IOException {
        URL url;
        try {
            url = new URL(version.assetIndex().url());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        String fetchedJSON = fetchJSON(url);
        if (!fetchedJSON.isEmpty()) {
            return moshi.adapter(Assets.class)
                    .fromJson(fetchedJSON);
        }
        return null;
    }

    private void downloadResource(@NotNull final File fileResource, @NotNull final Assets.Asset asset, final boolean overwrite) {
        if (fileResource.exists() && !overwrite) {
            return;
        }

        URL urlFile;
        try {
            urlFile = new URL(String.format(Constants.URL_RESOURCE, asset.hash().substring(0, 2), asset.hash()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        System.out.println(String.format("Downloading resource file \"%s\"...", fileResource.getPath()));
        downloadFile(urlFile, fileResource, overwrite);
        System.out.println("Done");
    }

    /**
     * Fetches and parses version_manifest_v2.json
     * @return {@link VersionManifest}
     */
    public VersionManifest getVersionManifest() throws IOException {
        if (versionManifest == null) {
            URL url = new URL(Constants.URL_VERSION_MANIFEST_V2);
            String fetchedJSON = fetchJSON(url);
            if (!fetchedJSON.isEmpty()) {
                return versionManifest = moshi.adapter(VersionManifest.class)
                        .fromJson(fetchedJSON);
            }
        }
        return versionManifest;
    }

    /**
     * Fetches and parses {version}.json
     * @param version Manifest Version to fetch
     * @return {@link org.slave.mcprd.models.Version} if JSON was fetched and parsed correctly. null if otherwise
     */
    public Version getVersion(final VersionManifest.Version version) throws IOException {
        URL url = new URL(version.url());
        String fetchedJSON = fetchJSON(url);
        if (!fetchedJSON.isEmpty()) {
            return moshi.adapter(Version.class)
                    .fromJson(fetchedJSON);
        }
        return null;
    }

    private String fetchJSON(final URL url) throws IOException {
        InputStream inputStream = url.openStream();

        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        StringBuilder lines = new StringBuilder();

        String buffer;
        while((buffer = bufferedReader.readLine()) != null) lines.append(buffer);
        return lines.toString();
    }

    private void downloadFile(@NotNull final URL urlFile, final File file, final boolean overwrite) {
        if (!file.getParentFile().exists()) throw new RuntimeException(String.format("Parent directory \"%s\" does not exist!", file.getParentFile().getPath()));
        if (file.isFile() && !overwrite) {
            System.out.println(String.format("File \"%s\" already exists but can't overwrite", file.getPath()));
            return;
        }
        try {
            ReadableByteChannel readableByteChannel = Channels.newChannel(urlFile.openStream());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);//Will ONLY transfer up to 16 MB... but that shouldn't be an issue...
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
