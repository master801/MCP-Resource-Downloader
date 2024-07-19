package org.slave.mcprd;

import com.squareup.moshi.Moshi;
import org.jetbrains.annotations.NotNull;
import org.slave.mcprd.json.AdapterFactory;
import org.slave.mcprd.models.Assets;
import org.slave.mcprd.models.Assets.Asset;
import org.slave.mcprd.models.Rule;
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
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;

@SuppressWarnings("RedundantStringFormatCall")
public final class MCPRD {

    public final Moshi moshi;

    private VersionManifest versionManifest = null;
    private Version version = null;

    private final MessageDigest messageDigestSHA1;

    public MCPRD() {
        moshi = new Moshi.Builder()
                .add(new AdapterFactory())
                .build();
        try {
            messageDigestSHA1 = MessageDigest.getInstance("SHA-1");
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ConstantValue")
    public void download(final String mcpDir, final String mcVersion, final boolean ignoreMCP, final boolean dlJars, final boolean clientOnly, final boolean serverOnly, final boolean dlLibraries, final boolean dlNatives, final boolean linux, final boolean windows, final boolean w32, final boolean w64, final boolean osx, final boolean dlResources, final boolean forge, final boolean overwrite) throws RuntimeException, IOException {
        if (Constants.DEBUG) downloadFile(new URL(Constants.URL_VERSION_MANIFEST_V2), new File(".", "DEBUG.VERSION_MANIFEST.JSON"), true);

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
                if (Constants.DEBUG) downloadFile(new URL(manifestVersion.url()), new File(".", "DEBUG.MANIFEST_VERSION.JSON"), true);

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

        switch (version.assets()) {
            case PRE_1_6 -> dirNatives = new File(dirJarsBin, "natives");
            case LEGACY -> dirNatives = new File(dirJarsVersionsID, String.format("%s-natives", version.id()));
            case NEWER -> {
                dirNatives = new File(dirJarsVersionsID, String.format("%s-natives", version.id()));
                System.out.println(String.format("Potentially unexpected version \"%s\"!\n", version.id()));
            }
            default -> throw new RuntimeException(String.format("Unexpected assets ID \"%s\"!?", version.assets().assets));
        }

        if (dlJars) {
            System.out.println("Downloading jar files...\n");
            switch(version.assets()) {
                case PRE_1_6 -> {
                    if (!dirJarsBin.exists()) {
                        if (!dirJarsBin.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirJarsBin.getPath()));
                    }
                }
                case LEGACY, NEWER -> {
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
            System.out.println("Done downloading jar files!\n\n");
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
                case LEGACY, NEWER -> {
                    if (!dirLibraries.exists()) {
                        if (!dirLibraries.mkdirs()) throw new RuntimeException(String.format("Failed to create directory \"%s\"!", dirLibraries.getPath()));
                    }
                    dir = dirLibraries;
                }
                default -> throw new RuntimeException(String.format("Unexpected assets ID \"%s\"!", version.assets().assets));
            }
            downloadLibraries(dir, version, linux, windows, osx, overwrite);

            System.out.println("Done downloading library files!\n");
        }
        if (dlNatives) {
            System.out.println("Downloading native files...");
            if (!dirNatives.exists()) {
                if (!dirNatives.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirNatives.getPath()));
            }
            downloadAndExtractNatives(dirNatives, version, linux, windows, w32, w64, osx, forge, overwrite);
            System.out.println("Done downloading native files!\n");
        }
        if (dlResources) {
            System.out.println("Downloading asset files...\n");

            Assets assets;
            try {
                assets = getAssets(version);
            } catch(IOException e) {
                throw new RuntimeException("Failed to get assets index!", e);
            }

            File dir;
            if (assets.map_to_resources()) {//pre-1.6
                dir = dirJarsResources;
            } else if (assets.virtual() || (!assets.virtual() && !assets.map_to_resources())) {//legacy (1.6+) & 1.7+
                File dirAssets = new File(dirJars, "assets");
                File dirAssetsIndexes = new File(dirAssets, "indexes");
                File dirAssetsObjects = new File(dirAssets, "objects");

                if (!dirAssets.exists()) {
                    if (!dirAssets.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirAssets.getPath()));
                }
                if (version.assets() == Version.Assets.LEGACY) {//legacy does not use the index system
                    dir = dirAssets;
                } else {
                    if (!dirAssetsIndexes.exists()) {
                        if (!dirAssetsIndexes.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirAssetsIndexes.getPath()));
                    }
                    if (!dirAssetsObjects.exists()) {
                        if (!dirAssetsObjects.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirAssetsObjects.getPath()));
                    }
                    dir = dirAssetsObjects;

                    serializeJSON(
                            new File(dirAssetsIndexes, String.format("%s.json", version.assetIndex().id())),//use version.assetIndex.id instead of version.assets.assets because it may return null (for "newer" versions)
                            Assets.class,
                            assets,
                            overwrite
                    );//Serialize instead of downloading to avoid additional network calls
                }
            } else {
                throw new RuntimeException("Failed to correctly set asset directory! This is unexpected!");
            }

            if (!dir.exists()) {
                if (!dir.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dir.getPath()));
            }

            downloadResources(dir, version, assets, overwrite);
            System.out.println("Done downloading asset files!\n");
        }

        if (forge) {
            System.out.println("Downloading Forge libs...");
            if (downloadForgeLibs(dirJars, dirMCP, overwrite)) {
                System.out.println("Done downloading Forge libs\n");
            } else {
                System.out.println("Failed to download Forge libs!\n");
            }

            System.out.println("Patching FML library hashes...");
            if (patchFMLHashes(dirMCP)) {
                System.out.println("Done patching FML library hashes!\n");
            } else {
                System.out.println("Failed to patch FML library hashes!\n");
            }
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
            } else if (version.assets() == Version.Assets.LEGACY || version.assets() == Version.Assets.NEWER) {//dir expects to be "jars"
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
            System.out.println(String.format("Downloaded client jar to \"%s\"\n", dest.getPath()));
            status += 1;
        }
        if (server) {
            if (version.downloads().server() != null && version.downloads().server().url() != null) {
                try {
                    url = new URL(version.downloads().server().url());
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
                System.out.println(
                        String.format("Downloaded server jar to \"%s\"\n", dest.getPath())
                );
                status += 1;
            } else {
                System.out.println("No server jar specified in version manifest!");
                System.out.println("Not downloading...\n");
            }
        }
        if (status == 0) System.out.println("Didn't download any jar files?!");
    }

    public void downloadAndExtractNatives(@NotNull File dirNatives, @NotNull final Version version, final boolean linux, final boolean windows, final boolean w32, final boolean w64, final boolean osx, final boolean forge, final boolean overwrite) {
        List<Version.Library> natives = downloadNatives(dirNatives, version, linux, windows, w32, w64, osx, overwrite);
        extractNatives(dirNatives, natives, linux, windows, w32, w64, osx, overwrite);

        if (forge) {
            List<String> jarFiles = new ArrayList<>();
            switch(version.id()) {
                case "1.4.7", "1.5.2" -> {//WTF Forge will error out if these do not exist
                    jarFiles.add("windows_natives.jar");
                    jarFiles.add("linux_natives.jar");
                    jarFiles.add("macosx_natives.jar");
                }
            }
            for(String jarFile : jarFiles) {
                File fileJar = new File(dirNatives, jarFile);
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(fileJar);
                    JarOutputStream jarOutputStream = new JarOutputStream(fileOutputStream);
                    jarOutputStream.putNextEntry(
                            new JarEntry("META-INF/DUMMY_JAR_FILE")
                    );
                    jarOutputStream.flush();
                    jarOutputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();

                    System.out.println(
                            String.format("Created dummy jar file \"%s\"", fileJar.getPath())
                    );
                } catch(IOException e) {
                    System.out.println(
                            String.format("Failed to make dummy jar file \"%s\" due to IOException \"%s\"", jarFile, e)
                    );
                }
            }
        }
    }

    private List<Version.Library> downloadNatives(@NotNull File dirNatives, @NotNull final Version version, final boolean linux, final boolean windows, final boolean w32, final boolean w64, final boolean osx, final boolean overwrite) {
        List<Version.Library> natives = new ArrayList<>();

        for(Version.Library library : version.libraries()) {
            if (library.natives() == null) continue;
            if (!checkLibraryRules(library)) {
                System.out.println(
                        String.format("Disallowed native \"%s\"", library.name())
                );
            } else {
                natives.add(library);
            }
        }

        int lwjgl = 0;
        for(Version.Library _native : natives) {
            if (_native.name().name().equals("lwjgl-platform")) lwjgl++;
        }
        if (lwjgl > 1) {
            Iterator<Version.Library> iterator = natives.iterator();
            while(iterator.hasNext()) {
                Version.Library library = iterator.next();
                if (!osx && library.name().name().equals("lwjgl-platform") && library.isNightly()) {//OSX prefers nightly, but Windows does not (if more than 1 LWJGL)
                    iterator.remove();
                    System.out.println(
                            String.format("Disallowing nightly native \"%s\"", Constants.Maven.to(library.name()))
                    );
                    break;
                }
            }
        }

        for(Version.Library library : natives) {
            if (linux && library.downloads().classifiers().natives_linux() != null) {
                downloadNative(dirNatives, library.downloads().classifiers().natives_linux(), overwrite);
            }
            if (windows && library.downloads().classifiers().natives_windows() != null) {
                downloadNative(dirNatives, library.downloads().classifiers().natives_windows(), overwrite);
            }
            if (windows && w32 && library.downloads().classifiers().natives_windows_32() != null) {
                downloadNative(dirNatives, library.downloads().classifiers().natives_windows_32(), overwrite);
            }
            if (windows && w64 && library.downloads().classifiers().natives_windows_64() != null) {
                downloadNative(dirNatives, library.downloads().classifiers().natives_windows_64(), overwrite);
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
            throw new RuntimeException(
                    String.format("Failed to download native \"%s\"!", fileNative),
                    e
            );
        }
    }

    private void extractNatives(@NotNull final File dirNatives, @NotNull final List<Version.Library> nativeFiles, final boolean linux, final boolean windows, final boolean w32, final boolean w64, final boolean osx, final boolean overwrite) {
        for(Version.Library _native : nativeFiles) {
            if (linux && _native.downloads().classifiers().natives_linux() != null) {
                extractNative(dirNatives, _native, _native.downloads().classifiers().natives_linux(), overwrite);
            }
            if (windows) {
                if (_native.downloads().classifiers().natives_windows() != null) {
                    extractNative(dirNatives, _native, _native.downloads().classifiers().natives_windows(), overwrite);
                } else if (_native.downloads().classifiers().natives_windows_32() != null && w32) {
                    extractNative(dirNatives, _native, _native.downloads().classifiers().natives_windows_32(), overwrite);
                } else if (_native.downloads().classifiers().natives_windows_64() != null && w64) {
                    extractNative(dirNatives, _native, _native.downloads().classifiers().natives_windows_64(), overwrite);
                }
            }
            if (osx) {
                if (_native.downloads().classifiers().natives_osx() != null) {
                    extractNative(dirNatives, _native, _native.downloads().classifiers().natives_osx(), overwrite);
                } else if (_native.downloads().classifiers().natives_macos() != null) {
                    extractNative(dirNatives, _native, _native.downloads().classifiers().natives_macos(), overwrite);
                }
            }
        }
    }

    private void extractNative(@NotNull final File dirNatives, @NotNull final Version.Library library, @NotNull final Version.Library.Downloads.Artifact nativeArtifact, final boolean overwrite) {
        File nativeFile = new File(dirNatives, nativeArtifact.path().substring(nativeArtifact.path().lastIndexOf('/')));
        if (!nativeFile.exists()) throw new RuntimeException(new FileNotFoundException(String.format("Could not find downloaded native \"%s\"", nativeFile.getPath())));

        System.out.println(
                String.format("Extracting native jar \"%s\" to \"%s\"", nativeFile.getName(), dirNatives.getPath())
        );

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
            if (!exclude && jarEntry.getName().startsWith("META-INF")) {//Mojang being lazy and forgetting to exclude META-INF
                System.out.println("Found bad jar exclusion!");
                exclude = true;
            }
            if (exclude) {
                System.out.println(
                        String.format("Not extracting excluded entry \"%s\"...", jarEntry.getName())
                );
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
                System.out.println(
                        String.format("Native \"%s\" already exists. Skipping...", extractFile.getPath())
                );
                continue;
            }

            try(FileOutputStream fileOutputStream = new FileOutputStream(extractFile)) {
                fileOutputStream.getChannel().transferFrom(Channels.newChannel(inputStream), 0, Long.MAX_VALUE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println(
                    String.format("Extracted native \"%s\"", extractFile.getPath())
            );
        }

        try {
            nativeJar.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Done extracting native jar\n");
    }

    public void downloadLibraries(@NotNull File dir, @NotNull final Version version, final boolean linux, final boolean windows, final boolean osx, final boolean overwrite) {
        List<Version.Library> toDownload = new ArrayList<>();
        for(Version.Library library : version.libraries()) {
            if (library.natives() != null) continue;
            if (checkLibraryRules(library)) {
                toDownload.add(library);
            } else {
                System.out.println(
                        String.format("Disallowed library \"%s\" for this OS", library.name())
                );
            }
        }

        for(Version.Library library : toDownload) {
            System.out.println(
                    String.format("Downloading library \"%s\"...", library.name())
            );

            File fileLibrary;
            switch(version.assets()) {
                case PRE_1_6 -> {//dir should be jars/bin
                    fileLibrary = new File(dir, String.format("%s.jar", library.name().name()));
                }
                case LEGACY, NEWER -> {//dir should be jars/libraries
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
                throw new RuntimeException(String.format("Failed to download library \"%s\"!\n", library.name()), e);
            }

            System.out.println(
                    String.format("Done downloading library \"%s\"!\n", library.name())
            );
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

    @SuppressWarnings("ConstantValue")
    public void downloadResources(@NotNull final File dir, @NotNull Version version, @NotNull final Assets assets, final boolean overwrite) {
        for(Entry<String, Asset> entry : assets.objects().entrySet()) {
            File file = null;
            if (assets.map_to_resources() || (version.assets() == Version.Assets.LEGACY && assets.virtual())) {
                file = new File(dir, entry.getKey());//dir is expected to be set to "jars/resources", or "jars/assets" if not proper and virtual
                if (!file.getParentFile().exists()) {
                    if (!file.getParentFile().mkdirs()) throw new RuntimeException(String.format("Failed to create directory \"%s\"!", file.getParentFile().getPath()));
                }
            } else {
                if (assets.virtual() || (!assets.map_to_resources() && !assets.virtual())) {
                    String dirAssetObjectName = entry.getValue().hash().substring(0, 2);
                    File dirAssetObject = new File(dir, dirAssetObjectName);//dir is expected to be set to "jars/assets/objects"
                    if (!dirAssetObject.exists()) {
                        if (!dirAssetObject.mkdirs()) throw new RuntimeException(String.format("Failed to create directory \"%s\"!", dirAssetObject.getPath()));
                    }
                    file = new File(dirAssetObject, entry.getValue().hash());
                }
            }
            if (file == null) throw new NullPointerException(String.format("Failed to get path for asset file \"%s\"!", entry.getKey()));
            downloadResource(file, entry.getValue(), overwrite);//TODO Check hash
        }
    }

    private void downloadResource(@NotNull final File fileAsset, @NotNull final Assets.Asset asset, final boolean overwrite) {
        if (fileAsset.exists()) {
            if (!overwrite) {
                System.out.println(
                        String.format("Asset \"%s\" already exists and can't overwrite", fileAsset.getPath())
                );
                return;
            }
        }

        URL urlFile;
        try {
            urlFile = new URL(String.format(Constants.URL_RESOURCE, asset.hash().substring(0, 2), asset.hash()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        System.out.println(
                String.format("Downloading asset file \"%s\"...", urlFile.getPath())
        );
        try {
            downloadFile(urlFile, fileAsset, overwrite);
        } catch(IOException e) {
            throw new RuntimeException(String.format("Failed to download asset file \"%s\"!", fileAsset.getName()), e);
        }
        System.out.println(
                String.format("Downloaded asset file to \"%s\"!\n", fileAsset.getPath())
        );
    }

    public boolean downloadForgeLibs(final File dirJars, final File dirMCP, final boolean overwrite) {
        File dirMCPLib = new File(dirMCP, "lib");
        if (!dirMCPLib.exists()) {
            if (dirMCPLib.mkdir()) System.out.println(String.format("Could not make directory \"%s\"!", dirMCPLib.getPath()));
        }

        if (dirMCPLib.exists()) {
            List<String[]> libs = new ArrayList<>();// mcp/lib
            List<String[]> jarLibs = new ArrayList<>();// mcp/jars/lib (1.5.2+)
            switch(version.id()) {
                //Download lib files from Maven
                //These are the most up-to-date versions (hotfixed CVEs)
                case "1.4.7" -> {
                    libs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/net/sourceforge/argo/argo/2.25/argo-2.25.jar",
                                    "argo-2.25.jar"
                            }
                    );
                    libs.add(
                            new String[] {
                                    "https://repository.ow2.org/nexus/content/repositories/releases/org/ow2/asm/asm-all/4.0/asm-all-4.0.jar",
                                    "asm-all-4.0.jar"
                            }
                    );
                    libs.add(
                            new String[] {
                                    "https://repository.ow2.org/nexus/content/repositories/releases/org/ow2/asm/asm-all/4.0/asm-all-4.0-sources.jar",
                                    "asm-all-4.0-source.jar"
                            }
                    );
                    libs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/org/ow2/asm/asm-debug-all/4.0/asm-debug-all-4.0.jar",
                                    "asm-debug-all-4.0.jar"
                            }
                    );
                    libs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.47/bcprov-jdk15on-1.47.jar",
                                    "bcprov-jdk15on-147.jar"
                            }
                    );
                    libs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/com/google/guava/guava/12.0.1/guava-12.0.1.jar",
                                    "guava-12.0.1.jar"
                            }
                    );
                    libs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/com/google/guava/guava/12.0.1/guava-12.0.1-sources.jar",
                                    "guava-12.0.1-sources.jar"
                            }
                    );

                    jarLibs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/net/sourceforge/argo/argo/2.25/argo-2.25.jar",
                                    "argo-2.25.jar"
                            }
                    );
                    jarLibs.add(
                            new String[] {
                                    "https://repository.ow2.org/nexus/content/repositories/releases/org/ow2/asm/asm-all/4.0/asm-all-4.0.jar",
                                    "asm-all-4.0.jar"
                            }
                    );
                    jarLibs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.47/bcprov-jdk15on-1.47.jar",
                                    "bcprov-jdk15on-147.jar"
                            }
                    );
                    jarLibs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/com/google/guava/guava/12.0.1/guava-12.0.1.jar",
                                    "guava-12.0.1.jar"
                            }
                    );
                }
                case "1.5.2" -> {
                    libs.add(
                            new String[] {
//                                        "https://repo1.maven.org/maven2/net/sourceforge/argo/argo/3.2/argo-3.2-sources.jar",
                                    "https://repo1.maven.org/maven2/net/sourceforge/argo/argo/3.2/argo-3.2.jar",//WTF
                                    "argo-3.2-src.jar"
                            }
                    );
                    libs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/com/google/guava/guava/14.0-rc3/guava-14.0-rc3.jar",
                                    "guava-14.0-rc3.jar"
                            }
                    );
                    libs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/org/ow2/asm/asm-debug-all/4.1/asm-debug-all-4.1.jar",
                                    "asm-debug-all-4.1.jar"
                            }
                    );
                    libs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.48/bcprov-jdk15on-1.48.jar",
                                    "bcprov-debug-jdk15on-148.jar"
                            }
                    );
                    libs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.48/bcprov-jdk15on-1.48-sources.jar",
                                    "bcprov-jdk15on-148-src.zip"
                            }
                    );
                    libs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/com/google/guava/guava/14.0-rc3/guava-14.0-rc3-sources.jar",
                                    "guava-14.0-rc3-sources.jar"
                            }
                    );
                    libs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.10.0/scala-library-2.10.0.jar",
                                    "scala-library.jar"
                            }
                    );

                    jarLibs.add(
                            new String[] {
                                    "https://master.dl.sourceforge.net/project/argo/argo/3.2/argo-small-3.2.jar?viasf=1",
                                    "argo-small-3.2.jar"
                            }
                    );
                    jarLibs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/com/google/guava/guava/14.0-rc3/guava-14.0-rc3.jar",
                                    "guava-14.0-rc3.jar"
                            }
                    );
                    jarLibs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/org/ow2/asm/asm-all/4.1/asm-all-4.1.jar",
                                    "asm-all-4.1.jar"
                            }
                    );
                    jarLibs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.48/bcprov-jdk15on-1.48.jar",
                                    "bcprov-jdk15on-148.jar"
                            }
                    );
                    jarLibs.add(
                            new String[] {
//                                        https://web.archive.org/web/20140213182129/http://files.minecraftforge.net/fmllibs/
                                    "https://web.archive.org/web/20140626042316if_/http://files.minecraftforge.net/fmllibs/deobfuscation_data_1.5.2.zip",
                                    "deobfuscation_data_1.5.2.zip"
                            }
                    );
                    jarLibs.add(
                            new String[] {
                                    "https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.10.0/scala-library-2.10.0.jar",
                                    "scala-library.jar"
                            }
                    );

                    //WTF
                    try {
                        downloadFile(
                                new URL("https://repo1.maven.org/maven2/org/ow2/asm/asm/4.1/asm-4.1.jar"),
                                new File(dirMCPLib, "asm-4.1.tar.gz"),
                                overwrite
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            int downloaded = 0;
            for(String[] lib : libs) {
                try {
                    downloadFile(
                            new URL(lib[0]),
                            new File(dirMCPLib, lib[1]),
                            overwrite
                    );
                    downloaded++;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            int downloaded2 = 0;
            if (!jarLibs.isEmpty()) {
                File dirJarsLib = new File(dirJars, "lib");
                if(!dirJarsLib.exists()) dirJarsLib.mkdir();

                for(String[] jarLib : jarLibs) {
                    try {
                        downloadFile(
                                new URL(jarLib[0]),
                                new File(dirJarsLib, jarLib[1]),
                                overwrite
                        );
                        downloaded2++;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            return downloaded == libs.size() && downloaded2 == jarLibs.size();
        } else {
            System.out.println(
                    String.format("Directory \"%s\" does not exist?!", dirMCPLib.getPath())
            );
        }
        return false;
    }

    public boolean patchFMLHashes(final File dirMCP) {
        File fileCoreFMLLibraries = new File(dirMCP, "../fml/common/cpw/mods/fml/relauncher/CoreFMLLibraries.java");
        if (fileCoreFMLLibraries.exists()) {
            Map<String, String> mapUpdatedHashes = new HashMap<>();
            switch(version.id()) {
                case "1.4.7" -> {
//                        mapUpdatedHashes.put("bb672829fde76cb163004752b86b0484bd0a7f4b", "bb672829fde76cb163004752b86b0484bd0a7f4b");//argo-2.25.jar
//                        mapUpdatedHashes.put("b8e78b9af7bf45900e14c6f958486b6ca682195f", "b8e78b9af7bf45900e14c6f958486b6ca682195f");//guava-12.0.1.jar
                    mapUpdatedHashes.put("98308890597acb64047f7e896638e0d98753ae82", "2518725354c7a6a491a323249b9e86846b00df09");//asm-all-4.0.jar
//                        mapUpdatedHashes.put("b6f5d9926b0afbde9f4dbe3db88c5247be7794bb", "b6f5d9926b0afbde9f4dbe3db88c5247be7794bb");//bcprov-jdk15on-147.jar
                }
                case "1.5.2" -> {
//                        mapUpdatedHashes.put("58912ea2858d168c50781f956fa5b59f0f7c6b51", "58912ea2858d168c50781f956fa5b59f0f7c6b51");//argo-small-3.2.jar
//                        mapUpdatedHashes.put("931ae21fa8014c3ce686aaa621eae565fefb1a6a", "931ae21fa8014c3ce686aaa621eae565fefb1a6a");//guava-14.0-rc3.jar
                    mapUpdatedHashes.put("054986e962b88d8660ae4566475658469595ef58", "ad568238ee36a820bd6c6806807e8a14ea34684d");//asm-all-4.1.jar
//                        mapUpdatedHashes.put("960dea7c9181ba0b17e8bab0c06a43f0a5f04e65", "960dea7c9181ba0b17e8bab0c06a43f0a5f04e65");//bcprov-jdk15on-148.jar
                    mapUpdatedHashes.put("458d046151ad179c85429ed7420ffb1eaf6ddf85", "43c6d98b445187c6b459a582c774ffb025120ef4");//scala-library.jar (2.10)
                }
            }

            if (!mapUpdatedHashes.isEmpty()) {
                //Replacing is way simpler than using Spoon
                String stringClass;
                try {
                    stringClass = Files.readString(fileCoreFMLLibraries.toPath());
                } catch(IOException e) {
                    System.out.println(
                            String.format("Failed to read file \"%s\" due to IOException!", fileCoreFMLLibraries.getPath())
                    );
                    throw new RuntimeException(e);
                }

                String ret = stringClass;
                for(String key : mapUpdatedHashes.keySet()) ret = ret.replaceFirst(key, mapUpdatedHashes.get(key));
                if (!ret.equals(stringClass)) {
                    try {
                        Files.writeString(fileCoreFMLLibraries.toPath(), ret);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(
                            String.format("Patched file \"%s\"", fileCoreFMLLibraries.getPath())
                    );
                    return true;
                } else {
                    System.out.println(
                            String.format("Failed to patch file \"%s\"!", fileCoreFMLLibraries.getPath())
                    );
                }
            }
        } else {
            System.out.println(
                    String.format("File \"%s\" does not exist!", fileCoreFMLLibraries)
            );
        }
        return false;
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
            for(Rule rule : library.rules()) {
                if (rule.os() == null || rule.os().name() == null) {//Check for name being null may be incorrect...
                    allow = rule.action().value;
                } else {//TODO nightly version is used for OSX exclusively
                    boolean matchesOS = rule.os().name().startsWith(Constants.OS_NAME.toLowerCase());
                    boolean matchesOSVersion = false;
                    if (rule.os().version() != null) matchesOSVersion = Pattern.compile(rule.os().version()).matcher(Constants.OS_VERSION).matches();
                    if (matchesOS && matchesOSVersion) {
                        allow = rule.action().value;
                    }
                }
            }
        } else {
            System.out.println(
                    String.format(
                            "Found no rules for library \"%s\"! This should not happen!",
                            String.format("%s / %s", library.name().name(), library.name().version())
                    )
            );
        }
        return allow && (!library.name().version().contains("nightly"));//Never use nightly releases - MCP never used them
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
                System.out.println(String.format("File \"%s\" exists, but overwriting...\n", file.getPath()));
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
            fos.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);//Will ONLY transfer UP TO 16 MiB... but that shouldn't be an issue...
        }
    }

}
