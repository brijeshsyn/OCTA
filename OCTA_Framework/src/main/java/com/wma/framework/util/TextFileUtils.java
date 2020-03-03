package com.wma.framework.util;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.wma.framework.common.ConfigProvider;

public class TextFileUtils {
	/**
	 * Private constructor to prevent object creation
	 */
	private TextFileUtils() {
	};

	/**
	 * This function is used to get the latest file for system user form given directory
	 * 
	 * @param dirPath
	 *         path of directory from which latest file is to be fetched 
	 * @return File
	 * @author pathaky
	 */
	public static File getLatestFileForSysUserFromDir(String dirPath) {
		File dir = new File(dirPath);
		String username = ConfigProvider.getInstance().getUserFirstAndLastName();
		
		File[] files = dir.listFiles();
		if(files == null || files.length == 0 ) {
			return null;
		}
		
		File lastModifiedFile = files[0];
		for (int i=1; i < files.length; i++ ) {
			
			if ((lastModifiedFile.lastModified() < files[i].lastModified())
					&& files[i].getName().toLowerCase().contains(username.toLowerCase())) {
				lastModifiedFile = files[i];
			}
		}
		return lastModifiedFile;
	}

	/**
	 * Get the newest file for a specific extension
	 * 
	 * @param filePath
	 * @param extension
	 * @return
	 */
	public static File getLatestFile(String filePath, String extension) {
		File theNewestFile = null;
		File dir = new File(filePath);
		FileFilter fileFilter = new WildcardFileFilter("*." + extension);
		File[] files = dir.listFiles(fileFilter);

		Arrays.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
			}
		});

		if (files.length > 0) {
			/** The newest file comes first **/
			Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
			theNewestFile = files[0];
		}

		return theNewestFile;
	}

	public static File getLatestFolder(String dirPath) {
		File dir = new File("\\" + dirPath);
		File max = null;
		for (File file : dir.listFiles()) {
			if (file.isDirectory() && (max == null || max.lastModified() < file.lastModified())) {
				max = file;
			}
		}
		return max;
	}

	/**
	 * This function is used get the latest folder within given folder
	 * 
	 * @param path
	 *            Provide path of folder in which we need to find latest folder
	 * @return String Folder name along with path will get returned
	 * @author pathaky
	 */
	public static String getLatestFolderFrom(String path) {

		Path parentFolder = Paths.get(path);
		String slatestFolder;

		Optional<File> mostRecentFolder = Arrays.stream(parentFolder.toFile().listFiles()).filter(f -> f.isDirectory())
				.max((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

		if (mostRecentFolder.isPresent()) {
			File mostRecent = mostRecentFolder.get();
			slatestFolder = mostRecent.getPath();
		} else {
			slatestFolder = "";
		}
		return slatestFolder;

	}

	/**
	 * This function is used to change the last modified date of file
	 * 
	 * @param filepath
	 *            Provide file name along with path
	 * @return String
	 * @author pathaky
	 */
	public static String modifyLastModified(String filepath) {

		File fileToChange = new File(filepath);

		Date filetime = new Date(fileToChange.lastModified());
		System.out.println(filetime.toString());

		System.out.println(fileToChange.setLastModified(System.currentTimeMillis()));

		filetime = new Date(fileToChange.lastModified());
		System.out.println(filetime.toString());

		String newLastModifiedString = new SimpleDateFormat("yyyyMMddHHmm").format(filetime);
		System.out.println("newLastModifiedString: " + newLastModifiedString);

		return newLastModifiedString;
	}

}
