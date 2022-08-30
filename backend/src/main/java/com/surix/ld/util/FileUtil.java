package com.surix.ld.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileUtil {
	
	public InputStream loadFile(String fileName) throws FileNotFoundException  {
		FileInputStream fis = new FileInputStream(fileName);
		BufferedInputStream bis = new BufferedInputStream(fis);
		return bis;
	}

	public InputStream inflateFile(String fileName) throws IOException {
		return new GZIPInputStream(loadFile(fileName));
	}

	public OutputStream saveFile(String fileName) throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		return bos;
	}

	public OutputStream deflateFile(String fileName) throws IOException {
		return new GZIPOutputStream(saveFile(fileName));
	}

	public void streamOut(InputStream in, OutputStream out) throws IOException {
		try {
			byte[] buf = new byte[1024];
			int count = 0;
			while ((count = in.read(buf)) != -1) {
				out.write(buf, 0, count);
			}
		} finally {
			in.close();
			out.close();
		}
	}

	public boolean delete(File file) {
		boolean result = true;
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				result = delete(files[i]);
			}
		}
		return file.delete() && result;
	}
}
