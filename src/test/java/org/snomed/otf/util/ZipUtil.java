package org.snomed.otf.util;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	public static File zipDirectoryRemovingCommentsAndBlankLines(String directoryPath) throws IOException {
		File directory = new File(directoryPath);
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("This is not a directory " + directory.getAbsolutePath());
		}
		Path zip = Files.createTempFile("zipped-directory_" + new Date().getTime(), "zip");
		File zipFile = zip.toFile();
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(zipOutputStream));
			Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					File file = path.toFile();
					zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
					try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
						String line;
						while ((line = bufferedReader.readLine()) != null) {
							if (!line.isEmpty() && !line.startsWith("#")) {
								bufferedWriter.write(line);
								bufferedWriter.newLine();
							}
						}
					}
					bufferedWriter.flush();
					return FileVisitResult.CONTINUE;
				}
			});
		}
		return zipFile;
	}

	public static InputStream getZipEntryStreamOrThrow(File file, String filenamePart) throws IOException {
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getName().contains(filenamePart)) {
				return zipFile.getInputStream(entry);
			}
		}
		throw new FileNotFoundException("No zip file entry found matching filename part '" + filenamePart + "'");
	}
}
