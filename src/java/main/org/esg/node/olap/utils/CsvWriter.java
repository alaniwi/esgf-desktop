package org.esg.node.olap.utils;

/*
 * Java CSV is a stream based library for reading and writing
 * CSV and other delimited data.
 *   
 * Copyright (C) Bruce Dunwiddie bruce@csvreader.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

import java.io.*;
import java.nio.charset.Charset;

/**
 * A stream based writer for writing delimited text data to a file or a stream.
 */
public class CsvWriter {
	private ByteArrayOutputStream outputStream = null;

	private boolean firstColumn = true;
	private boolean useCustomRecordDelimiter = false;
	private Charset charset = null;

	// this holds all the values for switches that the user is allowed to set
	private UserSettings userSettings = new UserSettings();

	private boolean initialized = false;
	private boolean closed = false;

	/**
	 * Double up the text qualifier to represent an occurrence of the text
	 * qualifier.
	 */
	public static final int ESCAPE_MODE_DOUBLED = 1;

	/**
	 * Use a backslash character before the text qualifier to represent an
	 * occurrence of the text qualifier.
	 */
	public static final int ESCAPE_MODE_BACKSLASH = 2;

	/**
	 * Creates a {@link com.csvreader.CsvWriter CsvWriter} object using a file
	 * as the data destination.
	 * 
	 * @param delimiter
	 *            The character to use as the column delimiter.
	 * @param charset
	 *            The {@link java.nio.charset.Charset Charset} to use while
	 *            writing the data.
	 */
	public CsvWriter(char delimiter, Charset charset) {
		if (charset == null) {
			throw new IllegalArgumentException("Parameter charset can not be null.");
		}
		userSettings.Delimiter = delimiter;
		this.setCharset(charset);
	}

	/**
	 * Creates a {@link com.csvreader.CsvWriter CsvWriter} object using a file
	 * as the data destination.&nbsp;Uses a comma as the column delimiter and
	 * ISO-8859-1 as the {@link java.nio.charset.Charset Charset}.
	 */
	public CsvWriter() {
		this(Letters.COMMA, Charset.forName("ISO-8859-1"));
	}

	/**
	 * Creates a {@link com.csvreader.CsvWriter CsvWriter} object using a Writer
	 * to write data to. 
	 * @param outputStream
	 *            The stream to write the column delimited data to.
	 * @param delimiter
	 *            The character to use as the column delimiter.
	 */
	public CsvWriter(ByteArrayOutputStream outputStream, char delimiter) {
		if (outputStream == null) {
			throw new IllegalArgumentException("Parameter outputStream can not be null.");
		}
		this.outputStream = outputStream;
		userSettings.Delimiter = delimiter;
		initialized = true;
	}

	/**
	 * Gets the character being used as the column delimiter.
	 * 
	 * @return The character being used as the column delimiter.
	 */
	public char getDelimiter() {
		return userSettings.Delimiter;
	}

	/**
	 * Sets the character to use as the column delimiter.
	 * 
	 * @param delimiter
	 *            The character to use as the column delimiter.
	 */
	public void setDelimiter(char delimiter) {
		userSettings.Delimiter = delimiter;
	}

	public char getRecordDelimiter() {
		return userSettings.RecordDelimiter;
	}

	/**
	 * Sets the character to use as the record delimiter.
	 * 
	 * @param recordDelimiter
	 *            The character to use as the record delimiter. Default is
	 *            combination of standard end of line characters for Windows,
	 *            Unix, or Mac.
	 */
	public void setRecordDelimiter(char recordDelimiter) {
		useCustomRecordDelimiter = true;
		userSettings.RecordDelimiter = recordDelimiter;
	}

	/**
	 * Gets the character to use as a text qualifier in the data.
	 * 
	 * @return The character to use as a text qualifier in the data.
	 */
	public char getTextQualifier() {
		return userSettings.TextQualifier;
	}

	/**
	 * Sets the character to use as a text qualifier in the data.
	 * 
	 * @param textQualifier
	 *            The character to use as a text qualifier in the data.
	 */
	public void setTextQualifier(char textQualifier) {
		userSettings.TextQualifier = textQualifier;
	}

	/**
	 * Whether text qualifiers will be used while writing data or not.
	 * 
	 * @return Whether text qualifiers will be used while writing data or not.
	 */
	public boolean getUseTextQualifier() {
		return userSettings.UseTextQualifier;
	}

	/**
	 * Sets whether text qualifiers will be used while writing data or not.
	 * 
	 * @param useTextQualifier
	 *            Whether to use a text qualifier while writing data or not.
	 */
	public void setUseTextQualifier(boolean useTextQualifier) {
		userSettings.UseTextQualifier = useTextQualifier;
	}

	public int getEscapeMode() {
		return userSettings.EscapeMode;
	}

	public void setEscapeMode(int escapeMode) {
		userSettings.EscapeMode = escapeMode;
	}

	public void setComment(char comment) {
		userSettings.Comment = comment;
	}

	public char getComment() {
		return userSettings.Comment;
	}

	/**
	 * Whether fields will be surrounded by the text qualifier even if the
	 * qualifier is not necessarily needed to escape this field.
	 * 
	 * @return Whether fields will be forced to be qualified or not.
	 */
	public boolean getForceQualifier() {
		return userSettings.ForceQualifier;
	}

	/**
	 * Use this to force all fields to be surrounded by the text qualifier even
	 * if the qualifier is not necessarily needed to escape this field. Default
	 * is false.
	 * 
	 * @param forceQualifier
	 *            Whether to force the fields to be qualified or not.
	 */
	public void setForceQualifier(boolean forceQualifier) {
		userSettings.ForceQualifier = forceQualifier;
	}

	/**
	 * Writes another column of data to this record.
	 * 
	 * @param content
	 *            The data for the new column.
	 * @param preserveSpaces
	 *            Whether to preserve leading and trailing whitespace in this
	 *            column of data.
	 * @exception IOException
	 *                Thrown if an error occurs while writing data to the
	 *                destination stream.
	 */
	public void write(String content, boolean preserveSpaces)
			throws IOException {
		checkClosed();

		checkInit();

		if (content == null) {
			content = "";
		}

		if (!firstColumn) {
			outputStream.write(userSettings.Delimiter);
		}

		boolean textQualify = userSettings.ForceQualifier;

		if (!preserveSpaces && content.length() > 0) {
			content = content.trim();
		}

		if (!textQualify
				&& userSettings.UseTextQualifier
				&& (content.indexOf(userSettings.TextQualifier) > -1
						|| content.indexOf(userSettings.Delimiter) > -1
						|| (!useCustomRecordDelimiter && (content
								.indexOf(Letters.LF) > -1 || content
								.indexOf(Letters.CR) > -1))
						|| (useCustomRecordDelimiter && content
								.indexOf(userSettings.RecordDelimiter) > -1)
						|| (firstColumn && content.length() > 0 && content
								.charAt(0) == userSettings.Comment) ||
				// check for empty first column, which if on its own line must
				// be qualified or the line will be skipped
				(firstColumn && content.length() == 0))) {
			textQualify = true;
		}

		if (userSettings.UseTextQualifier && !textQualify
				&& content.length() > 0 && preserveSpaces) {
			char firstLetter = content.charAt(0);

			if (firstLetter == Letters.SPACE || firstLetter == Letters.TAB) {
				textQualify = true;
			}

			if (!textQualify && content.length() > 1) {
				char lastLetter = content.charAt(content.length() - 1);

				if (lastLetter == Letters.SPACE || lastLetter == Letters.TAB) {
					textQualify = true;
				}
			}
		}

		if (textQualify) {
			outputStream.write(userSettings.TextQualifier);

			if (userSettings.EscapeMode == ESCAPE_MODE_BACKSLASH) {
				content = replace(content, "" + Letters.BACKSLASH, ""
						+ Letters.BACKSLASH + Letters.BACKSLASH);
				content = replace(content, "" + userSettings.TextQualifier, ""
						+ Letters.BACKSLASH + userSettings.TextQualifier);
			} else {
				content = replace(content, "" + userSettings.TextQualifier, ""
						+ userSettings.TextQualifier
						+ userSettings.TextQualifier);
			}
		} else if (userSettings.EscapeMode == ESCAPE_MODE_BACKSLASH) {
			content = replace(content, "" + Letters.BACKSLASH, ""
					+ Letters.BACKSLASH + Letters.BACKSLASH);
			content = replace(content, "" + userSettings.Delimiter, ""
					+ Letters.BACKSLASH + userSettings.Delimiter);

			if (useCustomRecordDelimiter) {
				content = replace(content, "" + userSettings.RecordDelimiter,
						"" + Letters.BACKSLASH + userSettings.RecordDelimiter);
			} else {
				content = replace(content, "" + Letters.CR, ""
						+ Letters.BACKSLASH + Letters.CR);
				content = replace(content, "" + Letters.LF, ""
						+ Letters.BACKSLASH + Letters.LF);
			}

			if (firstColumn && content.length() > 0
					&& content.charAt(0) == userSettings.Comment) {
				if (content.length() > 1) {
					content = "" + Letters.BACKSLASH + userSettings.Comment
							+ content.substring(1);
				} else {
					content = "" + Letters.BACKSLASH + userSettings.Comment;
				}
			}
		}

		outputStream.write(content.getBytes());

		if (textQualify) {
			outputStream.write(userSettings.TextQualifier);
		}

		firstColumn = false;
	}

	/**
	 * Writes another column of data to this record.&nbsp;Does not preserve
	 * leading and trailing whitespace in this column of data.
	 * 
	 * @param content
	 *            The data for the new column.
	 * @exception IOException
	 *                Thrown if an error occurs while writing data to the
	 *                destination stream.
	 */
	public void write(String content) throws IOException {
		write(content, false);
	}

	public void writeComment(String commentText) throws IOException {
		checkClosed();

		checkInit();

		outputStream.write(userSettings.Comment);

		outputStream.write(commentText.getBytes());

		if (useCustomRecordDelimiter) {
			outputStream.write(userSettings.RecordDelimiter);
		} else {
			outputStream.write("\n".getBytes());
		}

		firstColumn = true;
	}

	/**
	 * Writes a new record using the passed in array of values.
	 * 
	 * @param values
	 *            Values to be written.
	 * 
	 * @param preserveSpaces
	 *            Whether to preserver leading and trailing spaces in columns
	 *            while writing out to the record or not.
	 * 
	 * @throws IOException
	 *             Thrown if an error occurs while writing data to the
	 *             destination stream.
	 */
	public void writeRecord(String[] values, boolean preserveSpaces)
			throws IOException {
		if (values != null && values.length > 0) {
			for (int i = 0; i < values.length; i++) {
				write(values[i], preserveSpaces);
			}

			endRecord();
		}
	}

	/**
	 * Writes a new record using the passed in array of values.
	 * 
	 * @param values
	 *            Values to be written.
	 * 
	 * @throws IOException
	 *             Thrown if an error occurs while writing data to the
	 *             destination stream.
	 */
	public void writeRecord(String[] values) throws IOException {
		writeRecord(values, false);
	}

	/**
	 * Ends the current record by sending the record delimiter.
	 * 
	 * @exception IOException
	 *                Thrown if an error occurs while writing data to the
	 *                destination stream.
	 */
	public void endRecord() throws IOException {
		checkClosed();

		checkInit();

		if (useCustomRecordDelimiter) {
			outputStream.write(userSettings.RecordDelimiter);
		} else {
			outputStream.write("\n".getBytes());
		}

		firstColumn = true;
	}

	/**
	 * 
	 */
	private void checkInit() throws IOException {
		if (!initialized) {
			outputStream = new ByteArrayOutputStream();
			initialized = true;
		}
	}

	/**
	 * Clears all buffers for the current writer and causes any buffered data to
	 * be written to the underlying device.
	 * @throws IOException 
	 */
	public void flush() throws IOException {
		outputStream.flush();
	}

	/**
	 * Closes and releases all related resources.
	 */
	public void close() {
		if (!closed) {
			close(true);
			closed = true;
		}
	}

	/**
	 * 
	 */
	private void close(boolean closing) {
		if (!closed) {
			if (closing) {
				setCharset(null);
			}

			try {
				if (initialized) {
					outputStream.close();
				}
			} catch (Exception e) {
				// just eat the exception
			}
			outputStream = null;
			closed = true;
		}
	}

	/**
	 * 
	 */
	private void checkClosed() throws IOException {
		if (closed) {
			throw new IOException(
			"This instance of the CsvWriter class has already been closed.");
		}
	}

	/**
	 * 
	 */
	protected void finalize() {
		close(false);
	}

	private class Letters {
		public static final char LF = '\n';

		public static final char CR = '\r';

		public static final char QUOTE = '"';

		public static final char COMMA = ',';

		public static final char SPACE = ' ';

		public static final char TAB = '\t';

		public static final char POUND = '#';

		public static final char BACKSLASH = '\\';

		public static final char NULL = '\0';
	}

	private class UserSettings {
		// having these as publicly accessible members will prevent
		// the overhead of the method call that exists on properties
		public char TextQualifier;

		public boolean UseTextQualifier;

		public char Delimiter;

		public char RecordDelimiter;

		public char Comment;

		public int EscapeMode;

		public boolean ForceQualifier;

		public UserSettings() {
			TextQualifier = Letters.QUOTE;
			UseTextQualifier = true;
			Delimiter = Letters.COMMA;
			RecordDelimiter = Letters.NULL;
			Comment = Letters.POUND;
			EscapeMode = ESCAPE_MODE_DOUBLED;
			ForceQualifier = false;
		}
	}

	public static String replace(String original, String pattern, String replace) {
		final int len = pattern.length();
		int found = original.indexOf(pattern);

		if (found > -1) {
			StringBuffer sb = new StringBuffer();
			int start = 0;

			while (found != -1) {
				sb.append(original.substring(start, found));
				sb.append(replace);
				start = found + len;
				found = original.indexOf(pattern, start);
			}

			sb.append(original.substring(start));

			return sb.toString();
		} else {
			return original;
		}
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public Charset getCharset() {
		return charset;
	}
	
	public ByteArrayOutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(ByteArrayOutputStream outputStream) {
		this.outputStream = outputStream;
	}
}