package log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;



/**
 * Utility class (mainly for message processing).
 */
public class LEUtil {

    /**
     * displays errors to user.
     *
     * @param errorMessage errorMessage
     */
    public static void printError(String errorMessage) {
        System.out.println("error: " + errorMessage);
    }

    /**
     * displays information to user.
     *
     * @param infoMessage infoMessage
     */
    public static void printInformation(String infoMessage) {
        System.out.println("info: " + infoMessage);
    }

    /**
     * appends given string to given file.
     *
     * @param fileName fileName
     * @param message message
     */
    public static void writeToFile(String fileName, String message) {
        log(fileName, message, false);
    }

    /**
     * appends given string to given file.
     *
     * @param file file
     * @param message message
     */
    public static void writeToFile(File file, String message) {
        // TODO do not get path just to create file again in log.log(..)...
        log(file.getAbsolutePath(), message, false);
    }

    /**
     * logs the error message.
     *
     * @param message message
     */
    public static void logError(String message) {
        log(Settings.getInstance().getErrorLog(), message, true);
    }

    /**
     * appends the given string to a log.log file and adds a newline character.
     *
     * @param message string to log.log
     * @param fileName name of the log.log file
     */
    private static void log(String fileName, String message, boolean prepentDate) {
        String preString = "";
        if (prepentDate) {
            preString = getDate("yyyy.MM.dd-HH:mm:ss-z") + ": ";
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(fileName, true);
            fw.write(preString + message + "\n");
        } catch (Exception ex) {
            System.out.println("Util"
                    + ".log.log(): ERROR writing to log.log:"
                    + fileName + ": " + ex);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ex) {
                    System.out.println("Util"
                    + ".log.log(): ERROR closing stream:"
                    + fileName + ": " + ex);
                }
            }
        }
    }
    /**
     * returns the current date in desired format.
     *
     * example for format: "yyyy.MM.dd-HH:mm:ss-z" or "yyyy.mm.dd".
     * @param format
     * @return
     */
    public static String getDate(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(new Date());
    }

    /**
     * returns a sorted map containing the top n entries.
     *
     * @param <K>
     * @param <V>
     * @param max
     * @param source
     * @return
     */
    public static <K, V> SortedMap<K, V> getTopEntries(int max, SortedMap<K, V> source) {
        int count = 0;
        SortedMap<K, V> target = new TreeMap<K, V>();
        for (Map.Entry<K, V> entry : source.entrySet()) {
            if (count >= max) {
                break;
            }

            target.put(entry.getKey(), entry.getValue());
            count++;
        }
        return target;
    }

}
