package entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *   [Singleton]
 *   get input source when user specifying filename from the terminal, return the whole bunch of character stream to the Lexer entity.
 */
public class Reader {
    private static Reader reader = new Reader();

    private Reader() {}

    public static Reader getReader() { return reader; }

    public String read(String filename) throws IOException {
        File file = new File(filename);
        byte[] length = new byte[(int) file.length()];
        FileInputStream inputStream = new FileInputStream(file);
        inputStream.read(length);
        inputStream.close();
        return new String(length);
    }
}
