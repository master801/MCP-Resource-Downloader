package org.slave.mcprd.cmd.test;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Master on 1/23/2025 at 4:24 AM
 *
 * @author Master
 */
@UtilityClass
public class Utilities {

    public static void extractZip(@NonNull final ZipFile zipFile, @NonNull final File dirDest) throws IOException {
        if (!dirDest.exists()) {
            if (!dirDest.mkdirs()) throw new IOException(String.format("Failed to create directory \"%s\" due to an IO error!", dirDest.getAbsolutePath()));
        }
        Enumeration<? extends ZipEntry> enumerationZE = zipFile.entries();
        while(enumerationZE.hasMoreElements()) {
            ZipEntry ze =  enumerationZE.nextElement();
            File fileZE = new File(dirDest, ze.getName());
            if (ze.getName().endsWith("/")) {//denotes itself as a directory
                if (!fileZE.exists() && !fileZE.mkdirs()) throw new IOException(String.format("Failed to create directory \"%s\" due to an IO error!", fileZE.getAbsolutePath()));
            } else {
                if (!fileZE.getParentFile().exists() && !fileZE.getParentFile().mkdirs()) throw new IOException(String.format("Failed to create directory \"%s\" due to an IO error!", fileZE.getParentFile().getAbsolutePath()));
                try(InputStream isZE = zipFile.getInputStream(ze)) {
                    Files.copy(isZE, fileZE.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Patches a jar file with a zip patch
     * For ModLoader specifically
     *
     * @param fileJar The jar to patch
     * @param fileZipPatch The zip patch
     */
    public static void patchJar(final File fileJar, final File fileZipPatch, final File fileJarPatched) throws IOException {
        Files.copy(fileJar.toPath(), fileJarPatched.toPath());

        try(FileOutputStream fos = new FileOutputStream(fileJarPatched)) {
            try(JarOutputStream jos = new JarOutputStream(fos)) {
                try(ZipFile zip = new ZipFile(fileZipPatch)) {
                    //Manually transfer files from jar to jar - without any files from the zip patch (will create entry conflict if added)
                    try(JarFile jarFile = new JarFile(fileJar)) {
                        Enumeration<JarEntry> enumerationJar = jarFile.entries();
                        while(enumerationJar.hasMoreElements()) {
                            JarEntry jarEntry = enumerationJar.nextElement();
                            if (jarEntry.getName().startsWith("META-INF")) continue;//Strip META-INF folder and files
                            if (zip.getEntry(jarEntry.getName()) == null) {
                                jos.putNextEntry(
                                        new JarEntry(jarEntry)
                                );
                                jarFile.getInputStream(jarEntry).transferTo(jos);
                                jos.closeEntry();
                            }
                        }
                    }

                    //Finally add the patched files
                    Enumeration<? extends ZipEntry> enumerationZip = zip.entries();
                    while(enumerationZip.hasMoreElements()) {
                        ZipEntry ze = enumerationZip.nextElement();
                        jos.putNextEntry(
                                new ZipEntry(ze)
                        );
                        zip.getInputStream(ze).transferTo(jos);
                        jos.closeEntry();
                    }
                }
            }
        }
    }

}
