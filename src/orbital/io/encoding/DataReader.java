/**
 * @(#)DataReader.java 0.9 1999/11/03 Andre Platzer
 * 
 * Copyright (c) 1999 Andre Platzer. All Rights Reserved.
 */

package orbital.io.encoding;

import java.io.DataInput;
import orbital.io.AnyInputStream;
import java.io.Reader;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.io.IOException;
import java.io.EOFException;
import java.io.UnsupportedEncodingException;

import orbital.io.ParseException;
import orbital.io.IOUtilities;
import orbital.math.MathUtilities;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * The <code>DataReader</code> class is an implementation of the {@link java.io.DataInput} interface
 * that provides for reading formatted data from a character stream and
 * reconstructing from them data in any of
 * the Java primitive types. Thus it provides parsing human-understandable input like
 * <tt>Hello World</tt>, <tt>12345</tt> or <tt>true</tt>.
 * <p>
 * It is generally true of all the reading
 * routines in this interface that if end of
 * file is reached before the desired number
 * of bytes has been read, an <code>EOFException</code>
 * (which is a kind of <code>IOException</code>)
 * is thrown. If any byte cannot be read for
 * any reason other than end of file, an <code>IOException</code>
 * other than <code>EOFException</code> is
 * thrown. In particular, an <code>IOException</code>
 * may be thrown if the input stream has been
 * closed.</p>
 * <p>
 * All methods will throw a {@link orbital.io.ParseException} when the
 * input could be read but not parsed in the desired form.</p>
 * <p>
 * With the following idiom, you will get a keyboard reader for the console standard input
 * <pre>
 * <span class="Orbital">DataReader</span> in <span class="operator">=</span> <span class="keyword">new</span> <span class="Orbital">DataReader</span>(<span class="Class">System</span>.in);
 * </pre>
 * </p>
 * 
 * @version 0.9, 1999/11/03
 * @author  Andr&eacute; Platzer
 * @see java.io.DataInput
 * @see DataReader
 * @see DecodingReader
 * @see java.io.FilterReader
 * @see java.io.FilterInputStream
 * @todo replace IOUtilities.whitespaces with Character.isWhitespace(char)
 * @xxx throwing ParseException isn't really allowed. Instead it has been wrapped in an IOException which is not yet documented, correctly.
 */
public	  //TODO: is DecodingReader desired? Remember that InputStreamReader does odd buffering
class DataReader extends /* DecodingReader */ AnyInputStream implements DataInput {
	private static class Debug {
		private Debug() {}
		public static void main(String[] arg) throws Exception {
			DataReader d = getInstance(System.in, "strict");
			System.out.print("Type an int: ");
			System.out.flush();
			int i = d.readInt();
			System.out.println("int was: '" + i + "'");
			System.out.print("Type a short: ");
			System.out.flush();
			short s = d.readShort();
			System.out.println("short was: '" + s + "'");
			System.out.print("Type a boolean: ");
			System.out.flush();
			boolean b = d.readBoolean();
			System.out.println("boolean was: '" + b + "'");
			System.out.print("Type a line: ");
			System.out.flush();
			String l = d.readLine();
			System.out.println("line was: '" + l + "'");
			System.out.print("Type a char: ");
			System.out.flush();
			char c = d.readChar();
			System.out.println("char was: '" + c + "'");
			System.out.print("Type a second line: ");
			System.out.flush();
			l = d.readLine();
			System.out.println("line was: '" + l + "'");
			System.out.println("Anything more to say?");
			while (d.ready()) {
				Object o = d.readObject();
				System.out.println(o.getClass() + ":" + o);
			} 
		} 
	}	 // Debug

    static final Logger logger = Logger.getLogger(DataReader.class.getName());

	public static void main(String arg[]) throws Exception {
		if (arg.length == 0 || "-?".equals(arg[0])) {
			System.out.println(usage);
			return;
		} 
		DataReader d = getInstance(new java.io.FileReader(arg.length > 1 ? arg[1] : "t"), arg.length > 0 ? arg[0] : "default");
		if (d == null) {
			System.out.println("no decoding is necessary");
			return;
		} 
		while (d.ready()) {
			Object o = d.readObject();
			if (o == null) break;
			System.out.println(o.getClass() + ":" + o);
		} 
		d.close();
	} 
	public static final String usage = "usage: " + DataReader.class + " [encoding] [file]" + System.getProperty("line.separator") + "Where encoding is one of: default, none, strict, basic";


	//XXX: should be Integer.MAX_VALUE which is a problematic buffer size, unfortunately
	static final int MARK_LIMIT = 1000;

	/**
	 * Generates a DataReader object for the specified type of encoding.
	 * <p>
	 * Supported encodings are:<table border="1" style="width:80%">
     *   <caption>Encoding types for DataReaders / DataWriters</caption>
     *   <tr>
     *     <th>encoding</th>
     *     <th>effect</th>
     *   </tr>
     *   <tr>
     *     <td>default</td>
     *     <td>convenient human readable</td>
     *   </tr>
     *   <tr>
     *     <td>strict</td>
     *     <td>human readable implemented with strict use of tokenizers (totally safe
     *       and error-prone, but less performant)</td>
     *   </tr>
     *   <tr>
     *     <td>basic</td>
     *     <td>QuickBasic and VisualBasic data file encoding</td>
     *   </tr>
     * </table>
	 * <p>
	 * Call
	 * <kbd class="command">java orbital.io.encoding.DataReader -?</kbd>
	 * to get an up to date list of the supported encodings on a system.
	 * </p>
	 * @param rd from which reader to encode.
	 * @param encoding which decoding reader to instantiate with <code>rd</code>.
	 * @return an instance fitting the encoding
	 * or <code>null</code> if no decoding is necessary. (for efficiency reasons).
	 * @throws UnsupportedEncodingException if no reader is found for the given encoding.
	 * @throws IOException if the instantiation throws an IOException.
	 * @see <a href="{@docRoot}/DesignPatterns/FacadeFactory.html">&quot;FacadeFactory&quot;</a>
	 */
	public static DataReader getInstance(Reader rd, String encoding) throws UnsupportedEncodingException, IOException {
		if (encoding == null)
			return null;
		if ("".equals(encoding) || "default".equalsIgnoreCase(encoding) || "none".equalsIgnoreCase(encoding))
			return new StrictDataReader(rd);	//XXX: as long as DataReader has errors
		if ("basic".equalsIgnoreCase(encoding))
			return new BasicDataReader(rd);
		if ("strict".equalsIgnoreCase(encoding))
			return new StrictDataReader(rd);
		throw new UnsupportedEncodingException("no reader found for encoding '" + encoding + "'");
	} 

	/**
	 * Contained for compatibilitiy reasons only.
	 * <p>
	 * The preferred way to read character data from an input stream is to convert it into a character stream (Reader).
	 * Use <code>DataReader.getInstance(new InputStreamReader(is), encoding)</code> instead.
	 * Although this method is useful for direct input or reading from {@link System#in}.</p>
	 * @xxx deprecated The preferred way to read character data from an input stream is to convert it into a character stream (Reader).
	 * Use <code>DataReader.getInstance(new InputStreamReader(is), encoding)</code> instead.
	 * Although this method is useful for direct input or reading from {@link System#in}.
	 * @param rd from which reader to encode.
	 * @param encoding which decoding reader to instantiate with <code>rd</code>.
	 * @return an instance fitting the encoding
	 * or <code>null</code> if no decoding is necessary. (for efficiency reasons).
	 * @throws UnsupportedEncodingException if no reader is found for the given encoding.
	 * @throws IOException if the instantiation throws an IOException.
	 * @see #getInstance(Reader, String)
	 * @see <a href="{@docRoot}/DesignPatterns/FacadeFactory.html">&quot;FacadeFactory&quot;</a>
	 */
	public static DataReader getInstance(InputStream is, String encoding) throws UnsupportedEncodingException, IOException {
		if (encoding == null)
			return null;
		if ("".equals(encoding) || "default".equalsIgnoreCase(encoding) || "none".equalsIgnoreCase(encoding))
			return new DataReader(is);
		if ("basic".equalsIgnoreCase(encoding))
			return new BasicDataReader(is);
		if ("strict".equalsIgnoreCase(encoding))
			return new StrictDataReader(is);
		throw new UnsupportedEncodingException("no reader found for encoding '" + encoding + "'");
	} 


	/**
	 * The StreamTokenizer that parses the data from the Reader or InputStream.
	 * Remember updating underlying input stream.
	 */
	protected StreamTokenizer parse;

	/**
	 * Whether usual whitespaces should be skipped for character reading, too.
	 */

	/* private */
	boolean					  skipWhitespaces = true;

	/**
	 * Use and initialize the stream tokenizer specified to parse input.
	 * @param returnWhitespaces whether the stream tokenizer should return whitespaces as well
	 */
	protected void initialize(StreamTokenizer parse, boolean returnWhitespaces) {
		this.parse = parse;
		parse.resetSyntax();
		parse.wordChars('a', 'z');
		parse.wordChars('A', 'Z');
		parse.wordChars(128 + 32, 255);
		if (returnWhitespaces)
			parse.ordinaryChars(0, ' ');
		else
			parse.whitespaceChars(0, ' ');

		// parse.commentChar('/');
		parse.quoteChar('"');
		parse.quoteChar('\'');
		parse.parseNumbers();
		parse.eolIsSignificant(returnWhitespaces);
		if (returnWhitespaces ||!skipWhitespaces)
			for (int i = 0; i < IOUtilities.whitespaces.length(); i++)
				parse.ordinaryChar(IOUtilities.whitespaces.charAt(i));
	} 

	/**
	 * Create a DataReader parsing input from the specified character stream.
	 */
	public DataReader(Reader input) {
		super(input, true);
		initialize(new StreamTokenizer(underlying_reader), false);
	}

	/**
	 * Contained for compatibilitiy reasons only.
	 * The preferred way to read character data from an input stream is to convert it into a character stream (Reader).
	 * Use <code>DataReader(new InputStreamReader(is))</code> instead.
	 * Although this method is useful for direct input or reading from System.in.
	 * @see #DataReader(java.io.Reader)
	 */
	public DataReader(InputStream input) {
		super(input, true);
		initialize(new StreamTokenizer(underlying_inputstream), false);
	}

	/**
	 * Returns the corresponding encoding format.
	 * @return a String that specifies the format that is supported by this reader.
	 */
	public String getFormat() {
		return "default";
	} 

	// Controlling the StreamTokenizer parse.

	/**
	 * Returns a reference to the underlying StreamTokenizer
	 * that can then be configured.
	 * @security what happens with illegal changes to the object returned?
	 */
	public StreamTokenizer getStreamTokenizer() {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkPermission(new RuntimePermission("accessDeclaredMembers"));
		} 
		return parse;
	} 

	/**
	 * Get the current line number.
	 * @return the current line number.
	 */
	public int getLineNumber() {
		return parse.lineno();
	} 


	/**
	 * not supported.
	 * @throws java.lang.UnsupportedOperationException on every call.
	 */
	public void readFully(byte b[]) throws IOException {
		throw new UnsupportedOperationException();
	} 

	/**
	 * not supported.
	 * @throws java.lang.UnsupportedOperationException on every call.
	 */
	public void readFully(byte b[], int off, int len) throws IOException {
		throw new UnsupportedOperationException();
	} 

	public int skipBytes(int n) throws IOException {
		long s = skip(n);
		if (s < Integer.MIN_VALUE || Integer.MAX_VALUE < s)
			throw new IOException("skip was too big to fit an int");
		return (int) s;
	} 


	// basic methods.

	/**
	 * Reads an integer representation and returns an
	 * <code>int</code> value.
	 * Basic method.
	 * 
	 * @return     the <code>int</code> value read.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * all the bytes.
	 * @throws  IOException   if an I/O error occurs.
	 * @throws ParseException if the stream does not contain a parsable int.
	 */
	public int readInt() throws IOException {
		if (nextToken() != StreamTokenizer.TT_NUMBER)
			throw newParseException(parse, "integer number");
		long v = (long) parse.nval;
		if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE)
			throw newParseException(parse, "int sized integer number");
		return (int) v;
	} 

	/**
	 * Reads and returns
	 * a <code>long</code> value.
	 * Basic method.
	 * 
	 * @return     the <code>long</code> value read.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * all the bytes.
	 * @throws  IOException   if an I/O error occurs.
	 * @throws ParseException if the stream does not contain a parsable long.
	 */
	public long readLong() throws IOException {
		if (nextToken() != StreamTokenizer.TT_NUMBER)
			throw newParseException(parse, "integer number");
		double v = parse.nval;
		if (v < Long.MIN_VALUE || v > Long.MAX_VALUE)
			throw newParseException(parse, "long sized integer number");
		return (long) v;
	} 

	/**
	 * Reads and returns
	 * a <code>double</code> value.
	 * Basic method.
	 * 
	 * @return     the <code>double</code> value read.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * all the bytes.
	 * @throws  IOException   if an I/O error occurs.
	 * @throws ParseException if the stream does not contain a parsable double.
	 */
	public double readDouble() throws IOException {
		if (nextToken() != StreamTokenizer.TT_NUMBER)
			throw newParseException(parse, "real number");
		return parse.nval;
	} 

	/**
	 * Reads and returns a boolean value.
	 * A word <tt>true</tt> will be interpreted as <code>true</code> with ignore-case.
	 * A word <tt>false</tt> will be interpreted as <code>false</code> with ignore-case.
	 * A number <tt>0</tt> will be interpreted as <code>false</code>, any other number as <code>true</code>.
	 * 
	 * @return     the <code>boolean</code> value read.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * all the bytes.
	 * @throws  IOException   if an I/O error occurs.
	 * @throws ParseException if this stream does not contain a parsable boolean.
	 */
	public boolean readBoolean() throws IOException {
		boolean v;
		switch (nextToken()) {
			case StreamTokenizer.TT_WORD:
				if ("true".equalsIgnoreCase(parse.sval))
					v = true;
				else if ("false".equalsIgnoreCase(parse.sval))
					v = false;
				else
					throw newParseException(parse, "boolean value");
				break;
			case StreamTokenizer.TT_NUMBER:
				v = (0 != parse.nval);
				break;
			default:
				throw newParseException(parse, "boolean value");
		}
		return v;
	} 

	/**
	 * Reads the next word of text from the input stream.
	 * Will convert any token into a String.
	 * 
	 * @return     if this stream reaches the end before reading all the bytes.
	 * @throws  IOException  if an I/O error occurs.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * any character.
	 */
	public String readWord() throws IOException {
		switch (nextToken()) {
			case StreamTokenizer.TT_EOF:
				throw new EOFException("EOF during readWord");
			case StreamTokenizer.TT_EOL:
				return System.getProperty("line.separator");
			case '"':
			case StreamTokenizer.TT_WORD:
				return parse.sval;
			case StreamTokenizer.TT_NUMBER:
				return parse.nval + "";
			default:
				return (char) parse.ttype + "";
		}
	} 

	/**
	 * Reads a String in a well defined way.
	 * <p>
	 * UTF-encoding is not defined for character streams.
	 * A DataReader class can decide to read in a String in another well defined format
	 * that does not require additional formatting conventions by the user.</p>
	 * <p>
	 * Will read a quoted string <code>"<var>any characters</var>"</code>.</p>
	 * 
	 * @param      str   the string value to be written.
	 * @throws  IOException  if an I/O error occurs.
	 * @throws  UnsupportedOperationException if no such encoding is defined for this character stream.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * any character.
	 */
	public String readUTF() throws IOException {
		if (nextToken() != '"')
			throw newParseException(parse, "quoted string");
		return parse.sval;
	} 

	/**
	 * Reads the next line of text from the input stream.
	 * 
	 * @return     the line of text read, or <code>null</code> if this tream reaches the end before reading any character.
	 * @throws  IOException  if an I/O error occurs.
	 */
	public String readLine() throws IOException {
		if (underlying_reader != null)
			return IOUtilities.readLine(underlying_reader);
		if (underlying_inputstream != null)
			return IOUtilities.readLine(underlying_inputstream);
		throw new IllegalStateException("neither reader nor input stream is set");
	} 

	/**
	 * Reads a single input <code>char</code> and returns the <code>char</code> value.
	 * 
	 * @return     <code>(char)parse.read()</code>, the <code>char</code> read.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * all the bytes.
	 * @throws  IOException   if an I/O error occurs.
	 */
	public char readChar() throws IOException {
		if (!skipWhitespaces)
			return (char) read();
		int v;
		do {
			v = read();
			if (v == -1)
				throw new EOFException("EOF during character readChar");
		} while (IOUtilities.whitespaces.indexOf(v) >= 0);
		accepted();
		return (char) v;
	} 



	// dependent derived methods. implemented in terms of those above.

	/**
	 * Reads and returns one input byte.
	 * The byte is treated as a signed value in
	 * the range <code>-128</code> through <code>127</code>,
	 * inclusive.
	 * 
	 * @return     the 8-bit value read.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * all the bytes.
	 * @throws  IOException   if an I/O error occurs.
	 * @throws ParseException if the stream does not contain a parsable byte.
	 * @see #readInt()
	 */
	public byte readByte() throws IOException {
		int v = readInt();
		if (v < Byte.MIN_VALUE || v > Byte.MAX_VALUE)
			throw newParseException(parse, "byte sized integer number");
		return (byte) v;
	} 

	/**
	 * Reads one input byte, zero-extends
	 * it to type <code>int</code>, and returns
	 * the result, which is therefore in the range
	 * <code>0</code>
	 * through <code>255</code>.
	 * 
	 * @return     the unsigned 8-bit value read.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * all the bytes.
	 * @throws  IOException   if an I/O error occurs.
	 * @throws ParseException if the stream does not contain a parsable unsigned byte.
	 * @see #readInt()
	 */
	public int readUnsignedByte() throws IOException {
		int v = readInt();
		if (v < 0 || v > Byte.MAX_VALUE - Byte.MIN_VALUE)
			throw newParseException(parse, "unsigned byte sized integer number");
		return v;
	} 

	/**
	 * Reads a <code>short</code> value.
	 * 
	 * @return     the 16-bit value read.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * all the bytes.
	 * @throws  IOException   if an I/O error occurs.
	 * @throws ParseException if the stream does not contain a parsable short.
	 * @see #readInt()
	 */
	public short readShort() throws IOException {
		int v = readInt();
		if (v < Short.MIN_VALUE || v > Short.MAX_VALUE)
			throw newParseException(parse, "short sized integer number");
		return (short) v;
	} 

	/**
	 * Reads an unsigned short and returns
	 * an <code>int</code> value in the range <code>0</code>
	 * through <code>65535</code>.
	 * 
	 * @return     the unsigned 16-bit value read.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * all the bytes.
	 * @throws  IOException   if an I/O error occurs.
	 * @throws ParseException if the stream does not contain a parsable unsigned short.
	 * @see #readInt()
	 */
	public int readUnsignedShort() throws IOException {
		int v = readInt();
		if (v < 0 || v > Short.MAX_VALUE - Short.MIN_VALUE)
			throw newParseException(parse, "unsigned short sized integer number");
		return v;
	} 

	/**
	 * Reads and returns
	 * a <code>float</code> value.
	 * 
	 * @return     the <code>float</code> value read.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * all the bytes.
	 * @throws  IOException   if an I/O error occurs.
	 * @throws ParseException if the stream does not contain a parsable float.
	 * @see #readDouble()
	 */
	public float readFloat() throws IOException {
		double v = readDouble();
		if (v < Float.MIN_VALUE || v > Float.MAX_VALUE)
			throw newParseException(parse, "float sized real number");
		return (float) v;
	} 

	/**
	 * Read and return an object.
	 * @return the object read or null if there was nothing to read.
	 * @throws InvalidClassException	if the class of a serialized object is invalid.
	 * @throws IOException	if any of the usual Input/Output related exceptions occur.
	 * @throws ParseException if the stream does not contain a parsable object.
	 */
	public Object readObject() throws IOException {
		Class c = nextType();
		if (c == null)
			return null;
		if (c.equals(Void.TYPE))
			return null;

		if (c.equals(Byte.TYPE) || c.equals(Byte.class))
			return new Byte(readByte());
		if (c.equals(Short.TYPE) || c.equals(Short.class))
			return new Short(readShort());
		if (c.equals(Integer.TYPE) || c.equals(Integer.class))
			return new Integer(readInt());
		if (c.equals(Long.TYPE) || c.equals(Long.class))
			return new Long(readLong());
		if (c.equals(Float.TYPE) || c.equals(Float.class))
			return new Float(readFloat());
		if (c.equals(Double.TYPE) || c.equals(Double.class))
			return new Double(readDouble());
		if (c.equals(Number.class))
			return new Double(readDouble());

		if (c.equals(Boolean.TYPE) || c.equals(Boolean.class))
			return new Boolean(readBoolean());
		if (c.equals(Character.TYPE) || c.equals(Character.class))
			return new Character(readChar());
		if (c.equals(String.class))
			return parse.ttype == '"' ? readUTF() : readLine();	   // XXX: word?
		throw newParseException(parse, "could not handle type of object: ");
	} 


	// input implementation

	/**
	 * Predicts the type of the next input parsed.
	 * @return class-object describing the type of the next input that can be parsed.
	 * Will either describe a primitive type or String.
	 * @throws  IOException   if an I/O error occurs.
	 * @see #peekNextToken()
	 */
	public Class nextType() throws IOException {
		switch (peekNextToken()) {
			case StreamTokenizer.TT_EOF:
				return null;
			case StreamTokenizer.TT_EOL:
				return Character.TYPE;
			case StreamTokenizer.TT_NUMBER:
				if (MathUtilities.fract(parse.nval) == .0)
					return (Integer.MIN_VALUE < parse.nval && parse.nval < Integer.MAX_VALUE) ? Integer.TYPE : Long.TYPE;
				else
					return Double.TYPE;
			case StreamTokenizer.TT_WORD:
				if ("true".equalsIgnoreCase(parse.sval) || "false".equalsIgnoreCase(parse.sval))
					return Boolean.TYPE;
				else
					return String.class;	// XXX: Line / Word
			case '"':
				return String.class;		// XXX: UTF
			default:
				return Character.TYPE;
		}
	} 

	/**
	 * Peeks for the nextToken parsed without consuming it.
	 * Overwrite to change style.
	 * <p>
	 * Skips any EOLs occurring.</p>
	 * @return nextToken of StreamTokenizer <code>parse</code>, or StreamTokenizer.TT_EOF.
	 * @throws java.io.IOException if another input/output exception occurs.
	 * @see #mark(int)
	 * @see java.io.StreamTokenizer#nextToken()
	 * @see #reset()
	 */
	// FIXME: buggy for    -1|5,  will return -1, |, -1, | as tokens
	protected int peekNextToken() throws IOException {
		if (!markSupported())
			throw new IllegalStateException("underlying stream does not support marking");
		mark(MARK_LIMIT);
		try {
			do {
				if (parse.nextToken() == StreamTokenizer.TT_EOF)
					break;
			} while (parse.ttype == StreamTokenizer.TT_EOL || (skipWhitespaces && IOUtilities.whitespaces.indexOf(parse.ttype) >= 0));
			return parse.ttype;
		} 
		finally {
			reset();
			// clear internal storage for next token
			// initialize(new StreamTokenizer(underlying_reader));			//FIXME: does this work?
		} 
	} 

	/**
	 * Returns the nextToken in a parsed style.
	 * Called by all reading methods that need content in a parsed style.
	 * Overwrite to change style.
	 * <p>
	 * Skips any EOLs occurring.</p>
	 * @return nextToken of StreamTokenizer <code>parse</code>.
	 * @throws java.io.EOFException if <code>nextToken()==StreamTokenizer.TT_EOF</code>.
	 * @throws java.io.IOException if another input/output exception occurs.
	 * @see java.io.StreamTokenizer#nextToken()
	 */
	protected int nextToken() throws IOException, EOFException {
		do {
			if (parse.nextToken() == StreamTokenizer.TT_EOF)
				throw new EOFException("EOF during read");
		} while (parse.ttype == StreamTokenizer.TT_EOL || (skipWhitespaces && IOUtilities.whitespaces.indexOf(parse.ttype) >= 0));
		accepted();
		return parse.ttype;
	} 

	/**
	 * Processes any cleanup after data has been read in a parsed style.
	 * Called by all reading methods that need content in a parsed style after having matched.
	 * Overwrite to change style.
	 * <p>
	 * Used to clean EOL character after parsed input from the user. Skips an EOL occurring.</p>
	 * @see #nextToken()
	 */
	private void accepted() throws IOException {
		while (ready()) {	 // skip all whitespace chars
			if (!markSupported())
				throw new IllegalStateException("underlying stream does not support marking");
			mark(4);
			if (IOUtilities.whitespaces.indexOf(read()) < 0) {
				reset();
				break;
			} 
		} 
	} 

	public String toString() {
		return parse.toString();
	} 

	IOException newParseException(StreamTokenizer parse, String message) {
		return new IOException(new ParseException(parse, message).toString());
	}
}


/**
 * Alternative implementation of DataReader with strict use of the StreamTokenizer
 * ignoring underlying streams.
 */
class StrictDataReader extends DataReader {

	/**
	 * Create a DataReader parsing input from the specified character stream.
	 */
	public StrictDataReader(Reader input) {
		super(input);
	}

	/**
	 * Contained for compatibilitiy reasons only.
	 * The preferred way to read character data from an input stream is to convert it into a character stream (Reader).
	 * Use <code>DataReader(new InputStreamReader(is))</code> instead.
	 * Although this method is useful for direct input or reading from System.in.
	 * @see #StrictDataReader(java.io.Reader)
	 */
	public StrictDataReader(InputStream input) {
		super(input);
	}

	private boolean wouldAcceptEOL = false;

	/**
	 * Returns the corresponding encoding format.
	 * @return a String that specifies the format that is supported by this reader.
	 */
	public String getFormat() {
		return "strict";
	} 


	/**
	 * Reads the next line of text from the input stream.
	 * 
	 * @return     the line read.
	 * @throws  IOException  if an I/O error occurs.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * any character.
	 */
	public String readLine() throws IOException {
		// initialize(getStreamTokenizer(), true);
		parse.resetSyntax();
		parse.eolIsSignificant(true);
		try {
    		String nl = System.getProperty("line.separator");
    		String line = "";
    readLineLoop:
    		while (true) {
    			int typ = parse.nextToken();
    			switch (typ) {
    				case StreamTokenizer.TT_EOF:
    					if ("".equals(line))
    						line = null;
    					break readLineLoop;
    				case StreamTokenizer.TT_EOL:
        				if (wouldAcceptEOL) {	 // might still be last EOL
        					wouldAcceptEOL = false;
        
        					// did we only read whitespaces?
        					for (int i = 0; i < line.length(); i++)
        						if (IOUtilities.whitespaces.indexOf(line.charAt(i)) < 0)
        							break readLineLoop;
        
        							// then restart at next line
        					line = "";
        					continue readLineLoop;
        				} else
        					break readLineLoop;
    			}
    
    			// EOL recognition has been done above, so
    			// skip useless EOL elongation that we are not interested in
    			if (typ == '\r' || typ == '\n')
    				continue;
    			line += (char) typ;
    		} 
			return line;
		}
		finally {
			initialize(getStreamTokenizer(), false);
		}

		/*
		 * Alternative implementation
		 * // implementation using StreamTokenizer, only
		 * parse.nextToken();
		 * int line = parse.lineno();
		 * parse.pushBack();
		 * logger.log(Level.FINEST,"readLine()","start in line "+line);
		 * StringBuffer sb = new StringBuffer();
		 * reading: do {
		 * parse.nextToken();
		 * logger.log(Level.FINEST,"readLine()","in line "+parse.lineno());
		 * if (parse.lineno()>line) {
		 * parse.pushBack();
		 * break reading;
		 * }
		 * switch(parse.ttype) {
		 * case StreamTokenizer.TT_EOF:
		 * if (sb.length()==0)
		 * throw new EOFException("EOF during read");
		 * break reading;
		 * case StreamTokenizer.TT_EOL:
		 * break reading;
		 * case StreamTokenizer.TT_NUMBER:
		 * if (sb.length()>0)
		 * sb.append(' ');
		 * sb.append(parse.nval);
		 * break;
		 * case StreamTokenizer.TT_WORD:
		 * if (sb.length()>0)
		 * sb.append(' ');
		 * sb.append(parse.sval);
		 * break;
		 * default:
		 * sb.append((char)parse.ttype);
		 * }
		 * logger.log(Level.FINEST,"readLine()","\t..>'"+sb.toString()+"'");
		 * logger.log(Level.FINEST,"readLine()","and line "+parse.lineno());
		 * if (parse.lineno()>line) {
		 * parse.pushBack();
		 * break reading;
		 * }
		 * } while(parse.ttype!=StreamTokenizer.TT_EOL && parse.ttype!=StreamTokenizer.TT_EOF);
		 * return sb.toString();
		 */
	} 

	/**
	 * Reads a single input <code>char</code> and returns the <code>char</code> value.
	 * 
	 * @return     <code>(char)parse.read()</code>, the <code>char</code> read.
	 * @throws  EOFException  if this stream reaches the end before reading
	 * all the bytes.
	 * @throws  IOException   if an I/O error occurs.
	 */
	public char readChar() throws IOException {

		// implementation using StreamTokenizer, only
		switch (nextToken()) {
			case StreamTokenizer.TT_EOF:
				throw new EOFException("EOF during readChar");
			case StreamTokenizer.TT_EOL:
				return '\n';
			case StreamTokenizer.TT_WORD: {
				char ch = parse.sval.charAt(0);
				if (parse.sval.length() > 1) {	  // XXX: debug. better check whether it works. what's up with "xy7"?
					logger.log(Level.FINEST, "readChar()", "pushback: '" + parse.sval.substring(1) + "'");
					parse.sval = parse.sval.substring(1);
					parse.pushBack();
				} 
				return ch;
			} 
			case StreamTokenizer.TT_NUMBER: {
				String s = parse.nval + "";
				char   ch = s.charAt(0);
				if (s.length() > 1) {	 // XXX: debug. better check whether it works. what's up with "12347.01"?
					logger.log(Level.FINEST, "readChar()", "pushback: '" + s.substring(1) + "'");
					parse.nval = Double.parseDouble(s.substring(1));
					parse.pushBack();
				} 
				return ch;
			} 
			default:
				return (char) parse.ttype;
		}

		/*
		 Alternative implementation:
		 // implementation using StreamTokenizer, only
		 switch(nextToken()) {
		 case StreamTokenizer.TT_EOF:
		 throw new EOFException("no more tokens");
		 case StreamTokenizer.TT_WORD: {
		 char ch = parse.sval.charAt(0);
		 if (parse.sval.length()>1) {	//XXX debug. better check whether it works. what's up with "xy7"?
		 logger.log(Level.FINEST,"readChar()","pushback: '"+parse.sval.substring(1)+"'");
		 parse.sval = parse.sval.substring(1);
		 parse.pushBack();
		 }
		 return ch;
		 }
		 case StreamTokenizer.TT_NUMBER:
		 //TODO
		 throw new UnsupportedOperationException("not yet implemented for number case");
		 default:
		 return (char)parse.ttype;
		 }
		 */
	} 



	// dependent derived methods. implemented in terms of those above.

	/**
	 * Peeks for the nextToken parsed without consuming it.
	 * Overwrite to change style.
	 * <p>
	 * Skips any EOLs occurring.</p>
	 * @return nextToken of StreamTokenizer <code>parse</code>, or StreamTokenizer.TT_EOF.
	 * @throws java.io.IOException if another input/output exception occurs.
	 * @see #mark(int)
	 * @see java.io.StreamTokenizer#nextToken()
	 * @see #reset()
	 */

	// XXX: debug for    -1|5,  will return -1, |, -1, | as tokens
	protected int peekNextToken() throws IOException {
		do {
			if (parse.nextToken() == StreamTokenizer.TT_EOF)
				break;
		} while (parse.ttype == StreamTokenizer.TT_EOL || (skipWhitespaces && IOUtilities.whitespaces.indexOf(parse.ttype) >= 0));
		parse.pushBack();

		// System.err.println("peekNextToken(): ("+parse.ttype+") == "+parse);
		return parse.ttype;

		/*
		 * // unfortunately this does not conform with readChar, since that will forget StreamTokenizers internal buffer
		 * do {
		 * if (parse.nextToken()==StreamTokenizer.TT_EOF)
		 * break;
		 * } while(parse.ttype==StreamTokenizer.TT_EOL || (skipWhitespaces && IOUtilities.whitespaces.indexOf(parse.ttype)>=0));
		 * parse.pushBack();
		 * System.err.println("peekNextToken(): ("+parse.ttype+") == "+parse);
		 * return parse.ttype;
		 */

		// Alternative implementation:
		// mark, parse and reset
		// will solely forget streamtokenizers internal single character cache
	} 

	/**
	 * Returns the nextToken in a parsed style.
	 * Called by all reading methods that need content in a parsed style.
	 * Overwrite to change style.
	 * <p>
	 * Skips any EOLs occurring.</p>
	 * @return nextToken of StreamTokenizer <code>parse</code>.
	 * @throws java.io.EOFException if <code>nextToken()==StreamTokenizer.TT_EOF</code>.
	 * @throws java.io.IOException if another input/output exception occurs.
	 * @see java.io.StreamTokenizer#nextToken()
	 */
	protected int nextToken() throws IOException, EOFException {
		do {
			if (parse.nextToken() == StreamTokenizer.TT_EOF)
				throw new EOFException("EOF during read");
		} while (parse.ttype == StreamTokenizer.TT_EOL || (skipWhitespaces && IOUtilities.whitespaces.indexOf(parse.ttype) >= 0));
		accepted();
		return parse.ttype;
	} 

	/**
	 * Processes any cleanup after data has been read in a parsed style.
	 * Called by all reading methods that need content in a parsed style after having matched.
	 * Overwrite to change style.
	 * <p>
	 * Used to clean EOL character after parsed input from the user. Skips an EOL occurring.</p>
	 * @see #nextToken()
	 */
	private void accepted() throws IOException {
		wouldAcceptEOL = true;

		// TODO: implement with the streamtokenizer

		/*
		 eolIsSignificant(true)
		 whitespaceChars(none)
		 while EOL or whitespace
		 parse.nextToken()
		 parse.pushBack();
		 */

		// Alternative implementation
		// stream tokenizer is made eolSignificant(false) so this logic transfers to lineno() in readLine()
		// -> lineno() does not work
		// implementation using StreamTokenizer, only

		/*
		 if (!ready())
		 return;
		 logger.log(Level.FINEST,"accepted()","pending");
		 if (parse.nextToken()==StreamTokenizer.TT_EOF)
		 return;
		 if (parse.ttype!=StreamTokenizer.TT_EOL)                           // consume eols, but
		 parse.pushBack();												// pushback if input continues
		 logger.log(Level.FINEST,"accepted()","is: "+parse);
		 */
	} 
}

/**
 * Basic format is a comma or newline separated list of values
 * where each value is either a number, a boolean value (<tt>#true</tt> or <tt>#false</tt>) or a string
 * in quotes (like <tt>"a string value"</tt>).
 */
class BasicDataReader extends DataReader {
	public BasicDataReader(Reader input) {
		super(input);
		parse.eolIsSignificant(false);
		parse.whitespaceChars(',', ',');
	}

	/**
	 * Contained for compatibilitiy reasons only.
	 * @deprecated The preferred way to read character data from an input stream is to convert it into a character stream (Reader).
	 * Use <code>BasicDataReader(new InputStreamReader(is))</code> instead.
	 * @see #BasicDataReader(java.io.Reader)
	 */
	public BasicDataReader(InputStream input) {
		super(input);
		parse.eolIsSignificant(false);
		parse.whitespaceChars(',', ',');
	}

	public String getFormat() {
		return "basic";
	} 

	protected int nextToken() throws IOException, EOFException {
		if (parse.nextToken() == StreamTokenizer.TT_EOF)
			throw new EOFException("EOF during read");
		return parse.ttype;
	} 

	protected int peekNextToken() throws IOException, EOFException {
		if (!markSupported())
			throw new IllegalStateException("underlying stream does not support marking");
		mark(MARK_LIMIT);
		try {
			return parse.nextToken();
		} 
		finally {
			reset();
		} 
	} 

	public Class nextType() throws IOException {
		switch (peekNextToken()) {
			case StreamTokenizer.TT_EOF:
				return null;
			case StreamTokenizer.TT_NUMBER:
				if (MathUtilities.fract(parse.nval) == .0)
					return (Integer.MIN_VALUE < parse.nval && parse.nval < Integer.MAX_VALUE) ? Integer.TYPE : Long.TYPE;
				else
					return Double.TYPE;
			case '#':
				return Boolean.TYPE;
			case '"':
				return String.class;
			default:
				return Character.TYPE;
		}
	} 

	public boolean readBoolean() throws IOException {
		boolean v;
		switch (nextToken()) {
			case '#':
				if (nextToken() != StreamTokenizer.TT_WORD)
					throw newParseException(parse, "boolean value");
				if ("true".equalsIgnoreCase(parse.sval))
					v = true;
				else if ("false".equalsIgnoreCase(parse.sval))
					v = false;
				else
					throw newParseException(parse, "boolean value");
				if (nextToken() != '#')
					throw newParseException(parse, "boolean value");
				break;
			case StreamTokenizer.TT_NUMBER:
				v = (0 != parse.nval);
				break;
			default:
				throw newParseException(parse, "boolean value");
		}
		return v;
	} 

	public char readChar() throws IOException {
		String s = readUTF();
		if (s.length() != 1)
			throw newParseException(parse, "single character string");
		return s.charAt(0);
	} 

	/**
	 * Reads in a basic formatted String.
	 * format should be: <tt>"<var>any characters</var>"</tt> where a <tt>"</tt>
	 * in the string characters is encoded as <tt>\"</tt> or sometimes <tt>""</tt> to distinguish from delimiters.
	 */
	public String readUTF() throws IOException {
		switch (parse.nextToken()) {
			case StreamTokenizer.TT_EOF:
				throw new EOFException("EOF during readUTF");
			case '"': {
        		// XXX: recognize strings like "alles ist ""wunderbar"" hier", too
        		String s = parse.sval;
        		return s;
			}
			default:
				throw newParseException(parse, "string delimiter \"");
        }
	} 
}