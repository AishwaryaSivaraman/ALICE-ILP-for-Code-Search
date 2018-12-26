package extractor.extract;
/*
 * @(#) UTFile.java
 *
 * Copyright 2013 The Software Evolution and Analysis Laboratory Lab 
 * Electrical and Computer Engineering, The University of Texas at Austin
 * ACES 5.118, C5000, 201 E 24th Street, Austin, TX 78712-0240
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;


/**
 * @author Myoungkyu Song
 * @date Oct 22, 2013
 * @since J2SE-1.5 (Java SE 7 [1.7.0_40])
 */
public class UTFile {
	private static final int	BUF_SIZE	= 8192;

	/**
	 * Copy to.
	 * 
	 * @param is the is
	 * @param os the os
	 * @return the long
	 */
	public static long copyTo(InputStream is, OutputStream os) {
		byte[] buf = new byte[BUF_SIZE];
		long tot = 0;
		int len = 0;
		try {
			while (-1 != (len = is.read(buf))) {
				os.write(buf, 0, len);
				tot += len;
			}
		} catch (IOException ioe) {
			throw new RuntimeException("error - ", ioe);
		}
		return tot;
	}

	/**
	 * Convert file to i file.
	 * 
	 * @param aFile the file
	 * @return the i file
	 */
//	public static IFile convertFileToIFile(File aFile) {
////		IWorkspace workspace = ResourcesPlugin.getWorkspace();
////		
////		IPath location = org.eclipse.core.runtime.Path.fromOSString(aFile.getAbsolutePath());
////		IFile iFile = workspace.getRoot().getFile(location);
////		return iFile;
//	}

	/**
	 * Read2lines from file.
	 * 
	 * @param fileName the file name
	 * @param list1 the list1
	 * @param list2 the list2
	 */
	public void read2linesFromFile(String fileName, List<String> list1, List<String> list2) {

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileName));
			String line1 = null, line2 = null;

			while ((line1 = in.readLine()) != null) {
				line1 = line1.trim();

				if (line1.isEmpty()) {
					continue;
				} else {
					line2 = in.readLine();
					line2 = line2.trim();
				}
				list1.add(line1);
				list2.add(line2);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Read file in list buffer.
	 * 
	 * @param fileName the file name
	 * @param buffer the buffer
	 */
	public void readFileInListBuffer(String fileName, List<String> buffer) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileName));
			String line = null;
			boolean isComment = false;

			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("//")) {
					continue;
				}
				if (isComment) {
					if (line.endsWith("*/"))
						isComment = false;
					continue;
				}
				if (line.startsWith("/*")) {
					isComment = true;
				} else if (line.isEmpty()) {
					buffer.add("");
				} else {
					buffer.add(line);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Read reverse by token.
	 * 
	 * @param file the file
	 * @param token the token
	 * @param bgn the bgn
	 * @param end the end
	 * @return the string
	 */
	public String readReverseByToken(File file, String token, String bgn, String end) {
		BufferedReader in = null;
		String result = null;
		try {
			in = new BufferedReader(new InputStreamReader(new ReverseLineInputStream(file)));

			while (true) {
				String line = in.readLine();
				if (line == null) {
					break;
				} else if (line.contains("Command-line arguments") && line.contains(token)) {

					// DBG__________________________________________------------------------
					// [DBG] FOUND: Command-line arguments: -product org.eclipse.sdk.ide -data
					// /Users/mksong/workspaceCHIME/../runtime-ChimeUI
					// -dev file:/Users/mksong/workspaceCHIME/.metadata/.plugins/org.eclipse.pde.core/ChimeUI/dev.properties
					// -os macosx -ws cocoa -arch x86_64 -consoleLog
					// DBG__________________________________________------------------------
					// [DBG] Users/mksong/workspaceCHIME/
					// DBG__________________________________________------------------------

					int idx_token = line.indexOf(token);
					int idx_bgn_path = line.indexOf(bgn, idx_token);
					int idx_end_path = line.indexOf(end, idx_bgn_path);
					result = line.substring(idx_bgn_path, idx_end_path);
					break;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return result;
	}

	class ReverseLineInputStream extends InputStream {
		RandomAccessFile	in;
		long				currentLineStart	= -1;
		long				currentLineEnd		= -1;
		long				currentPos			= -1;
		long				lastPosInFile		= -1;

		/**
		 * Instantiates a new reverse line input stream.
		 * 
		 * @param file the file
		 * @throws FileNotFoundException the file not found exception
		 */
		public ReverseLineInputStream(File file) throws FileNotFoundException {
			in = new RandomAccessFile(file, "r");
			currentLineStart = file.length();
			currentLineEnd = file.length();
			lastPosInFile = file.length() - 1;
			currentPos = currentLineEnd;
		}

		/**
		 * Find prev line.
		 * 
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void findPrevLine() throws IOException {

			currentLineEnd = currentLineStart;

			if (currentLineEnd == 0) {
				currentLineEnd = -1;
				currentLineStart = -1;
				currentPos = -1;
				return;
			}

			long filePointer = currentLineStart - 1;

			while (true) {
				filePointer--;

				if (filePointer < 0) {
					break;
				}

				in.seek(filePointer);
				int readByte = in.readByte();

				if (readByte == 0xA && filePointer != lastPosInFile) {
					break;
				}
			}
			currentLineStart = filePointer + 1;
			currentPos = currentLineStart;
		}

		/* (non-Javadoc)
		 * @see java.io.InputStream#read()
		 */
		public int read() throws IOException {

			if (currentPos < currentLineEnd) {
				in.seek(currentPos++);
				int readByte = in.readByte();
				return readByte;

			} else if (currentPos < 0) {
				return -1;
			} else {
				findPrevLine();
				return read();
			}
		}
	}

	/**
	 * Read file.
	 * 
	 * @param fileName the file name
	 * @return the string
	 */
	public static String readFile(String fileName) {
		BufferedReader in = null;
		StringBuilder sb = new StringBuilder();
		try {
			in = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Read file.
	 * 
	 * @param fileName the file name
	 * @return the string
	 */
	public static String readFileWithNewLine(String fileName) {
		BufferedReader in = null;
		StringBuilder sb = new StringBuilder();
		try {
			in = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append(System.getProperty("line.separator"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Read file to list.
	 * 
	 * @param fileName the file name
	 * @return the list
	 */
	public static List<String> readFileToList(String fileName) {
		List<String> list = new ArrayList<String>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = in.readLine()) != null) {
				list.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	/**
	 * Read entire file.
	 * 
	 * @param filename the filename
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String readEntireFile(String filename) throws IOException {
		FileReader in = new FileReader(filename);
		StringBuilder contents = new StringBuilder();
		char[] buffer = new char[4096];
		int read = 0;
		do {
			contents.append(buffer, 0, read);
			read = in.read(buffer);
		} while (read >= 0);
		in.close();
		return contents.toString();
	}

	/**
	 * Read file without space.
	 * 
	 * @param fileName the file name
	 * @return the string
	 */
	public static String readFileWithoutSpace(String fileName) {
		BufferedReader in = null;
		String line;
		StringBuilder buf = new StringBuilder();
		try {
			in = new BufferedReader(new FileReader(fileName));
			while ((line = in.readLine()) != null) {
				if (line.trim().startsWith("#")) {
					continue;
				}
				buf.append(line.trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buf.toString();
	}

	/**
	 * Creates the dir.
	 * 
	 * @param dirName the dir name
	 */
	public void createDir(String dirName) {
		File f = new File(dirName);
		if (f.mkdirs()) {
			System.out.println("[DBG] DIR CREATED: " + f.getAbsolutePath());
		}
	}

	/**
	 * Gets the contents.
	 * 
	 * @param fileName the file name
	 * @return the contents
	 */
	public static String getContents(String fileName) {
		String L = System.getProperty("line.separator");
		BufferedReader in = null;
		StringBuilder buf = new StringBuilder();
		try {
			in = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = in.readLine()) != null) {
				buf.append(line + L);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buf.toString();
	}

	/**
	 * Gets the dir name.
	 * 
	 * @param fileName the file name
	 * @return the dir name
	 */
	public String getDirName(String fileName) {
		String S = System.getProperty("file.separator");
		int idx = fileName.lastIndexOf(S);
		if (idx == -1) {
			idx = fileName.lastIndexOf("/");
			if (idx == -1) {
				idx = fileName.lastIndexOf("\\");
			}
		}
		return fileName.substring(0, idx);
	}

	/**
	 * Gets the attributes.
	 * 
	 * @param pathStr the path str
	 * @return the attributes
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static long getLastModifiedTime(String pathStr) throws IOException {
		Path p = Paths.get(pathStr);
		BasicFileAttributes view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
		System.out.println(view.creationTime() + ", " + view.lastModifiedTime());
		return view.lastModifiedTime().toMillis();
	}

	/**
	 * Gets the short file name.
	 * 
	 * @param fileName the file name
	 * @return the short file name
	 */
	public String getShortFileName(String fileName) {
		String S = System.getProperty("file.separator");
		int idx = fileName.lastIndexOf(S);
		if (idx == -1) {
			idx = fileName.lastIndexOf("/");
			if (idx == -1) {
				idx = fileName.lastIndexOf("\\");
			}
		}
		return fileName.substring(idx + 1);
	}

	/**
	 * Gets the string from input stream.
	 * 
	 * @param is the is
	 * @return the string from input stream
	 */
	public static String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			// char[] cbuf = new char[2];
			// br.read(cbuf, 0, 2); // avoid special characters like "??"
			while ((line = br.readLine()) != null) {
				sb.append(line + '\n');
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Write.
	 * 
	 * @param file the file
	 * @param buf the buf
	 */
	public static void write(String file, String buf) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file, "UTF-8");
			writer.print(buf);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Write file.
	 * 
	 * @param fileName the batchfile
	 * @param outputList the output list
	 * @return the string
	 */
	public static String writeFile(String fileName, List<String> outputList) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			for (int i = 0; i < outputList.size(); i++) {
				String elem = outputList.get(i);
				writer.println(elem);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
				return fileName;
			}
		}
		return null;
	}
}
