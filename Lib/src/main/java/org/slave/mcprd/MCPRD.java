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
import java.io.OutputStreamWriter;
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
import java.util.regex.Pattern;

public final class MCPRD {

    public final Moshi moshi;

    private VersionManifest versionManifest = null;
    private Version version = null;

    public MCPRD() {
        moshi = new Moshi.Builder()
                .add(new AdapterFactory())
                .build();
    }

    public void download(final String mcpDir, final String mcVersion, final boolean ignoreMCP, final boolean dlJars, final boolean clientOnly, final boolean serverOnly, final boolean dlLibraries, final boolean dlNatives, final boolean linux, final boolean windows, final boolean osx, final boolean dlResources, final boolean proper, final boolean overwrite) throws RuntimeException, IOException {
        VersionManifest versionManifest;
        try {
            System.out.println("Getting \"version_manifest_v2.json\"...");
            versionManifest = getVersionManifest();
        } catch(IOException e) {
            throw new RuntimeException("Failed to get \"version_manifest_v2.json\"!", e);
        }
        System.out.println("Done getting version manifest.\n");

        if (mcpDir == null && mcVersion == null) {//Just print out all versions
            System.out.println("Printing out all Minecraft versions...\n");
            for(VersionManifest.Version version : versionManifest.versions()) {
                System.out.println(version.id());
            }
            System.out.println("\nDone");
            return;
        }

        if (mcpDir == null) throw new FileNotFoundException("No MCP directory was set!");
        if (mcVersion == null) throw new NullPointerException("No Minecraft version was set!");

        VersionManifest.Version manifestVersion = null;
        for(VersionManifest.Version i : versionManifest.versions()) {
            if(i.id().equals(mcVersion)) {
                manifestVersion = i;
                break;
            }
        }

        if (manifestVersion != null) {
            if (version == null || !mcVersion.equals(version.id())) {
                System.out.println("Getting version JSON...");
                try {
                    version = getVersion(manifestVersion);
                } catch(IOException e) {
                    throw new RuntimeException("Failed to get version!", e);
                }
                System.out.println("Done getting version JSON.\n");
            }
        }

        if (version == null) throw new NullPointerException("Failed to get version?!");

        File dirMCP = new File(mcpDir);
        if (!dirMCP.exists()) throw new FileNotFoundException(String.format("MCP directory \"%s\" does not exist!", mcpDir));
        if (!dirMCP.isDirectory()) throw new FileNotFoundException(String.format("Selected path \"%s\" is not a directory!", mcpDir));
        if (!ignoreMCP && !new File(dirMCP, "docs/README-MCP.TXT").isFile()) throw new FileNotFoundException(String.format("Selected path \"%s\" is not not a valid MCP directory!", mcpDir));

        File dirJars = new File(dirMCP, "jars");
        File dirJarsBin = new File(dirJars, "bin");//pre-1.6
        File dirLibraries = new File(dirJars, "libraries");//legacy
        File dirJarsResources = new File(dirJars, "resources");//pre-1.6
        File dirNatives;
        File dirJarsVersions = new File(dirJars, "versions");//legacy
        File dirJarsVersionsID = new File(dirJarsVersions, version.id());//legacy

        if (!dirJars.exists()) {
            if (!dirJars.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirJars.getPath()));
        }

        switch(version.assets()) {
            case PRE_1_6 -> dirNatives = new File(dirJarsBin, "natives");
            case LEGACY -> dirNatives = new File(dirJarsVersionsID, String.format("%s-natives", version.id()));
            default -> throw new RuntimeException(String.format("Unexpected assets ID \"%s\"!", version.assets().assets));
        }

        if (dlJars) {
            System.out.println("Downloading jar files...");
            switch(version.assets()) {
                case PRE_1_6 -> {
                    if (!dirJarsBin.exists()) {
                        if (!dirJarsBin.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirJarsBin.getPath()));
                    }
                }
                case LEGACY -> {
                    if (!dirJarsVersions.exists()) {
                        if (!dirJarsVersions.mkdirs()) throw new RuntimeException(String.format("Failed to create directory \"%s\"!", dirJarsVersions.getPath()));
                    }
                    if (!dirJarsVersionsID.exists()) {
                        if (!dirJarsVersionsID.mkdirs()) throw new RuntimeException(String.format("Failed to create directory \"%s\"!", dirJarsVersionsID.getPath()));
                    }
                }
                default -> throw new RuntimeException(String.format("Unexpected assets ID \"%s\"!", version.assets().assets));
            }
            downloadMinecraftJars(dirJars, dirJarsBin, dirJarsVersionsID, version, clientOnly, serverOnly, overwrite);
            System.out.println("Done downloading jar files!\n");
        }
        if (dlLibraries) {
            System.out.println("Downloading library files...");
            File dir;
            switch(version.assets()) {
                case PRE_1_6 -> {
                    if (!dirJarsBin.exists()) {
                        if (!dirJarsBin.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirJarsBin.getPath()));
                    }
                    dir = dirJarsBin;
                }
                case LEGACY -> {
                    if (!dirLibraries.exists()) {
                        if (!dirLibraries.mkdirs()) throw new RuntimeException(String.format("Failed to create directory \"%s\"!", dirLibraries.getPath()));
                    }
                    dir = dirLibraries;
                }
                default -> throw new RuntimeException(String.format("Unexpected assets ID \"%s\"!", version.assets().assets));
            }
            downloadLibraries(dir, version, overwrite);
            System.out.println("Done downloading library files!\n");
        }
        if (dlNatives) {
            System.out.println("Downloading native files...");
            if (!dirNatives.exists()) {
                if (!dirNatives.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirNatives.getPath()));
            }
            downloadAndExtractNatives(dirNatives, version, linux, windows, osx, overwrite);
            System.out.println("Done downloading native files!\n");
        }
        if (dlResources) {
            System.out.println("Downloading asset files...");

            Assets assets;
            try {
                assets = getAssets(version);
            } catch(IOException e) {
                throw new RuntimeException("Failed to get assets index!", e);
            }

            File dir;
            if (assets.virtual()) {//legacy (1.6+)
                File dirAssets = new File(dirJars, "assets");
                File dirAssetsIndexes = new File(dirAssets, "indexes");
                File dirAssetsObjects = new File(dirAssets, "objects");

                if (!dirAssets.exists()) {
                    if (!dirAssets.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirAssets.getPath()));
                }
                if (proper) {
                    if (!dirAssetsIndexes.exists()) {
                        if (!dirAssetsIndexes.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirAssetsIndexes.getPath()));
                    }
                    if (!dirAssetsObjects.exists()) {
                        if (!dirAssetsObjects.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirAssetsObjects.getPath()));
                    }
                    serializeJSON(
                            new File(dirAssetsIndexes, String.format("%s.json", version.assets().assets)),
                            Assets.class,
                            assets,
                            overwrite
                    );//Serialize instead of downloading to avoid additional network calls
                    dir = dirAssetsObjects;
                } else {
                    dir = dirAssets;
                }
            } else if (assets.map_to_resources()) {//pre-1.6
                dir = dirJarsResources;
            } else {
                throw new RuntimeException("Failed to correctly set asset directory! This is unexpected!");
            }

            if (!dir.exists()) {
                if (!dir.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dir.getPath()));
            }

            downloadResources(dir, assets, proper, overwrite);
            System.out.println("Done downloading asset files!\n");
        }
    }

    /**
     * @param dirJars jars (for server)
     * @param dirJarsBin jars/bin (for pre-1.6 client)
     * @param dirJarsVersionsID client - jars/bin (for pre-1.6), versions/{id} (for legacy - 1.6)
     * @param version
     * @param client Should download client jar
     * @param server Should download server jar
     * @param overwrite Should overwrite any existing file
     */
    public void downloadMinecraftJars(@NotNull final File dirJars, final File dirJarsBin, @NotNull final File dirJarsVersionsID, @NotNull final Version version, final boolean client, final boolean server, final boolean overwrite) {
        URL url;
        File dest;

        int status = 0;
        if (client) {
            try {
                url = new URL(version.downloads().client().url());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            if (version.assets() == Version.Assets.PRE_1_6) {//dir expects to be "jars/bin"
                dest = new File(dirJarsBin, "minecraft.jar");
            } else if (version.assets() == Version.Assets.LEGACY) {//dir expects to be "jars"
                dest = new File(dirJarsVersionsID, String.format("%s.jar", version.id()));
                serializeJSON(
                        new File(dirJarsVersionsID, String.format("%s.json", version.id())),
                        Version.class,
                        version,
                        overwrite
                );//Serialize instead of downloading to avoid additional network calls
            } else {
                throw new RuntimeException("Failed to download client jar due to unexpected asset ID!");
            }

            System.out.println("Downloading client jar...");
            try {
                downloadFile(url, dest, overwrite);
            } catch(IOException e) {
                throw new RuntimeException("Failed to download client jar!", e);
            }
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
            try {
                downloadFile(url, dest, overwrite);
            } catch(IOException e) {
                throw new RuntimeException("Failed to download server jar!", e);
            }
            System.out.println("Done downloading server jar");
            status += 1;
        }
        if (status == 0) System.out.println("Didn't download any jar files?!");
    }

    public void downloadAndExtractNatives(@NotNull File dirNatives, @NotNull final Version version, final boolean linux, final boolean windows, final boolean osx, final boolean overwrite) {
        List<Version.Library> natives = downloadNatives(dirNatives, version, linux, windows, osx, overwrite);
        extractNatives(dirNatives, natives, linux, windows, osx, overwrite);
    }

    private List<Version.Library> downloadNatives(@NotNull File dirNatives, @NotNull final Version version, final boolean linux, final boolean windows, final boolean osx, final boolean overwrite) {
        List<Version.Library> natives = new ArrayList<>();

        for(Version.Library library : version.libraries()) {
            if (library.natives() == null) continue;
            if (!checkLibraryRules(library)) {
                System.out.println(String.format("Disallowed native \"%s\" for this OS", library.name()));
            } else {
                natives.add(library);
            }
        }

        for(Version.Library library : natives) {
            if (linux && library.downloads().classifiers().natives_linux() != null) {
                downloadNative(dirNatives, library.downloads().classifiers().natives_linux(), overwrite);
            }
            if (windows && library.downloads().classifiers().natives_windows() != null) {
                downloadNative(dirNatives, library.downloads().classifiers().natives_windows(), overwrite);
            }
            if (osx && library.downloads().classifiers().natives_osx() != null) {
                downloadNative(dirNatives, library.downloads().classifiers().natives_osx(), overwrite);
            }
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

        File fileNative = new File(dirNatives, artifact.path().substring(artifact.path().lastIndexOf('/')));
        try {
            downloadFile(url, fileNative, overwrite);
        } catch(IOException e) {
            throw new RuntimeException(String.format("Failed to download native \"%s\"!", fileNative), e);
        }
    }

    private void extractNatives(@NotNull final File dirNatives, @NotNull final List<Version.Library> nativeFiles, final boolean linux, final boolean windows, final boolean osx, final boolean overwrite) {
        for(Version.Library _native : nativeFiles) {
            if (linux && _native.downloads().classifiers().natives_linux() != null) {
                extractNative(dirNatives, _native, _native.downloads().classifiers().natives_linux(), overwrite);
            }
            if (windows && _native.downloads().classifiers().natives_windows() != null) {
                extractNative(dirNatives, _native, _native.downloads().classifiers().natives_windows(), overwrite);
            }
            if (osx && _native.downloads().classifiers().natives_osx() != null) {
                extractNative(dirNatives, _native, _native.downloads().classifiers().natives_osx(), overwrite);
            }
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

    public void downloadLibraries(@NotNull File dir, @NotNull final Version version, final boolean overwrite) {
        List<Version.Library> toDownload = new ArrayList<>();
        for(Version.Library library : version.libraries()) {
            if (library.natives() != null) continue;
            if (checkLibraryRules(library)) {
                toDownload.add(library);
            } else {
                System.out.println(String.format("Disallowed library \"%s\" for this OS", library.name()));
            }
        }

        for(Version.Library library : toDownload) {
            System.out.println(String.format("Downloading library \"%s\"...", library.name()));

            File fileLibrary;
            switch(version.assets()) {
                case PRE_1_6 -> {//dir should be jars/bin
                    fileLibrary = new File(dir, String.format("%s.jar", library.name().split(":", 3)[1]));
                }
                case LEGACY -> {//dir should be jars/libraries
                    if (library.downloads().artifact() != null) {
                        fileLibrary = new File(dir, library.downloads().artifact().path());
                        if (!fileLibrary.getParentFile().exists()) {
                            if (!fileLibrary.getParentFile().mkdirs()) throw new RuntimeException(String.format("Failed to create file directory \"%s\"!", fileLibrary.getParentFile().getPath()));
                        }
                    } else {
                        continue;
                    }
                }
                default -> throw new RuntimeException(String.format("Unexpected asset! \"%s\"", version.assets()));
            }

            try {
                downloadFile(
                        new URL(library.downloads().artifact().url()),
                        fileLibrary,
                        overwrite
                );
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to download library \"%s\"!", library.name()), e);
            }

            System.out.println(String.format("Done downloading library \"%s\"!", library.name()));
        }
    }

    public void downloadResources(@NotNull final File dir, @NotNull final Assets assets, final boolean proper, final boolean overwrite) {
        for(Entry<String, Asset> entry : assets.objects().entrySet()) {
            File file = null;
            if (assets.map_to_resources() || (!proper && assets.virtual())) {
                file = new File(dir, entry.getKey());//dir is expected to be set to "jars/resources", or "jars/assets" if not proper and virtual
                if (!file.getParentFile().exists()) {
                    if (!file.getParentFile().mkdirs()) throw new RuntimeException(String.format("Failed to create directory \"%s\"!", file.getParentFile().getPath()));
                }
            } else {
                if (assets.virtual()) {
                    String dirAssetObjectName = entry.getValue().hash().substring(0, 2);//FIXME This does not seem right...
                    File dirAssetObject = new File(dir, dirAssetObjectName);//dir is expected to be set to "jars/assets/objects"
                    if (!dirAssetObject.exists()) {
                        if (!dirAssetObject.mkdirs()) throw new RuntimeException(String.format("Failed to create directory \"%s\"!", dirAssetObject.getPath()));
                    }
                    file = new File(dirAssetObject, entry.getValue().hash());
                }
            }
            if (file == null) throw new NullPointerException(String.format("Failed to get path for asset file \"%s\"!", entry.getKey()));
            downloadResource(file, entry.getValue(), overwrite);
        }
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
        throw new NullPointerException("Failed to parse JSON!");
    }

    private void downloadResource(@NotNull final File fileAsset, @NotNull final Assets.Asset asset, final boolean overwrite) {
        if (fileAsset.exists()) {
            if (!overwrite) {
                System.out.println(String.format("Asset \"%s\" already exists and can't overwrite", fileAsset.getPath()));
                return;
            } else {
                System.out.println(String.format("Asset \"%s\" already exists, but overwriting...", fileAsset.getPath()));
            }
        }

        URL urlFile;
        try {
            urlFile = new URL(String.format(Constants.URL_RESOURCE, asset.hash().substring(0, 2), asset.hash()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        System.out.println(String.format("Downloading asset file \"%s\"...", fileAsset.getPath()));
        try {
            downloadFile(urlFile, fileAsset, overwrite);
        } catch(IOException e) {
            throw new RuntimeException(String.format("Failed to download asset file \"%s\"!", fileAsset.getName()), e);
        }
        System.out.println("Done downloading asset file!\n");
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

    private boolean checkLibraryRules(final @NotNull Version.Library library) {
        boolean allow = true;//WTF - jinput has no rule
        if (library.rules() != null) {
            for(Version.Library.Rule rule : library.rules()) {
                if (rule.os() == null) {
                    switch(rule.action()) {
                        case ALLOW -> allow = true;
                        case DISALLOW -> allow = false;
                    }
                } else {//TODO nightly version is used for OSX exclusively
                    boolean matchesOS = rule.os().name().startsWith(System.getProperty("os.name").toLowerCase());
                    boolean matchesOSVersion = false;
                    if (rule.os().version() != null) matchesOSVersion = Pattern.compile(rule.os().version()).matcher(System.getProperty("os.version")).matches();
                    if (matchesOS && matchesOSVersion) {
                        switch(rule.action()) {
                            case ALLOW -> allow = true;
                            case DISALLOW -> allow = false;
                        }
                    }
                }
                //TODO Prefer non-nightly version over nightly
            }
        }
        return allow;
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

    private <T> void serializeJSON(final File file, final Class<T> classObject, final T object, final boolean overwrite) {
        if (file.exists()) {
            if (!overwrite) {
                System.out.println(String.format("File \"%s\" exists, but can't overwrite!", file.getPath()));
                return;
            } else {
                System.out.println(String.format("File \"%s\" exists, but overwriting...", file.getPath()));
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(
                    moshi.adapter(classObject)
                            .toJson(object)
            );
            outputStreamWriter.flush();
            outputStreamWriter.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write assets JSON file \"%s\"!", file.getPath()), e);
        }
    }

    private void downloadFile(@NotNull final URL urlFile, final File file, final boolean overwrite) throws IOException {
        if (!file.getParentFile().exists()) throw new RuntimeException(String.format("Parent directory \"%s\" does not exist!", file.getParentFile().getPath()));
        if (file.isFile()) {
            if (!overwrite) {
                System.out.println(String.format("File \"%s\" already exists but can't overwrite", file.getPath()));
                return;
            } else {
                System.out.println(String.format("File \"%s\" already exists, but overwriting...", file.getPath()));
            }
        }
        ReadableByteChannel readableByteChannel = Channels.newChannel(urlFile.openStream());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);//Will ONLY transfer up to 16 MB... but that shouldn't be an issue...
        }
    }

}
