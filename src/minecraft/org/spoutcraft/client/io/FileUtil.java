/*
 * This file is part of Spoutcraft (http://www.spout.org/).
 *
 * Spoutcraft is licensed under the SpoutDev License Version 1.
 *
 * Spoutcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * Spoutcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev license version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spoutcraft.client.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import net.minecraft.client.Minecraft;

import org.spoutcraft.client.SpoutClient;

public class FileUtil {
	private static final String[] validExtensions = {"txt", "yml", "xml", "png", "jpg", "ogg", "midi", "wav", "zip"};
	private static final HashMap<String, String> fileNameCache = new HashMap<String, String>();
	public static File getCacheDirectory() {
		boolean wasSandboxed = SpoutClient.isSandboxed();
		if (wasSandboxed) {
			SpoutClient.disableSandbox();
		}

		File directory = new File(getSpoutcraftDirectory(), "cache");
		if (!directory.exists()) {
			directory.mkdir();
		}

		if (wasSandboxed) {
			SpoutClient.enableSandbox();
		}
		return directory;
	}

	public static File getSpoutcraftDirectory() {
		boolean wasSandboxed = SpoutClient.isSandboxed();
		if (wasSandboxed) {
			SpoutClient.disableSandbox();
		}

		File directory = new File(Minecraft.getMinecraftDir(), "spoutcraft");
		if (!directory.exists()) {
			directory.mkdir();
		}

		if (wasSandboxed) {
			SpoutClient.enableSandbox();
		}
		return directory;
	}

	public static File getTempDirectory() {
		boolean wasSandboxed = SpoutClient.isSandboxed();
		if (wasSandboxed) {
			SpoutClient.disableSandbox();
		}

		File directory = new File(getCacheDirectory(), "temp");
		if (!directory.exists()) {
			directory.mkdir();
		}

		if (wasSandboxed) {
			SpoutClient.enableSandbox();
		}
		return directory;
	}

	public static File getStatsDirectory() {
		boolean wasSandboxed = SpoutClient.isSandboxed();
		if (wasSandboxed) {
			SpoutClient.disableSandbox();
		}

		File directory = new File(Minecraft.getMinecraftDir(), "stats");
		if (!directory.exists()) {
			directory.mkdir();
		}

		if (wasSandboxed) {
			SpoutClient.enableSandbox();
		}
		return directory;
	}

	public static void migrateOldFiles() {
		File directory = new File(Minecraft.getMinecraftDir(), "spout");
		if (directory.exists()) {
			try {
				FileUtils.copyDirectory(directory, getCacheDirectory(), true);
				FileUtils.deleteDirectory(getTempDirectory());
			}
			catch (Exception e) {}
		}
	}

	public static void deleteTempDirectory() {
		try {
			FileUtils.deleteDirectory(getTempDirectory());
		}
		catch (Exception e) {}
	}

	public static File findFile(String plugin, String fileName) {
		File directory = new File(getCacheDirectory(), plugin);
		if (directory.isDirectory() && directory.exists()) {
			Collection<File> files = FileUtils.listFiles(directory, null, true);
			for (File file : files) {
				String name = getFileName(file.getPath());
				if (name != null && name.equals(fileName)) {
					return file;
				}
			}
		}
		return null;
	}

	public static File getTexturePackDirectory() {
		boolean wasSandboxed = SpoutClient.isSandboxed();
		if (wasSandboxed) {
			SpoutClient.disableSandbox();
		}

		File directory = new File(Minecraft.getMinecraftDir(), "texturepacks");
		if (!directory.exists()) {
			directory.mkdir();
		}

		if (wasSandboxed) {
			SpoutClient.enableSandbox();
		}
		return directory;
	}

	public static File getSelectedTexturePackZip() {
		boolean wasSandboxed = SpoutClient.isSandboxed();
		if (wasSandboxed) {
			SpoutClient.disableSandbox();
		}

		String fileName = Minecraft.theMinecraft.renderEngine.texturePack.selectedTexturePack.texturePackFileName;
		File file = new File(getTexturePackDirectory(), fileName);
		if (!file.exists()) {
			file = new File(new File(Minecraft.getAppDir("minecraft"), "texturepacks"), fileName);
		}

		if (wasSandboxed) {
			SpoutClient.enableSandbox();
		}
		return file;
	}

	public static String getFileName(String url) {
		if (fileNameCache.containsKey(url)) {
			return fileNameCache.get(url);
		}
		int end = url.lastIndexOf("?");
		int lastDot = url.lastIndexOf('.');
		int slash = url.lastIndexOf('/');
		int forwardSlash = url.lastIndexOf("\\");
		slash = slash > forwardSlash ? slash : forwardSlash;
		end = end == -1 || lastDot > end ? url.length() : end;
		String result = url.substring(slash + 1, end).replaceAll("%20", " ");
		if (url.contains("?")) {
			//Use hashcode instead.
			String ext = FilenameUtils.getExtension(result);
			result = url.hashCode() + (!ext.isEmpty()?"." + ext:"");
		}
		fileNameCache.put(url, result);
		return result;
	}

	public static boolean isAudioFile(String file) {
		String extension = FilenameUtils.getExtension(file);
		if (extension != null) {
			return extension.equalsIgnoreCase("ogg") || extension.equalsIgnoreCase("wav") || extension.matches(".*[mM][iI][dD][iI]?$");
		}
		return false;
	}

	public static boolean isImageFile(String file) {
		String extension = FilenameUtils.getExtension(file);
		if (extension != null) {
			return extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpg");
		}
		return false;
	}

	public static boolean isZippedFile(String file) {
		String extension = FilenameUtils.getExtension(file);
		if (extension != null) {
			return extension.equalsIgnoreCase("zip");
		}
		return false;
	}

	public static long getCRC(File file, byte[] buffer) {
		FileInputStream in = null;

		try {
			in = new FileInputStream(file);
			return getCRC(in, buffer);
		} catch (FileNotFoundException e) {
			return 0;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static long getCRC(URL url, byte[] buffer) {
		InputStream in = null;

		try {
			in = url.openStream();
			return getCRC(in, buffer);
		} catch (IOException e) {
			return 0;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}

	}

	public static long getCRC(InputStream in, byte[] buffer) {
		long hash = 1;

		int read = 0;
		int i;
		while (read >= 0) {
			try {
				read = in.read(buffer);
				for (i=0; i < read; i++) {
					hash += (hash << 5) + (long)buffer[i];
				}
			} catch (IOException ioe) {
				return 0;
			}
		}

		return hash;
	}

	public static boolean canCache(File file) {
		String filename = FileUtil.getFileName(file.getPath());
		return FilenameUtils.isExtension(filename, validExtensions);
	}

	public static boolean canCache(String fileUrl) {
		String filename = FileUtil.getFileName(fileUrl);
		return FilenameUtils.isExtension(filename, validExtensions);
	}
}
