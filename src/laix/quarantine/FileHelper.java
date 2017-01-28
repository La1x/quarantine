package laix.quarantine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public final class FileHelper {

    public static String getString(String fileName) throws IOException {
        File file = new File(fileName);
        Reader reader = new FileReader(file);
        BufferedReader bReader = new BufferedReader(reader);
        String line;
        String tmp = "";
        while ((line = bReader.readLine())!= null) {
            tmp += line;
        }

        return  tmp;
    }

    public static boolean writeString (String src, String packetPath, boolean append) {
        File file = new File(packetPath);
        Writer writer = null;
        try {
            FileWriter fw = new FileWriter(file, append);
            writer = new BufferedWriter(fw);
            writer.append(src);
        } catch (Exception e) {
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }
        }
        return true;
    }
}
