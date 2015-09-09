/**
 * Copyright (c) 2011-2015, Jeff  Son   (jeffson.app@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sxb.kit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Enumeration;

/**
 * FileKit. 文件工具类
 */
public class FileKit {
	public static final int BUFFER_SIZE = StreamKits.BUFFER_SIZE;

	// ---------------------------------------------------------------------
	// Copy methods for java.io.File
	// ---------------------------------------------------------------------

	/**
	 * Copy the contents of the given input File to the given output File.
	 * 
	 * @param in
	 *            the file to copy from
	 * @param out
	 *            the file to copy to
	 * @return the number of bytes copied
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static int copy(File in, File out) throws IOException {
		Assert.notNull(in, "No input File specified");
		Assert.notNull(out, "No output File specified");
		return copy(new BufferedInputStream(new FileInputStream(in)),
				new BufferedOutputStream(new FileOutputStream(out)));
	}

	/**
	 * Copy the contents of the given byte array to the given output File.
	 * 
	 * @param in
	 *            the byte array to copy from
	 * @param out
	 *            the file to copy to
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static void copy(byte[] in, File out) throws IOException {
		Assert.notNull(in, "No input byte array specified");
		Assert.notNull(out, "No output File specified");
		ByteArrayInputStream inStream = new ByteArrayInputStream(in);
		OutputStream outStream = new BufferedOutputStream(new FileOutputStream(
				out));
		copy(inStream, outStream);
	}

	/**
	 * Copy the contents of the given input File into a new byte array.
	 * 
	 * @param in
	 *            the file to copy from
	 * @return the new byte array that has been copied to
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static byte[] copyToByteArray(File in) throws IOException {
		Assert.notNull(in, "No input File specified");
		return copyToByteArray(new BufferedInputStream(new FileInputStream(in)));
	}

	// ---------------------------------------------------------------------
	// Copy methods for java.io.InputStream / java.io.OutputStream
	// ---------------------------------------------------------------------

	/**
	 * Copy the contents of the given InputStream to the given OutputStream.
	 * Closes both streams when done.
	 * 
	 * @param in
	 *            the stream to copy from
	 * @param out
	 *            the stream to copy to
	 * @return the number of bytes copied
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static int copy(InputStream in, OutputStream out) throws IOException {
		Assert.notNull(in, "No InputStream specified");
		Assert.notNull(out, "No OutputStream specified");
		try {
			return StreamKits.copy(in, out);
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
			}
			try {
				out.close();
			} catch (IOException ex) {
			}
		}
	}

	/**
	 * Copy the contents of the given byte array to the given OutputStream.
	 * Closes the stream when done.
	 * 
	 * @param in
	 *            the byte array to copy from
	 * @param out
	 *            the OutputStream to copy to
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static void copy(byte[] in, OutputStream out) throws IOException {
		Assert.notNull(in, "No input byte array specified");
		Assert.notNull(out, "No OutputStream specified");
		try {
			out.write(in);
		} finally {
			try {
				out.close();
			} catch (IOException ex) {
			}
		}
	}

	/**
	 * Copy the contents of the given InputStream into a new byte array. Closes
	 * the stream when done.
	 * 
	 * @param in
	 *            the stream to copy from
	 * @return the new byte array that has been copied to
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static byte[] copyToByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
		copy(in, out);
		return out.toByteArray();
	}

	// ---------------------------------------------------------------------
	// Copy methods for java.io.Reader / java.io.Writer
	// ---------------------------------------------------------------------

	/**
	 * Copy the contents of the given Reader to the given Writer. Closes both
	 * when done.
	 * 
	 * @param in
	 *            the Reader to copy from
	 * @param out
	 *            the Writer to copy to
	 * @return the number of characters copied
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static int copy(Reader in, Writer out) throws IOException {
		Assert.notNull(in, "No Reader specified");
		Assert.notNull(out, "No Writer specified");
		try {
			int byteCount = 0;
			char[] buffer = new char[BUFFER_SIZE];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
			}
			out.flush();
			return byteCount;
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
			}
			try {
				out.close();
			} catch (IOException ex) {
			}
		}
	}

	/**
	 * Copy the contents of the given String to the given output Writer. Closes
	 * the writer when done.
	 * 
	 * @param in
	 *            the String to copy from
	 * @param out
	 *            the Writer to copy to
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static void copy(String in, Writer out) throws IOException {
		Assert.notNull(in, "No input String specified");
		Assert.notNull(out, "No Writer specified");
		try {
			out.write(in);
		} finally {
			try {
				out.close();
			} catch (IOException ex) {
			}
		}
	}

	/**
	 * Copy the contents of the given Reader into a String. Closes the reader
	 * when done.
	 * 
	 * @param in
	 *            the reader to copy from
	 * @return the String that has been copied to
	 * @throws IOException
	 *             in case of I/O errors
	 */
	public static String copyToString(Reader in) throws IOException {
		StringWriter out = new StringWriter();
		copy(in, out);
		return out.toString();
	}
	
	
	public static File mkDirs(String path) {
	    return mkDirs(new File(path));
	  }

	  public static File mkDirs(File file) {
	    if (file == null) {
	      throw new FileException("File could not be null.");
	    }
	    File parent = file.getParentFile();
	    if (!parent.exists()) {
	      if (!parent.mkdirs()) {
	        throw new FileException("Directory " + parent.getAbsolutePath() + " not exists and can not create directory.");
	      }
	    }
	    return file;
	  }

	  public static boolean exist(String file) {
	    Enumeration<URL> urls = null;
	    try {
	      urls = Thread.currentThread().getContextClassLoader().getResources(file);
	      while (urls.hasMoreElements()) {
	        return true;
	      }
	    } catch (IOException e) {
	      throw new FileException("Could not getResource from file - " + file, e);
	    }
	    return false;
	  }

	/**
	 * 删除指定文件 循环删除指定目录的下的文件以及文件夹
	 * 
	 * @param file
	 */
	public static void delete(File file) {
		if (file != null && file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					delete(files[i]);
				}
			}
			file.delete();
		}
	}
	
}

/**
 * FileException
 */
class FileException extends RuntimeException {

  /**
	 * 
	 */
	private static final long serialVersionUID = -1551251728819340615L;

public FileException() {
  }

  public FileException(String message) {
    super(message);
  }

  public FileException(Throwable cause) {
    super(cause);
  }

  public FileException(String message, Throwable cause) {
    super(message, cause);
  }
}
