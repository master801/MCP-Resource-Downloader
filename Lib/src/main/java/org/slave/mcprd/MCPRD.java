package org.slave.mcprd;

import com.squareup.moshi.Moshi;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.slave.mcprd.Constants.Maven;
import org.slave.mcprd.exceptions.JavaNotFoundException;
import org.slave.mcprd.json.AdapterFactory;
import org.slave.mcprd.models.Assets;
import org.slave.mcprd.models.Assets.Asset;
import org.slave.mcprd.models.Rule;
import org.slave.mcprd.models.Version;
import org.slave.mcprd.models.Version.Library;
import org.slave.mcprd.models.Version.Library.Downloads;
import org.slave.mcprd.models.Version.Library.Downloads.Artifact;
import org.slave.mcprd.models.VersionManifest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.module.ModuleDescriptor;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;

@SuppressWarnings("RedundantStringFormatCall")
public final class MCPRD {

    private static final Map<String, String[]> MAP_PATCH_SERVER_HASH = new HashMap<>();
    private static final Map<String, String[][][]> FORGE_LIBS = new HashMap<>();

    public final Moshi moshi;

    private VersionManifest versionManifest = null;
    private Version version = null;

    @Getter
    private String regkeyJavaVersion = null, regkeyJavaHome = null;

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

        //<editor-fold desc="MCP Patched Server Hashes">
        //<editor-fold desc="1.6.4">
		MCPRD.MAP_PATCH_SERVER_HASH.put(
                "1.6.4",
                new String[] {
                        "ba3145656b1480122bd8759cecd7b7a1",
                        "abcf286a14f7aee82e8bf89270433509"
                }
        );
        //</editor-fold>
        //</editor-fold>

        //<editor-fold desc="Forge Libs">
        //<editor-fold desc="1.3.2">
		MCPRD.FORGE_LIBS.put(
                "1.3.2",
                new String[][][] {
                        new String[][] {//lib
                                new String[] {
                                        "argo-2.25.jar",
                                        "https://repo1.maven.org/maven2/net/sourceforge/argo/argo/2.25/argo-2.25.jar"
                                },
                                new String[] {
                                        "asm-all-4.0.jar",
                                        "https://repo1.maven.org/maven2/org/ow2/asm/asm-all/4.0/asm-all-4.0.jar"
                                },
                                new String[] {
                                        "asm-all-4.0-source.jar",
                                        "https://repo1.maven.org/maven2/org/ow2/asm/asm-all/4.0/asm-all-4.0-sources.jar"
                                },
                                new String[] {
                                        "guava-12.0.1.jar",
                                        "https://repo1.maven.org/maven2/com/google/guava/guava/12.0.1/guava-12.0.1.jar"
                                },
                                new String[] {
                                        "guava-12.0.1-sources.jar",
                                        "https://repo1.maven.org/maven2/com/google/guava/guava/12.0.1/guava-12.0.1-sources.jar"
                                }
                        },
                        new String[][] {//jar lib
                                new String[] {
                                        "asm-all-4.0.jar",
                                        "https://repo1.maven.org/maven2/org/ow2/asm/asm-all/4.0/asm-all-4.0.jar"
                                },
                                new String[] {
                                        "argo-2.25.jar",
                                        "https://repo1.maven.org/maven2/net/sourceforge/argo/argo/2.25/argo-2.25.jar"
                                },
                                new String[] {
                                        "guava-12.0.1.jar",
                                        "https://repo1.maven.org/maven2/com/google/guava/guava/12.0.1/guava-12.0.1.jar"
                                }
                        }
                }
        );
        //</editor-fold>

        //<editor-fold desc="1.4.7">
        MCPRD.FORGE_LIBS.put(
                "1.4.7",
                new String[][][] {
                        new String[][] {//lib
                                new String[] {
                                        "argo-2.25.jar",
                                        "https://repo1.maven.org/maven2/net/sourceforge/argo/argo/2.25/argo-2.25.jar"
                                },
                                new String[] {
                                        "asm-all-4.0.jar",
                                        "https://repo1.maven.org/maven2/org/ow2/asm/asm-all/4.0/asm-all-4.0.jar"
                                },
                                new String[] {
                                        "asm-all-4.0-source.jar",
                                        "https://repo1.maven.org/maven2/org/ow2/asm/asm-all/4.0/asm-all-4.0-sources.jar"
                                },
                                new String[] {
                                        "asm-debug-all-4.0.jar",
                                        "https://repo1.maven.org/maven2/org/ow2/asm/asm-debug-all/4.0/asm-debug-all-4.0.jar"
                                },
                                new String[] {
                                        "bcprov-jdk15on-147.jar",
                                        "https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.47/bcprov-jdk15on-1.47.jar"
                                },
                                new String[] {
                                        "guava-12.0.1.jar",
                                        "https://repo1.maven.org/maven2/com/google/guava/guava/12.0.1/guava-12.0.1.jar"
                                },
                                new String[] {
                                        "guava-12.0.1-sources.jar",
                                        "https://repo1.maven.org/maven2/com/google/guava/guava/12.0.1/guava-12.0.1-sources.jar"
                                }
                        },
                        new String[][] {//jar lib
                                new String[] {
                                        "argo-2.25.jar",
                                        "https://repo1.maven.org/maven2/net/sourceforge/argo/argo/2.25/argo-2.25.jar"
                                },
                                new String[] {
                                        "asm-all-4.0.jar",
                                        "https://repo1.maven.org/maven2/org/ow2/asm/asm-all/4.0/asm-all-4.0.jar"
                                },
                                new String[] {
                                        "bcprov-jdk15on-147.jar",
                                        "https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.47/bcprov-jdk15on-1.47.jar"
                                },
                                new String[] {
                                        "guava-12.0.1.jar",
                                        "https://repo1.maven.org/maven2/com/google/guava/guava/12.0.1/guava-12.0.1.jar"
                                }
                        }
                }
        );
        //</editor-fold>

        //<editor-fold desc="1.5.2">
		MCPRD.FORGE_LIBS.put(
                "1.5.2",
                new String[][][] {
                        new String[][] {//lib
                                new String[] {
                                        "argo-3.2-src.jar",
//                                "https://repo1.maven.org/maven2/net/sourceforge/argo/argo/3.2/argo-3.2-sources.jar"
                                        "https://repo1.maven.org/maven2/net/sourceforge/argo/argo/3.2/argo-3.2.jar"//WTF
                                },
                                new String[] {
                                        "guava-14.0-rc3.jar",
                                        "https://repo1.maven.org/maven2/com/google/guava/guava/14.0-rc3/guava-14.0-rc3.jar"
                                },
                                new String[] {
                                        "asm-debug-all-4.1.jar",
                                        "https://repo1.maven.org/maven2/org/ow2/asm/asm-debug-all/4.1/asm-debug-all-4.1.jar"
                                },
                                new String[] {
                                        "bcprov-debug-jdk15on-148.jar",
                                        "https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.48/bcprov-jdk15on-1.48.jar"
                                },
                                new String[] {
                                        "bcprov-jdk15on-148-src.zip",
                                        "https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.48/bcprov-jdk15on-1.48-sources.jar"
                                },
                                new String[] {
                                        "guava-14.0-rc3-sources.jar",
                                        "https://repo1.maven.org/maven2/com/google/guava/guava/14.0-rc3/guava-14.0-rc3-sources.jar"
                                },
                                new String[] {
                                        "scala-library.jar",
                                        "https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.10.0/scala-library-2.10.0.jar"
                                },

								//Do not download. This is never used so it doesn't matter.
//								new String[] {
//										"asm-4.1.tar.gz",
//										""
//								}
                        },
                        new String[][] {//jar lib
                                new String[] {
                                        "argo-small-3.2.jar",
                                        "https://master.dl.sourceforge.net/project/argo/argo/3.2/argo-small-3.2.jar?viasf=1"
                                },
                                new String[] {
                                        "guava-14.0-rc3.jar",
                                        "https://repo1.maven.org/maven2/com/google/guava/guava/14.0-rc3/guava-14.0-rc3.jar"
                                },
                                new String[] {
                                        "asm-all-4.1.jar",
                                        "https://repo1.maven.org/maven2/org/ow2/asm/asm-all/4.1/asm-all-4.1.jar"
                                },
                                new String[] {
                                        "bcprov-jdk15on-148.jar",
                                        "https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk15on/1.48/bcprov-jdk15on-1.48.jar"
                                },
                                new String[] {
                                        "scala-library.jar",
                                        "https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.10.0/scala-library-2.10.0.jar"
                                },

                                new String[] {
                                        "asm-4.1.tar.gz",
                                        "https://repo1.maven.org/maven2/org/ow2/asm/asm/4.1/asm-4.1.jar"
                                }
                        }
                }
        );
        //</editor-fold>
        //</editor-fold>
    }

    public void getWindowsRegistryValues() {
        try {
            if (regkeyJavaVersion == null) {
                String regJavaCurrentVersion = getWindowsRegistryValue("HKLM\\Software\\JavaSoft\\Java Development Kit", "CurrentVersion");
                if (regJavaCurrentVersion != null) {
                    regkeyJavaVersion = regJavaCurrentVersion;
                    if (regkeyJavaHome == null) {
                        String regJavaHome = getWindowsRegistryValue(String.format("HKLM\\Software\\JavaSoft\\Java Development Kit\\%s", regkeyJavaVersion), "JavaHome");
                        if (regJavaHome != null) {
                            regkeyJavaHome = regJavaHome;
                            File fileJavaExe = new File(String.join(File.separator, regJavaHome, "bin", "java.exe"));
                            if (!fileJavaExe.exists()) {
                                throw new RuntimeException(
                                        new FileNotFoundException(String.format("Invalid JDK path \"%s\"!", fileJavaExe.getAbsolutePath()))
                                );
                            }
                        }
                    }
                } else {
                    throw new JavaNotFoundException("JDK is not installed or set!");
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getWindowsRegistryValue(final String keyFolder, final String keyName) throws InterruptedException {
		String build = String.format(
				"REG QUERY \"%s\"%s",
				keyFolder,
				keyName == null || keyName.isEmpty() ? "" : String.format(" /v \"%s\"", keyName)
		);

        Process process;
        try {
            process = new ProcessBuilder(build)
					.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        process.waitFor(10, TimeUnit.SECONDS);

		//FIXME
//        int exitCode = process.exitValue();
//        if (exitCode != 0) {
//            System.out.println(
//                    String.format("Failed to get registry value \"%s\" \"%s\" due to an unknown exit error \"%d\"!", keyFolder, keyName, exitCode)
//            );
//            return null;
//        }

        byte[] bufferOutput;
        try {
            bufferOutput = process.getInputStream().readAllBytes();
        } catch(IOException e) {
            System.out.println(
                    String.format("Failed to read input stream! %s", e)
            );
            return null;
        }
        //noinspection ConstantValue
        if (bufferOutput == null || bufferOutput.length < 1) {
            System.out.println(
                    String.format("Failed to get registry key \"%s\" \"%s\"!", keyFolder, keyName)
            );
            return null;
        }
        String output = new String(bufferOutput, StandardCharsets.US_ASCII).trim();
        if (output.equals("ERROR: The system was unable to find the specified registry key or value.")) {//FIXME I don't think this really works...
            System.out.println(
                    String.format("Failed to get registry key \"%s\" \"%s\"!", keyFolder, keyName)
            );
            return null;
        }
        String[] split = output.split("\r\n");//We only care about the last line
        return split[1].trim().split(" {4}")[2];
    }

	public void init() throws RuntimeException, IOException {
		if (Constants.DEBUG) {
			URI uri;
			try {
				uri = new URI(Constants.URL_VERSION_MANIFEST_V2);
				downloadFile(uri, new File(".", "DEBUG.VERSION_MANIFEST.JSON"), null, -1, true);
			} catch(URISyntaxException e) {
				System.out.println(
						String.format("Caught exception while downloading version manifest! \"%s\"", e)
				);
			}
		}

		if (this.versionManifest == null) {
			try {
				System.out.println("Getting \"version_manifest_v2.json\"...");
				getVersionManifest();
			} catch(IOException e) {
				throw new RuntimeException("Failed to get \"version_manifest_v2.json\" due to an IO Exception!", e);
			}
			System.out.println(
					String.format("Done getting version manifest.%s", System.lineSeparator())
			);
		}
	}

	public boolean setVersion(final String mcVersion) throws RuntimeException, IOException {
		if (mcVersion == null || mcVersion.isEmpty()) return false;

		VersionManifest.Version manifestVersion;
		if (this.versionManifest != null) {
			manifestVersion = Arrays.stream(versionManifest.versions())
					.filter(i -> i.id().equals(mcVersion))
					.findFirst()
					.orElse(null);
		} else {
			throw new RuntimeException("VersionManifest not set!");
		}
		if (manifestVersion != null) {
			if (version == null || !mcVersion.equals(version.id())) {
				if (Constants.DEBUG) {
					try {
						downloadFile(
								new URI(manifestVersion.url()),
								new File(".", "DEBUG.VERSION.JSON"),
								null,
								-1,
								true
						);
					} catch (URISyntaxException e) {
						throw new RuntimeException(e);
					}
				}

				System.out.println("Getting version JSON...");
				try {
					version = getVersion(manifestVersion);
				} catch(IOException e) {
					throw new RuntimeException("Failed to get version!", e);
				}
				System.out.println(
						String.format("Done getting version JSON.%s", System.lineSeparator())
				);

				if (Constants.DEBUG) {
					try {
						Files.writeString(
								new File(".", "DEBUG.VERSION.RECONSTRUCTED.JSON").toPath(),
								moshi.adapter(Version.class)
										.indent("  ")
										.toJson(version),
								StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE
						);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				return true;
			}
		} else {
			throw new NullPointerException("Failed to get version?!");
		}
		return false;
	}

    @SuppressWarnings("ConstantValue")
    public void download(final String mcpDir, final String mcVersion, final boolean ignoreMCP, final boolean dlJars, final boolean clientOnly, final boolean serverOnly, final boolean dlLibraries, final boolean dlNatives, final boolean linux, final boolean windows, final boolean w32, final boolean w64, final boolean osx, final boolean dlResources, final boolean forge, final boolean useLocalAssets, final boolean overwrite) throws RuntimeException, IOException {
		if (versionManifest == null) throw new NullPointerException("Version manifest not initialized!");

        if (mcpDir == null && mcVersion == null) {//Just print out all versions
            System.out.println(
                    String.format("No version nor MCP directory specified.%sPrinting out all Minecraft versions...%s", System.lineSeparator(), System.lineSeparator())
            );
            for(VersionManifest.Version version : versionManifest.versions()) {
                String year = version.releaseTime().substring(0, 4), month = version.releaseTime().substring(5, 7), day = version.releaseTime().substring(8, 10);
                System.out.println(
                        String.format("%s - %s/%s/%s", version.id(), month, day, year)
                );
            }
            System.out.println(
                    String.format("%sDone", System.lineSeparator())
            );
            return;
        }

        if (mcpDir == null) throw new FileNotFoundException("No MCP directory was set!");
        if (mcVersion == null) throw new NullPointerException("No Minecraft version was set!");

        if (version == null) throw new NullPointerException("Failed to get version?!");

        File dirMCP = new File(mcpDir);
        if (!dirMCP.exists()) throw new FileNotFoundException(String.format("MCP directory \"%s\" does not exist!", mcpDir));
        if (!dirMCP.isDirectory()) throw new FileNotFoundException(String.format("Selected path \"%s\" is not a directory!", mcpDir));
        if (!ignoreMCP && !new File(dirMCP, "docs/README-MCP.TXT").exists()) throw new FileNotFoundException(String.format("Selected path \"%s\" is not not a valid MCP directory!", mcpDir));

        File dirMCPConf = new File(dirMCP, "conf");
        File dirMCPJars = new File(dirMCP, "jars");
        File dirMCPJarsBin = new File(dirMCPJars, "bin");//pre-1.6
        File dirMCPJarsLib = new File(dirMCPJars, "lib");//pre-1.6
        File dirMCPJarsLibraries = new File(dirMCPJars, "libraries");//legacy (1.6)
        File dirMCPJarsResources = new File(dirMCPJars, "resources");//pre-1.6
        File dirMCPJarsBinNatives;
        File dirMCPJarsVersions = new File(dirMCPJars, "versions");//legacy
        File dirMCPJarsVersionsID = new File(dirMCPJarsVersions, version.id());//legacy

        File dirFml = null;
        if (forge) dirFml = new File(dirMCP.getParentFile(), "fml");

        File fileMCPConfCfg = new File(dirMCPConf, "mcp.cfg");

        if (!dirMCPJars.exists()) {
            if (!dirMCPJars.mkdir()) throw new IOException(String.format("Could not create directory \"%s\"!", dirMCPJars.getPath()));
        }

        switch (version.assets()) {
			case Constants.ASSETS_LEGACY -> dirMCPJarsBinNatives = new File(dirMCPJarsVersionsID, String.format("%s-natives", version.id()));
			case Constants.ASSETS_PRE_1_6 -> {
                if (!dirMCPJarsLib.exists() && !dirMCPJarsLib.mkdirs()) {
                    System.out.println(
                            String.format("Failed to make directory \"%s\"!", dirMCPJarsLib.getAbsolutePath())
                    );
                }
                dirMCPJarsBinNatives = new File(dirMCPJarsBin, "natives");
            }
			default -> {
				dirMCPJarsBinNatives = new File(dirMCPJarsVersionsID, String.format("%s-natives", version.id()));
				System.out.println(
						String.format("Potentially unexpected version \"%s\"!%s", version.id(), System.lineSeparator())
				);
			}
        }

		//<editor-fold desc="Jars">
        if (dlJars) {
            System.out.println("Downloading jar files...\n");
            switch(version.assets()) {
				case Constants.ASSETS_PRE_1_6 -> {
                    if (!dirMCPJarsBin.exists()) {
                        if (!dirMCPJarsBin.mkdir()) throw new IOException(String.format("Could not create directory \"%s\"!", dirMCPJarsBin.getPath()));
                    }
                }
                default -> {
                    if (!dirMCPJarsVersions.exists()) {
                        if (!dirMCPJarsVersions.mkdirs()) throw new RuntimeException(String.format("Failed to create directory \"%s\"!", dirMCPJarsVersions.getPath()));
                    }
                    if (!dirMCPJarsVersionsID.exists()) {
                        if (!dirMCPJarsVersionsID.mkdirs()) throw new RuntimeException(String.format("Failed to create directory \"%s\"!", dirMCPJarsVersionsID.getPath()));
                    }
                }
            }
            downloadMinecraftJars(dirMCPJars, dirMCPJarsBin, dirMCPJarsVersionsID, fileMCPConfCfg, version, forge, clientOnly, serverOnly, overwrite);
            System.out.println(
                    String.format("Done downloading jar files!%s%s", System.lineSeparator(), System.lineSeparator())
            );
        }
		//</editor-fold>

		//<editor-fold desc="Libraries">
        if (dlLibraries) {
            System.out.println("Downloading library files...");

            File dir;
            switch(version.assets()) {
				case Constants.ASSETS_PRE_1_6 -> {
                    if (!dirMCPJarsBin.exists()) {
                        if (!dirMCPJarsBin.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirMCPJarsBin.getPath()));
                    }
                    dir = dirMCPJarsBin;
                }
                default -> {
                    if (!dirMCPJarsLibraries.exists()) {
                        if (!dirMCPJarsLibraries.mkdirs()) throw new RuntimeException(String.format("Failed to create directory \"%s\"!", dirMCPJarsLibraries.getPath()));
                    }
                    dir = dirMCPJarsLibraries;
                }
            }
            downloadLibraries(dir, version, forge, linux, windows, osx, overwrite);
            if (forge) {
                //Download libraries for Forge
                System.out.println("Downloading Forge libs...");
                if (downloadForgeLibs(version, dirMCPJars, dirMCPJarsLib, dirMCP, overwrite)) {
                    System.out.println(
                            String.format("Done downloading Forge libs%s", System.lineSeparator())
                    );
                } else {
                    System.out.println(
                            String.format("Failed to download Forge libs!%s", System.lineSeparator())
                    );
                }

                if (version.assets().equals(Constants.ASSETS_PRE_1_6)) {
                    System.out.println("Patching FML library hashes...");
                    if (patchFMLHashes(version, dirMCP)) {
                        System.out.println(
                                String.format("Done patching FML library hashes!%s", System.lineSeparator())
                        );
                    } else {
                        System.out.println(
                                String.format("Failed to patch FML library hashes!%s", System.lineSeparator())
                        );
                    }
                }
            }
            System.out.println(
                    String.format("Done downloading library files!%s", System.lineSeparator())
            );
        }
		//</editor-fold>

		//<editor-fold desc="Natives">
        if (dlNatives) {
            System.out.println("Downloading native files...");
            if (!dirMCPJarsBinNatives.exists()) {
                if (!dirMCPJarsBinNatives.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirMCPJarsBinNatives.getPath()));
            }
            downloadAndExtractNatives(dirMCPJarsBinNatives, version, linux, windows, w32, w64, osx, forge, overwrite);
            System.out.println(
                    String.format("Done downloading native files!%s", System.lineSeparator())
            );
        }
		//</editor-fold>

		//<editor-fold desc="Resources">
        if (dlResources) {
            System.out.println(
                    String.format("Downloading asset files...%s", System.lineSeparator())
            );

            Assets assets;
            try {
                assets = getAssets(version);
            } catch(IOException e) {
                throw new RuntimeException("Failed to get assets index!", e);
            }

            File dir;
            if (assets.map_to_resources()) {//pre-1.6
                dir = dirMCPJarsResources;
            } else if (assets.virtual() || (!assets.virtual() && !assets.map_to_resources())) {//legacy (1.6+) & 1.7+
                File dirAssets = new File(dirMCPJars, "assets");
                File dirAssetsIndexes = new File(dirAssets, "indexes");
                File dirAssetsObjects = new File(dirAssets, "objects");

                if (!dirAssets.exists()) {
                    if (!dirAssets.mkdirs()) throw new IOException(String.format("Could not create directory \"%s\"!", dirAssets.getPath()));
                }
                if (version.assets() == Constants.ASSETS_LEGACY) {//legacy does not use the index system
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

            downloadResources(dir, version, assets, useLocalAssets, windows, linux, osx, overwrite);
            System.out.println(
                    String.format("Done downloading asset files!%s", System.lineSeparator())
            );
        }
		//</editor-fold>

        if (forge) patchFMLScript(version, dirFml);
    }

    /**
     * @param dirJars           jars (for server)
     * @param dirJarsBin        jars/bin (for pre-1.6 client)
     * @param dirJarsVersionsID client - jars/bin (for pre-1.6), versions/{id} (for legacy - 1.6)
     * @param version           {@link org.slave.mcprd.models.Version}
     * @param forge             If using Forge
     * @param client            Should download client jar
     * @param server            Should download server jar
     * @param overwrite         Should overwrite any existing file
     */
    public void downloadMinecraftJars(@NotNull final File dirJars, final File dirJarsBin, @NotNull final File dirJarsVersionsID, @NotNull final File fileMCPConfCfg, @NotNull final Version version, final boolean forge, final boolean client, final boolean server, final boolean overwrite) {
        URI uri;
        File dest;

        int status = 0;
        if (client) {
            try {
                uri = new URI(version.downloads().client().url());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            if (version.assets().equals(Constants.ASSETS_PRE_1_6)) {//dir expects to be "jars/bin"
                dest = new File(dirJarsBin, "minecraft.jar");
//            } else if (version.assets() == Version.Assets.LEGACY || version.assets() == Version.Assets.NEWER) {//dir expects to be "jars"
            } else {//dir expects to be "jars"
                dest = new File(dirJarsVersionsID, String.format("%s.jar", version.id()));
                serializeJSON(
                        new File(dirJarsVersionsID, String.format("%s.json", version.id())),
                        Version.class,
                        version,
                        overwrite
                );//Serialize instead of downloading to avoid additional network calls
            }

            System.out.println("Downloading client jar...");
            try {
                downloadFile(uri, dest, version.downloads().client().sha1(), version.downloads().client().size(), overwrite);
            } catch(IOException e) {
                throw new RuntimeException("Failed to download client jar!", e);
            }
            System.out.println(
                    String.format("Downloaded client jar to \"%s\"%s", dest.getPath(), System.lineSeparator())
            );
            status += 1;
        }
        if (server) {
            if (version.downloads().server() != null && version.downloads().server().url() != null) {
                try {
                    uri = new URI(version.downloads().server().url());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                String fn = "minecraft_server.jar";
				if (forge) {
					if (version.assets().equals(Constants.ASSETS_LEGACY)) {
						fn = String.format("minecraft_server.%s.jar", version.id());
					}
				}
//                if (forge && (!version.assets().equals(Version.Assets.PRE_1_6) && !version.assets().equals(Version.Assets.NEWER))) {
//                    fn = String.format("minecraft_server.%s.jar", version.id());
//                } else {
//                    fn = "minecraft_server.jar";
//                }
                dest = new File(dirJars, fn);
                System.out.println("Downloading server jar...");
                try {
                    downloadFile(uri, dest, version.downloads().server().sha1(), version.downloads().server().size(), overwrite);
                } catch(IOException e) {
                    throw new RuntimeException("Failed to download server jar!", e);
                }
                System.out.println(
                        String.format("Downloaded server jar to \"%s\"%s", dest.getPath(), System.lineSeparator())
                );

                if (MCPRD.MAP_PATCH_SERVER_HASH.containsKey(version.id())) {
                    System.out.println(
                            String.format("Detected %s%sThis version of MCP has an invalid file hash for this version's server jar. Patching...", version.id(), System.lineSeparator())
                    );
                    File fpCfg = null;
                    String stringCfg = null;
                    try {
                        if (forge) {
                            fpCfg = new File(
                                    new File(
                                            fileMCPConfCfg.getParentFile().getParentFile().getParentFile(),
                                            "fml"
                                    ),
                                    "mc_versions.cfg"
                            );
                        } else {
                            fpCfg = fileMCPConfCfg;
                        }
                        stringCfg = Files.readString(fpCfg.toPath(), StandardCharsets.US_ASCII);
                    } catch (IOException e) {
                        System.out.println(
                                String.format("Failed to patch file \"%s\" due to IO Exception!%s%s", fpCfg.toString(), System.lineSeparator(), e.toString())
                        );
                    }
                    if (stringCfg != null) {
                        String[] hashes = MCPRD.MAP_PATCH_SERVER_HASH.get(version.id());
                        String newCfg = stringCfg.replaceFirst(hashes[0], hashes[1]);//lazy, but it works
                        if (!newCfg.equals(stringCfg)) {
                            try {
                                Files.writeString(fpCfg.toPath(), newCfg, StandardCharsets.US_ASCII);
                                System.out.println(
                                        String.format("Patched config file \"%s\" successfully!%s", fpCfg.getPath(), System.lineSeparator())
                                );
                            } catch (IOException e) {
                                System.out.println(
                                        String.format("Failed to write config file \"%s\" due to IO Exception!%s%s%s", fpCfg.getAbsolutePath(), System.lineSeparator(), e, System.lineSeparator())
                                );
                            }
                        } else if (stringCfg.contains(hashes[1])) {
                            System.out.println(
                                    String.format("Config file \"%s\" has already been patched%s", fpCfg.getPath(), System.lineSeparator())
                            );
                        } else {
                            System.out.println(
                                    String.format("Failed to patch config file \"%s\"!%s", fpCfg.getPath(), System.lineSeparator())
                            );
                        }
                    }
                }

                status += 1;
            } else {
                System.out.println("No server jar specified in version manifest!");
                System.out.println(
                        String.format("Not downloading...%s", System.lineSeparator())
                );
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
				try(FileOutputStream fos = new FileOutputStream(fileJar)) {
					try(JarOutputStream jos = new JarOutputStream(fos)) {
						jos.putNextEntry(
								new JarEntry("META-INF/DUMMY_JAR_FILE")
						);
						System.out.println(
								String.format("Created dummy jar file \"%s\"", fileJar.getPath())
						);
					}
				} catch(IOException e) {
					System.out.println(
							String.format("Failed to make dummy jar file \"%s\" due to IOException \"%s\"", jarFile, e)
					);
				}
            }
        }
    }

    private List<Version.Library> downloadNatives(@NotNull File dirNatives, @NotNull final Version version, final boolean linux, final boolean windows, final boolean w32, final boolean w64, final boolean osx, final boolean overwrite) {
        List<Version.Library> natives = Arrays.stream(version.getNatives())
				.filter(this::checkLibraryRules).toList();

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
        URI uri;
        try {
            uri = new URI(artifact.url());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        File fileNative = new File(dirNatives, artifact.path().substring(artifact.path().lastIndexOf('/')));
        try {
            downloadFile(uri, fileNative, artifact.sha1(), artifact.size(), overwrite);
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

        try {
            try (JarFile nativeJar = new JarFile(nativeFile)) {
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

                    try(InputStream inputStream = nativeJar.getInputStream(jarEntry)) {
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
                            fileOutputStream.getChannel().transferFrom(
                                    Channels.newChannel(inputStream),
                                    0,
                                    Long.MAX_VALUE
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println(
                                String.format("Extracted native \"%s\"", extractFile.getPath())
                        );
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(
                String.format("Done extracting native jar%s", System.lineSeparator())
        );
    }

    public void downloadLibraries(@NotNull File dir, @NotNull final Version version, final boolean forge, final boolean linux, final boolean windows, final boolean osx, final boolean overwrite) {
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

        if (forge) {
            if (version.id().equals("1.6.4")) {
                //Lib that Forge requires
                toDownload.add(
                        new Library(
                                new Downloads(
                                        new Artifact(
                                                "org/ow2/asm/asm-debug-all/4.1/asm-debug-all-4.1.jar",
                                                "dd6ba5c392d4102458494e29f54f70ac534ec2a2",
                                                342664,
                                                "https://repo1.maven.org/maven2/org/ow2/asm/asm-debug-all/4.1/asm-debug-all-4.1.jar"
                                        ),
                                        null
                                ),
                                null,
                                Maven.from("org.ow2.asm:asm-debug-all:4.1"),
                                null,
                                null
                        )
                );
                toDownload.add(
                        new Library(
                                new Downloads(
                                        new Artifact(
                                                "org/ow2/asm/asm-debug-all/4.1/asm-debug-all-4.1-sources.jar",
                                                "1c218d5a0b8b932a1528b74069aa6973dcfcea74",
                                                983980,
                                                "https://repo1.maven.org/maven2/org/ow2/asm/asm-debug-all/4.1/asm-debug-all-4.1-sources.jar"
                                        ),
                                        null
                                ),
                                null,
                                Maven.from("org.ow2.asm:asm-debug-all-sources:4.1"),
                                null,
                                null
                        )
                );

                //Jar built from
                //https://github.com/Mojang/LegacyLauncher/tree/840529603677cf361a3df9df97b3574772eda35a
                toDownload.add(
                        new Library(
                                new Downloads(
                                        new Artifact(
                                                "net/minecraft/launchwrapper/1.8/launchwrapper-1.8.jar",
                                                "a1b1cc433d256d3de3472965ac44b94c175db5e1",
                                                28945,
                                                "file://launchwrapper-1.8.jar"//Use interally bundled jar file
                                        ),
                                        null
                                ),
                                null,
                                Maven.from("net.minecraft:launchwrapper:1.8"),
                                null,
                                null
                        )
                );

                //Add lzma - this seems to be a custom jar built by *someone*, so we have to grab it from Sponge's Maven repo
                toDownload.add(
                        new Library(
                                new Downloads(
                                        new Artifact(
                                                "lzma/lzma/0.0.1/lzma-0.0.1.jar",
                                                "521616dc7487b42bef0e803bd2fa3faf668101d7",
                                                5762,
                                                "https://repo.spongepowered.org/repository/sponge-legacy/lzma/lzma/0.0.1/lzma-0.0.1.jar"
                                        ),
                                        null
                                ),
                                null,
                                Maven.from("lzma:lzma:0.0.1"),
                                null,
                                null
                        )
                );
            }
        }

        for(Version.Library library : toDownload) {
            System.out.println(
                    String.format("Downloading library \"%s\"...", library.name())
            );

            File fileLibrary;
            switch(version.assets()) {
				case Constants.ASSETS_PRE_1_6 -> {//dir should be jars/bin
                    fileLibrary = new File(dir, String.format("%s.jar", library.name().name()));
                }
				default -> {//dir should be jars/libraries
                    if (library.downloads().artifact() != null) {
                        fileLibrary = new File(dir, library.downloads().artifact().path());
                        if (!fileLibrary.getParentFile().exists()) {
                            if (!fileLibrary.getParentFile().mkdirs()) throw new RuntimeException(String.format("Failed to create file directory \"%s\"!", fileLibrary.getParentFile().getPath()));
                        }
                    } else {
                        continue;
                    }
                }
            }

            try {
                URL url;
                if (library.downloads().artifact().url().startsWith("file://")) {
                    url = MCPRD.class.getClassLoader().getResource(//Use the class loader since it grabs files from the entire classpath
                            library.downloads().artifact().url().substring("file://".length())
                    );
                } else {
                    url = new URI(library.downloads().artifact().url()).toURL();
                }
                if (url != null) {
                    downloadFile(
                            url,
                            fileLibrary, library.downloads().artifact().sha1(),
                            library.downloads().artifact().size(),
                            overwrite
                    );
                } else {
                    System.out.println(
                            String.format("Failed to download file \"%s\" due to inputstream being null!??", library.name())
                    );
                }
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(
                        String.format("Failed to download library \"%s\"!%s", library.name(), System.lineSeparator()),
                        e
                );
            }

            System.out.println(
                    String.format("Done downloading library \"%s\"!%s", library.name(), System.lineSeparator())
            );
        }

        //FIXME Dead code?
//        if (forge) {
//            if (version.assets().equals(Version.Assets.LEGACY)) {
//                switch(version.id()) {
//                    case "1.6.4" -> {
//                        //For some reason this isn't in the right package - blame Mojang
//                        File fileArgoFixed = new File(dir, "argo/argo/2.25_fixed/argo-2.25_fixed.jar");
//                        File fileArgoActual = new File(dir, "net/sourceforge/argo/argo/2.25/argo-2.25.jar");
//                        if (!fileArgoActual.getParentFile().exists()) fileArgoActual.getParentFile().mkdirs();
//                        try {
//                            Files.move(fileArgoFixed.toPath(), fileArgoActual.toPath(), StandardCopyOption.ATOMIC_MOVE);
//                        } catch (IOException e) {
//                            System.out.println(
//                                    String.format("Failed to move library file \"%s\" to \"%s\" due to IOException!", fileArgoFixed.getAbsolutePath(), fileArgoActual.getAbsolutePath())
//                            );
//                            System.out.println(e);
//                        }
//                    }
//                }
//            }
//        }
    }

    private Assets getAssets(@NotNull final Version version) throws IOException {
        URI uri;
        try {
            uri = new URI(version.assetIndex().url());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String fetchedJSON = fetchJSON(uri);
        if (!fetchedJSON.isEmpty()) {
            return moshi.adapter(Assets.class)
                    .fromJson(fetchedJSON);
        }
        throw new NullPointerException("Failed to parse JSON!");
    }

    @SuppressWarnings("ConstantValue")
    public void downloadResources(@NotNull final File dir, @NotNull Version version, @NotNull final Assets assets, final boolean useLocalAssets, final boolean windows, final boolean linux, final boolean osx, final boolean overwrite) {
        for(Entry<String, Asset> entry : assets.objects().entrySet()) {
            //TODO useLocalAssets
            File file = null;
            if (assets.map_to_resources() || (version.assets().equals(Constants.ASSETS_LEGACY) && assets.virtual())) {
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
            if (dir == null) throw new NullPointerException(String.format("Failed to get path for asset file \"%s\"!", entry.getKey()));

            boolean downloadAsset = !useLocalAssets;
            if (useLocalAssets) {
                String fpAppdata;
                if (windows) {
                    fpAppdata = System.getenv("APPDATA");
                } else if (linux) {
                    fpAppdata = System.getProperty("user.home");
                } else {//osx users aren't included here...
                    fpAppdata = null;
                }
//                System.out.println("debug");//TODO Find correct paths according to OS
                if (fpAppdata != null) {
                    File dirDotMinecraft = new File(fpAppdata, ".minecraft");
                    File dirDotAssets = new File(dirDotMinecraft, "assets");
                    File dirDotAssetsObjects = new File(dirDotAssets, "objects");
                    File dirAsset = new File(dirDotAssetsObjects, entry.getValue().hash().substring(0, 2));
                    File fileAsset = new File(dirAsset, entry.getValue().hash());
                    if (!dirDotMinecraft.exists()) {
                        downloadAsset = true;
                        System.out.println(
                                String.format("Directory \"%s\" does not exist! Resorting to download method...", dirDotMinecraft.getAbsolutePath())
                        );
                    } else {
                        if (!dirDotAssets.exists()) {
                            downloadAsset = true;
                            System.out.println(
                                    String.format("Directory \"%s\" does not exist! Resorting to download method...", dirDotAssets.getAbsolutePath())
                            );
                        } else {
                            if (!dirDotAssetsObjects.exists()) {
                                downloadAsset = true;
                                System.out.println(
                                        String.format("Directory \"%s\" does not exist! Resorting to download method...", dirDotAssetsObjects.getAbsolutePath())
                                );
                            } else {
                                if (!dirAsset.exists()) {
                                    downloadAsset = true;
                                    System.out.println(
                                            String.format("Directory \"%s\" does not exist! Resorting to download method...", dirAsset.getAbsolutePath())
                                    );
                                } else {
                                    if (!fileAsset.exists()) {
                                        downloadAsset = true;
                                        System.out.println(
                                                String.format("File \"%s\" does not exist! Resorting to download method...", fileAsset.getAbsolutePath())
                                        );
                                    } else {
                                        System.out.println(
                                                String.format("Copying local asset \"%s\" to \"%s\"...", fileAsset.getAbsolutePath(), file.getAbsolutePath())
                                        );
                                        if (overwrite) {
                                            try {
                                                Files.copy(fileAsset.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                            } catch (IOException e) {
                                                if (!(e instanceof FileAlreadyExistsException)) {
                                                    throw new RuntimeException(
                                                            String.format("Failed to copy asset \"%s\" to \"%s\"!", fileAsset.getAbsolutePath(), file.getAbsolutePath()),
                                                            e
                                                    );
                                                }
                                            }
                                        } else {
                                            try {
                                                Files.copy(fileAsset.toPath(), file.toPath());
                                            } catch (IOException e) {
                                                if (e instanceof FileAlreadyExistsException) {
                                                    System.out.println("Asset already exists in MCP. Not copying.");
                                                } else {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }
                                        System.out.println(
                                                String.format("Done!%s", System.lineSeparator())
                                        );
                                    }
                                }
                            }
                        }
                    }
                    if (!downloadAsset) continue;
                }
            }
            downloadResource(file, entry.getValue(), overwrite);
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

        URI uriFile;
        try {
            uriFile = new URI(
                    String.format(
                            Constants.URL_RESOURCE,
                            asset.hash().substring(0, 2), asset.hash()
                    )
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        System.out.println(
                String.format("Downloading asset file \"%s\"...", uriFile.getPath())
        );
        try {
            downloadFile(uriFile, fileAsset, asset.hash(), asset.size(), overwrite);
        } catch(IOException e) {
            throw new RuntimeException(String.format("Failed to download asset file \"%s\"!", fileAsset.getName()), e);
        }
        System.out.println(
                String.format("Downloaded asset file to \"%s\"!%s", fileAsset.getPath(), System.lineSeparator())
        );
    }

    public boolean downloadForgeLibs(final Version version, final File dirMCPJars, final File dirMCPJarsLib, final File dirMCP, final boolean overwrite) {
        File dirMCPLib = new File(dirMCP, "lib");
        File dirMCPConf = new File(dirMCP, "conf");
        if (!dirMCPLib.exists()) {
            if (!dirMCPLib.mkdir()) {
                System.out.println(
                        String.format("Could not download Forge libs due to directory \"%s\" not being able to be created!?", dirMCPLib.getPath())
                );
                return false;
            }
        }

        if (dirMCPLib.exists()) {
            List<String[]> libs = new ArrayList<>();// mcp/lib
            List<String[]> jarLibs = new ArrayList<>();// mcp/jars/lib (1.3.2+)
            if (MCPRD.FORGE_LIBS.containsKey(version.id())) {
                Collections.addAll(
                        libs,
                        MCPRD.FORGE_LIBS.get(version.id())[0]//lib
                );

                Collections.addAll(
                        jarLibs,
						MCPRD.FORGE_LIBS.get(version.id())[1]//jar lib
                );
            }

            int downloaded = 0;
            for(String[] lib : libs) {
                try {
                    downloadFile(
                            new URI(lib[1]),
                            new File(dirMCPLib, lib[0]),
                            null,
                            -1,
                            overwrite
                    );
                    downloaded++;
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            int downloaded2 = 0;
            if (!jarLibs.isEmpty()) {
                File dirJarsLib = new File(dirMCPJars, "lib");
                if(!dirJarsLib.exists()) dirJarsLib.mkdir();

                for(String[] jarLib : jarLibs) {
                    try {
                        downloadFile(
                                new URI(jarLib[1]),
                                new File(dirJarsLib, jarLib[0]),
                                null,
                                -1,
                                overwrite
                        );
                        downloaded2++;
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

			if (version.id().equals("1.5.2")) {
				//deobfuscation_data_1.5.2.zip is just a zip file containing "packaged.srg" (renamed as "joined.srg"), AFTER Forge 1.5.2-7.8.1.738 has been installed into MCP
				//Since MCP-RD should not be ran AFTER Forge+FML has been installed, we have to package and copy our own version and rewrite the SHA1 checksum in FML, so FML doesn't whine and error out
				File fileDeobfData = new File(dirMCPJarsLib, "deobfuscation_data_1.5.2.zip");
				if (!fileDeobfData.exists() || overwrite) {
					InputStream streamDeobf = MCPRD.class.getClassLoader().getResourceAsStream("deobfuscation_data_1.5.2.zip");
					if (streamDeobf != null) {
						try(FileOutputStream fos = new FileOutputStream(fileDeobfData)) {
							fos.write(streamDeobf.readAllBytes());
						} catch(IOException e) {
							throw new RuntimeException(e);
						}
						try {
							streamDeobf.close();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}

						System.out.println(
								String.format("Patching SHA1 checksum in FML for \"deobfuscation_data.1.5.2.zip\"...")
						);
						File fileFMLVersionProperties = new File(new File(dirMCP, "../fml/common"), "fmlversion.properties");
						if (fileFMLVersionProperties.exists()) {
							String stringFMLVersionProperties;
							try {
								stringFMLVersionProperties = Files.readString(fileFMLVersionProperties.toPath());
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							String patchedFMLVersionProperties = stringFMLVersionProperties.replaceFirst(
									"446e55cd986582c70fcf12cb27bc00114c5adfd9",
									"3a1331e6e843083d29ef1223c35e9234ae4ce61d"
							);
							if (!patchedFMLVersionProperties.equals(stringFMLVersionProperties)) {
								if (fileFMLVersionProperties.renameTo(new File(fileFMLVersionProperties.getParentFile(), fileFMLVersionProperties.getName() + ".bk"))) {
									try(FileOutputStream fos = new FileOutputStream(fileFMLVersionProperties)) {
										try(OutputStreamWriter osw = new OutputStreamWriter(fos)) {
											osw.write(patchedFMLVersionProperties);
											System.out.println(
													String.format("Done patching SHA1 checksum")
											);
										}
									} catch (FileNotFoundException e) {
										throw new RuntimeException(e);
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								} else {
									System.out.println(
											String.format("Failed to patch due to backup file already existing?!")
									);
								}
							} else {
								System.out.println(
										String.format("Has file already been patched?! Ignoring!")
								);
							}
						} else {
							System.out.println(
									String.format("Failed to patch due to file \"%s\" not existing!?", fileFMLVersionProperties.getPath())
							);
						}
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

    public boolean patchFMLHashes(final Version version, final File dirMCP) {
        File fileCoreFMLLibraries = null;
        switch(version.id()) {
            case "1.3.2" -> fileCoreFMLLibraries = new File(dirMCP, "forge/fml/common/cpw/mods/fml/relauncher/CoreFMLLibraries.java");//Still using 1.2.5 MCP style... - dirMCP uses style of mcp/forge/fml
            case "1.4.7", "1.5.2" -> fileCoreFMLLibraries = new File(dirMCP, "../fml/common/cpw/mods/fml/relauncher/CoreFMLLibraries.java");//dirMCP uses style of forge/mcp
        }

        if (fileCoreFMLLibraries != null) {
            if (fileCoreFMLLibraries.exists()) {
                //noinspection ExtractMethodRecommender
                Map<String, String> mapUpdatedHashes = new HashMap<>();
                switch(version.id()) {
                    case "1.3.2" -> {
//                    mapUpdatedHashes.put("bb672829fde76cb163004752b86b0484bd0a7f4b", "bb672829fde76cb163004752b86b0484bd0a7f4b");//argo-2.25.jar
//                    mapUpdatedHashes.put("b8e78b9af7bf45900e14c6f958486b6ca682195f", "b8e78b9af7bf45900e14c6f958486b6ca682195f");//guava-12.0.1.jar
                        mapUpdatedHashes.put("98308890597acb64047f7e896638e0d98753ae82", "2518725354c7a6a491a323249b9e86846b00df09");//asm-all-4.0.jar
                    }
                    case "1.4.7" -> {
//                        mapUpdatedHashes.put("bb672829fde76cb163004752b86b0484bd0a7f4b", "bb672829fde76cb163004752b86b0484bd0a7f4b");//argo-2.25.jar
//                        mapUpdatedHashes.put("b8e78b9af7bf45900e14c6f958486b6ca682195f", "b8e78b9af7bf45900e14c6f958486b6ca682195f");//guava-12.0.1.jar
                        mapUpdatedHashes.put("98308890597acb64047f7e896638e0d98753ae82", "2518725354c7a6a491a323249b9e86846b00df09");//asm-all-4.0.jar
//                        mapUpdatedHashes.put("b6f5d9926b0afbde9f4dbe3db88c5247be7794bb", "b6f5d9926b0afbde9f4dbe3db88c5247be7794bb");//bcprov-jdk15on-147.jar
                    }
                    case "1.5.2" -> {
//                        mapUpdatedHashes.put("58912ea2858d168c50781f956fa5b59f0f7c6b51", "58912ea2858d168c50781f956fa5b59f0f7c6b51");//argo-small-3.2.jar
//                        mapUpdatedHashes.put("931ae21fa8014c3ce686aaa621eae565fefb1a6a", "931ae21fa8014c3ce686aaa621eae565fefb1a6a");//guava-14.0-rc3.jar
//                        mapUpdatedHashes.put("054986e962b88d8660ae4566475658469595ef58", "ad568238ee36a820bd6c6806807e8a14ea34684d");//asm-all-4.1.jar
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
                        String.format("Directory \"%s\" does not exist!", fileCoreFMLLibraries)
                );
            }
        }
        return false;
    }

    public void patchFMLScript(final Version version, final File dirFml) {
        //Patch broken Python script (no SSL support)
        if (version.id().equals("1.6.4")) {
            System.out.println("Patching broken python script...");

            File fileFmlPy = new File(dirFml, "fml.py");
			if (fileFmlPy.exists()) {
				String stringFmlPy = null;
				try {
					stringFmlPy = Files.readString(fileFmlPy.toPath(), StandardCharsets.US_ASCII);
				} catch (IOException e) {
					System.out.println(
							String.format("Failed to patch file \"%s\" due to IOException!%s%s", fileFmlPy.getAbsolutePath(), System.lineSeparator(), e.toString())
					);
				}
				if (stringFmlPy != null) {
					String newFmlPy = stringFmlPy.replaceFirst(//Disable download_libraries
							" {8}failed = download_libraries\\(.+\\) or failed",
							"        # failed = download_libraries(mcp_dir, version_json['libraries'], mc_info['natives_dir']) or failed"
					);
					if (!newFmlPy.equals(stringFmlPy)) {
						if (fileFmlPy.renameTo(new File(fileFmlPy.getParentFile(), fileFmlPy.getName() + ".bk"))) {
							try {
								Files.writeString(fileFmlPy.toPath(), newFmlPy, StandardCharsets.US_ASCII);
								System.out.println(
										String.format("Patched file \"%s\" successfully!%s", fileFmlPy.getAbsolutePath(), System.lineSeparator())
								);
							} catch (IOException e) {
								System.out.println(
										String.format("Failed to write file \"%s\" due to IO Exception!%s%s%s", fileFmlPy.getAbsolutePath(), System.lineSeparator(), e, System.lineSeparator())
								);
							}
						} else {
							System.out.println(
									String.format("Failed to patch file \"%s\" because a backup file already exists!", fileFmlPy.getPath())
							);
						}
					} else {
						System.out.println(
								String.format("Failed to patch file \"%s\"!%sHas it already been patched?", fileFmlPy.getAbsolutePath(), System.lineSeparator())
						);
					}
				}
			} else {
				System.out.println(
						String.format("Could not find file \"%s\"?!", fileFmlPy.getAbsolutePath())
				);
			}
		}
    }

    public int checkJavaVersionFromWindowsRegistry() {
        if (version != null && regkeyJavaVersion != null) {
            ModuleDescriptor.Version version = ModuleDescriptor.Version.parse(this.version.id());
            if (version.compareTo(ModuleDescriptor.Version.parse("1.7.10")) >= 0) {//JDK 1.8
                if (!regkeyJavaVersion.equals("1.8")) {
                    return -2;
                } else {
                    return 1;
                }
            } else {//JDK 1.7
                if (!regkeyJavaVersion.equals("1.7") && !regkeyJavaVersion.equals("1.6")) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
        return -100;
    }

    /**
     * Fetches and parses version_manifest_v2.json
     * @return {@link VersionManifest}
     */
    public VersionManifest getVersionManifest() throws IOException {
        if (versionManifest == null) {
            try {
                URI uri = new URI(Constants.URL_VERSION_MANIFEST_V2);
                String fetchedJSON = fetchJSON(uri);
                if (!fetchedJSON.isEmpty()) {
                    versionManifest = moshi.adapter(VersionManifest.class)
                            .fromJson(fetchedJSON);
                }
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
			if (versionManifest != null) {
				if (Constants.DEBUG) {
					try {
						Files.writeString(
								new File(".", "DEBUG.VERSION_MANIFEST.RECONSTRUCTED.JSON").toPath(),
								moshi.adapter(VersionManifest.class)
										.indent("  ")
										.toJson(versionManifest),
								StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE
						);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
        }
        return versionManifest;
    }

	/**
	 * Unsafe! Use with caution!
	 * @return {@link VersionManifest} directly.
	 */
    public VersionManifest getVersionManifestUnsafe() {
        return versionManifest;
    }

    /**
     * Fetches and parses {version}.json
     * @param version Manifest Version to fetch
     * @return {@link org.slave.mcprd.models.Version} if JSON was fetched and parsed correctly. null if otherwise
     */
    public Version getVersion(final VersionManifest.Version version) throws IOException {
        if (version.url() != null) {//Should never happen, but who knows...
            try {
                URI uri = new URI(version.url());
                String fetchedJSON = fetchJSON(uri);
                if (!fetchedJSON.isEmpty()) {
                    return moshi.adapter(Version.class)
                            .fromJson(fetchedJSON);
                }
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        } else {
            throw new NullPointerException(
                    String.format("URL for version \"%s\" is null????", version.id())
            );
        }
        return null;
    }

	public Version getVersionUnsafe() {
		return version;
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

    private String fetchJSON(final URI uri) throws IOException {
        StringBuilder lines = new StringBuilder();
        try(InputStream inputStream = uri.toURL().openStream()) {
            try(InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII)) {
                try(BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                    String buffer;
                    while((buffer = bufferedReader.readLine()) != null) lines.append(buffer);
                }
            }
        }
        return lines.toString();
    }

    private <T> void serializeJSON(final File file, final Class<T> classObject, final T object, final boolean overwrite) {
        if (file.exists()) {
            if (!overwrite) {
                System.out.println(
                        String.format("File \"%s\" exists, but can't overwrite!", file.getPath())
                );
                return;
            } else {
                System.out.println(
                        String.format("File \"%s\" exists, but overwriting...%s", file.getPath(), System.lineSeparator())
                );
            }
        }
        try {
            try(FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                try(OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream)) {
                    outputStreamWriter.write(
                            moshi.adapter(classObject)
									.indent("  ")
                                    .toJson(object)
                    );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write assets JSON file \"%s\"!", file.getPath()), e);
        }
    }

    private void downloadFile(@NotNull final URI uriFile, final File file, final String sha1, final int size, final boolean overwrite) throws IOException {
        downloadFile(uriFile.toURL(), file, sha1, size, overwrite);
    }

    private void downloadFile(@NotNull final URL url, final File file, final String sha1, final int size, final boolean overwrite) throws IOException {
        if (!file.getParentFile().exists()) throw new RuntimeException(String.format("Parent directory \"%s\" does not exist!?", file.getParentFile().getPath()));
        if (file.isFile()) {
            boolean invalidSHA = false;
            if (sha1 != null && size != -1) {//sha1=null & size=-1 are for forge libs
                try(InputStream is = new FileInputStream(file)) {
                    messageDigestSHA1.reset();
                    messageDigestSHA1.update(is.readAllBytes());
                    byte[] ba = messageDigestSHA1.digest();
                    //Adapted from https://www.geeksforgeeks.org/sha-1-hash-in-java/
                    BigInteger bi = new BigInteger(1, ba);
                    StringBuilder sbReadSha1 = new StringBuilder(bi.toString(16));
                    while (sbReadSha1.length() < 40) sbReadSha1.insert(0, "0");
                    String readSha1 = sbReadSha1.toString();
                    invalidSHA = !readSha1.equalsIgnoreCase(sha1);
                }
            }
            if (invalidSHA) {//Allow overwrite if invalid SHA
                System.out.println(
                        String.format("File \"%s\" has an invalid file hash! Redownloading...", file.getPath())
                );
            } else if (!overwrite) {
                System.out.println(
                        String.format("File \"%s\" already exists but can't overwrite", file.getPath())
                );
                return;
            } else {
                System.out.println(
                        String.format("File \"%s\" already exists, but overwriting...", file.getPath())
                );
            }
        }
        try(InputStream is = url.openStream()) {//Only open stream if allowed to download
            ReadableByteChannel readableByteChannel = Channels.newChannel(is);
            try(FileOutputStream fos = new FileOutputStream(file)) {
                fos.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);//Will ONLY transfer UP TO 16 MiB... but that shouldn't be an issue...
            }
        }
    }

}
