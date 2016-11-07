package log;


import log.model.ExcluderLine;
import log.model.ExcluderLineSimple;
import log.model.LogEntry;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;


import java.io.*;
import java.util.*;

import java.util.regex.Pattern;

/**
 * Created by Aziz on 01/01/2016.
 */
public class global {
    List<LogEntry> entries;

    public global(String logfile) throws IOException{
        entries=new ArrayList<LogEntry>();
        readLog(logfile);
    }
    

    public List<LogEntry> getEntries(){
        return entries;
    }

    private void readLog(String logfile) throws IOException {
        String invalidLog=Settings.getInstance().getInvalidLog();
        File invalid=new File(invalidLog);
        if(invalid.exists()){
            invalid.delete();
        }
        boolean writeLogfileNotExcluded=Settings.getInstance().isWriteLogfileNotExcluded();

        BufferedReader bufferedReader=null;
        FileReader fileReader=null;
        int errors=0,lines=0,excludedLines=0;
        try{
            fileReader=new FileReader(logfile);
            bufferedReader=new BufferedReader(fileReader);
            String read;
            while ((read=bufferedReader.readLine()) !=null){
                lines++;
                if(isExcluded(read)){
                    excludedLines++;
                }else {
                    //add line
                    try {
                        LogEntry logEntry=new LogEntry(read);
                        if(!logEntry.isExclude()){
                            entries.add(logEntry);
                            if (writeLogfileNotExcluded) {
                                LEUtil.writeToFile(Settings.getInstance().getLogFileNotExcluded(), read);
                            }
                        }else {
                            excludedLines++;
                        }
                    }catch (Exception ex){
                        LEUtil.writeToFile(invalidLog, read + ": " + ex);
                        // malformed log entry. ignore it.
                        errors++;
                    }
                }
            }



            LEUtil.printInformation(lines + " lines read (total)");
            LEUtil.printInformation(errors + " errors");
            LEUtil.printInformation(excludedLines + " excluded");

        }finally {
            if (fileReader != null) {
                fileReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

    private boolean isExcluded(String line) {
        boolean exclude = false;
        ExcluderLineSimple excludeLineSimple = ExcluderLineSimple.getInstance();
        Iterator<String> excludeSimpleIterator = excludeLineSimple.getExcludePattern().iterator();

        // check if line should be excluded (simple, fast)
        while (excludeSimpleIterator.hasNext() && !exclude) {
            if (line.contains(excludeSimpleIterator.next())) {
                exclude = true;
            }
        }

        // is excluded? return without testing expensive regexes.
        if (exclude) {
            return true;
        }

        ExcluderLine excludeLine = ExcluderLine.getInstance();
        Iterator<Pattern> excludeIterator = excludeLine.getExcludePattern().iterator();

        // check if line should be excluded (regex, slow)
        while (excludeIterator.hasNext() && !exclude) {
            if (excludeIterator.next().matcher(line).matches()) {
                exclude = true;
            }
        }
        return exclude;
    }


    /* * * * * * * * * * * * * * *
     *  Grouping methods below   *
     * * * * * * * * * * * * * * */

    public TreeMap<Integer, List<LogEntry>> groupByResponseSorted() {
        Map<Integer, List<LogEntry>> groupedMap = groupBy(RETRIEVE_TYPE_RESPONSE);
        return sortByOccurrence(groupedMap);
    }

    public TreeMap<Integer, List<LogEntry>> groupByRequestSorted() {
        Map<String, List<LogEntry>> groupedMap = groupBy(RETRIEVE_TYPE_REQUEST);
        return sortByOccurrence(groupedMap);
    }

    public TreeMap<Integer, List<LogEntry>> groupByBrowserSorted() {
        Map<String, List<LogEntry>> groupedMap = groupBy(RETRIEVE_TYPE_BROWSER);
        return sortByOccurrence(groupedMap);
    }

    public TreeMap<Integer, List<LogEntry>> groupByIPSorted() {


        Map<String, List<LogEntry>> groupedMap = groupBy(RETRIEVE_TYPE_IP);
        return sortByOccurrence(groupedMap);
    }

    public TreeMap<Integer, List<LogEntry>> groupByRefererSorted(boolean excludeEmpty) {
        Map<String, List<LogEntry>> groupedMap = groupBy(RETRIEVE_TYPE_REFERER);
        if (excludeEmpty) {
            removeEmptyKeys(groupedMap);
        }
        return sortByOccurrence(groupedMap);
    }

    public TreeMap<Integer, List<LogEntry>> groupByRefererHostSorted(boolean excludeEmpty) {
        Map<String, List<LogEntry>> groupedMap = groupBy(RETRIEVE_TYPE_REFERER_HOST);
        groupedMap.remove(Settings.getInstance().excludefromRefererResult());
        if (excludeEmpty) {
            removeEmptyKeys(groupedMap);
        }
        return sortByOccurrence(groupedMap);
    }

    public TreeMap<Integer, List<LogEntry>> groupByBrowserSimpleSorted() {
        Map<String, List<LogEntry>> groupedMap = groupBy(RETRIEVE_TYPE_BROWSER_SIMPLE);
        return sortByOccurrence(groupedMap);
    }

    public TreeMap<Integer, List<LogEntry>> groupByByteSendSorted() {
        // TODO implement
        // do not group by exact byte, but some ranges
        throw new UnsupportedOperationException("not implemented yet.");
    }

    /**
     * returns a map containing lists containing all entries for one day.
     * <p>
     * The key for the map is the number of occurences.
     * <p>
     *
     *
     * @return
     */
    public TreeMap<Integer, List<LogEntry>> groupByDaySorted() {
        Map<DateMidnight, List<LogEntry>> groupedMap = groupBy(RETRIEVE_TYPE_DAY);
        return sortByOccurrence(groupedMap);
    }

    public TreeMap<Integer, List<LogEntry>> groupByHourSorted() {
        Map<DateTime, List<LogEntry>> groupedMap = groupBy(RETRIEVE_TYPE_HOUR);
        return sortByOccurrence(groupedMap);
    }

    /**
     * returns a map containing the entries grouped by the key described by groupTypeKey.
     *
     * @param <T> type of log entry information ("browser" eg is String, "response" is int, etc)
     * @param groupTypeKey which log entry information to retrieve
     * @return a map containing the entries grouped by the key described by groupTypeKey
     */
   private  <T> Map<T, List<LogEntry>> groupBy(GroupTypeKey<T> groupTypeKey) {
        // T is the type of the key (eg. "String" for "browser")
        Map<T, List<LogEntry>> map = new HashMap<T, List<LogEntry>>();
        for (LogEntry entry : entries) {
            T key = groupTypeKey.getGroupKey(entry);
            if (map.get(key) == null) {
                map.put(key, new ArrayList<LogEntry>());
            }
            map.get(key).add(entry);
        }
        return map;
    }

    /**
     * stores the given log entries in a TreeMap sorted by occurrence.
     * The most occurrence will be at first position.
     *
     * @param <T> key of original map (not relevant to sorting)
     * @param map original map
     * @return sorted map
     */
    public <T> TreeMap<Integer, List<LogEntry>> sortByOccurrence(Map<T, List<LogEntry>> map) {
        TreeMap<Integer, List<LogEntry>> sortedMap = new TreeMap<Integer, List<LogEntry>>(integerComparator);
        Iterator<List<LogEntry>> it = map.values().iterator();
        while (it.hasNext()) {
            List<LogEntry> next = it.next();
            sortedMap.put(next.size(), next);
        }
        return sortedMap;
    }

    /**
     * removes all entries from the map that have the following keys:
     * <p>
     * ""
     * <p>
     * "-"
     *
     * @param map map
     */
    private void removeEmptyKeys(Map<String, List<LogEntry>> map) {
        map.remove("");
        map.remove("-");
    }

    /* * * * * * * * * * * * * *
     *  to file methods below  *
     * * * * * * * * * * * * * */

    public void writeGroupedResponse() {
        writeGrouped(groupByResponseSorted(), RETRIEVE_TYPE_RESPONSE, "groupedResponse.log");
    }

    public void writeGroupedRequest() {
        writeGrouped(groupByRequestSorted(), RETRIEVE_TYPE_REQUEST, "groupedRequest.log");
    }

    public void writeGroupedBrowser() {
        writeGrouped(groupByBrowserSorted(), RETRIEVE_TYPE_BROWSER, "groupedBrowser.log");
    }

    public void writeGroupedIP() {
        writeGrouped(groupByIPSorted(), RETRIEVE_TYPE_IP, "groupedIP.log");
    }

    public void writeGroupedReferer() {
        writeGrouped(groupByRefererSorted(false), RETRIEVE_TYPE_REFERER, "groupedReferer.log");
    }

    public void writeGroupedRefererHost() {
        writeGrouped(groupByRefererHostSorted(false), RETRIEVE_TYPE_REFERER_HOST, "groupedRefererHost.log");
    }

    public void writeGroupedDay() {
        writeGrouped(groupByDaySorted(), RETRIEVE_TYPE_DAY, "groupedDay");
    }

    /**
     * writes the entries in the given TreeMap to the given file
     * in the order they are in.
     * <p>
     * One entry will be printed each line. A line starts with the size of
     * the list in that entry, followed by the LogEntry field described by
     * groupTypeKey.
     *
     * @param <T> type of log entry information ("browser" eg is String, "response" is int, etc)
     * @param groupedBy map to write to file
     * @param groupTypeKey which log entry information to retrieve
     * @param fileName fileName to write to
     */
    public <T> void writeGrouped(TreeMap<Integer, List<LogEntry>> groupedBy, GroupTypeKey<T> groupTypeKey, String fileName) {
        Iterator<List<LogEntry>> itr = groupedBy.values().iterator();
        String spaces = calcTabs(groupedBy.firstKey().toString());

        String path = Settings.getInstance().getResultDir();
        FileWriter fw = null;
        try {
            fw = new FileWriter(path + fileName, false);
            // iterate over grouped entries
            while (itr.hasNext()) {
                List<LogEntry> next = itr.next();
                String typeEntry;
                if (next.isEmpty()) {
                    typeEntry = "";
                } else {
                    // get desired logentry info (eg "browser")
                    typeEntry = groupTypeKey.getGroupKey(next.get(0)).toString();
                }
                fw.write(next.size() + spaces + typeEntry + "\n");

            }
        } catch (Exception ex) {
            LEUtil.printError("Log"
                    + ".writeGrouped(): ERROR writing to file:"
                    + path + fileName + ": " + ex);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    // TODO tabs *should* be calculated for each entry...
    private static String calcTabs(String string) {
        String spaces = "\t";
        for (int i = 1; i < (string.length() / 4); i++) {
            spaces += "\t";

        }
        return spaces;
    }

    /* * * * * * * * * * * * * * * * * * * * *
     *  Sorting methods below (do not group / unused for now) *
     * * * * * * * * * * * * * * * * * * * * */

    public void sortByByteSendLowestFirst() {
        sortWithLowestFirst(byteSendComparator);
    }

    public void sortByByteSendHighestFirst() {
        sortWithHighestFirst(byteSendComparator);
    }

    private void sortWithHighestFirst(Comparator<LogEntry> comparator) {
        // a lot faster than the Collections.sort method
        SortedSet<LogEntry> sortedByRollNo = new TreeSet<LogEntry>(comparator);
        sortedByRollNo.addAll(entries);
        entries.clear();
        entries.addAll(sortedByRollNo);
    }

    private void sortWithLowestFirst(Comparator<LogEntry> comparator) {
        Collections.sort(entries, comparator);
    }

    /* * * * * * * * * * * * * * * * * * *
     *  static stuff and private classes *
     * * * * * * * * * * * * * * * * * * */

    private static final ByteSendComparator byteSendComparator = new ByteSendComparator();
    private static final IntegerComparator integerComparator = new IntegerComparator();

    /**
     * uses the getRequestPathNice method of LogEntry.
     */
    public static final GroupTypeKey<String> RETRIEVE_TYPE_REQUEST = new GroupTypeKey<String>() {
        @Override
        public String getGroupKey(LogEntry entry) {
            return entry.getRequestPathNice();
        }
    };

    /**
     * uses the getResponse method of LogEntry.
     */
    public static final GroupTypeKey<Integer> RETRIEVE_TYPE_RESPONSE = new GroupTypeKey<Integer>() {
        @Override
        public Integer getGroupKey(LogEntry entry) {
            return entry.getResponse();
        }
    };

    /**
     * uses the getBrowser method of LogEntry.
     */
    public static final GroupTypeKey<String> RETRIEVE_TYPE_BROWSER = new GroupTypeKey<String>() {
        @Override
        public String getGroupKey(LogEntry entry) {
            return entry.getBrowser();
        }
    };

    /**
     * uses the getIpAddress method of LogEntry.
     */
    public static final GroupTypeKey<String> RETRIEVE_TYPE_IP = new GroupTypeKey<String>() {
        @Override
        public String getGroupKey(LogEntry entry) {
            return entry.getIpAddress();
        }
    };

    /**
     * uses the getReferer method of LogEntry.
     */
    public static final GroupTypeKey<String> RETRIEVE_TYPE_REFERER = new GroupTypeKey<String>() {
        @Override
        public String getGroupKey(LogEntry entry) {
            return entry.getReferer();
        }
    };

    /**
     * uses the getRefererHost method of LogEntry.
     */
    public static final GroupTypeKey<String> RETRIEVE_TYPE_REFERER_HOST = new GroupTypeKey<String>() {
        @Override
        public String getGroupKey(LogEntry entry) {
            return entry.getRefererHost();
        }
    };


    /**
     * uses the getDate method of LogEntry.
     */
    public static final GroupTypeKey<java.lang.Long> RETRIEVE_TYPE_DATE_MILLIS = new GroupTypeKey<java.lang.Long>() {
        @Override
        public java.lang.Long getGroupKey(LogEntry entry) {
            return entry.getDate();
        }
    };

    /**
     * uses the getDate method of LogEntry and creates new DateMidnight with it.
     */
    public static final GroupTypeKey<DateMidnight> RETRIEVE_TYPE_DAY = new GroupTypeKey<DateMidnight>() {
        @Override
        public DateMidnight getGroupKey(LogEntry entry) {
            return new DateMidnight(entry.getDate());
        }
    };

    /**
     * uses the getDate method of LogEntry and creates new DateTime with it,
     * setting minuteOfHour and secondOfMinute to zero.
     */
    public static final GroupTypeKey<DateTime> RETRIEVE_TYPE_HOUR = new GroupTypeKey<DateTime>() {
        @Override
        public DateTime getGroupKey(LogEntry entry) {

            DateTime temp = new DateTime(entry.getDate());
            return new DateTime(temp.getYear(), temp.getMonthOfYear(), temp.getDayOfMonth(),
                    temp.getHourOfDay(), 0, 0);


        }
    };

    /**
     * uses the getDate method of LogEntry and creates new DateTime with it,
     * setting everything but hours to zero.
     */
    public static final GroupTypeKey<DateTime> RETRIEVE_TYPE_ONLY_HOUR = new GroupTypeKey<DateTime>() {
        @Override
        public DateTime getGroupKey(LogEntry entry) {

            DateTime temp = new DateTime(entry.getDate());
            return new DateTime(0, 1, 1,
                    temp.getHourOfDay(), 0, 0);


        }
    };

    /**
     * uses the getDate method of LogEntry and creates new DateTime with it,
     * setting everything but hours to zero.
     */
    public static final GroupTypeKey<Integer> RETRIEVE_TYPE_ONLY_DAY_OF_WEEK = new GroupTypeKey<Integer>() {
        @Override
        public Integer getGroupKey(LogEntry entry) {

            DateTime temp = new DateTime(entry.getDate());
            DateTime.Property dayOfWeek = temp.dayOfWeek();
            return dayOfWeek.get();
        }
    };

    /**
     * uses the getBrowserSimple method of LogEntry.
     */
    public static final GroupTypeKey<String> RETRIEVE_TYPE_BROWSER_SIMPLE = new GroupTypeKey<String>() {
        @Override
        public String getGroupKey(LogEntry entry) {
            return entry.getBrowserSimple();
        }
    };

    /**
     * This interface defines which values of a LogEntry is to be used.
     * <p>
     * It may for example return the browser or response of a LogEntry or a combination
     * of them.
     *
     * @param <T> type of log entry (eg String vor browser or int for response)
     */
    public interface GroupTypeKey<T> {
        T getGroupKey(LogEntry entry);
    }

    private static class ByteSendComparator implements Comparator<LogEntry>, Serializable {
        private static final long serialVersionUID = 1L;
        @Override
        public int compare(LogEntry l, LogEntry l1) {
            return l1.getBytesSend() - l.getBytesSend();
        }
    }

    /**
     * used to sort integer. high value will be at the front.
     */
    private static class IntegerComparator implements Comparator<Integer>, Serializable {
        private static final long serialVersionUID = 1L;
        @Override
        public int compare(Integer l, Integer l1) {
            return l1 - l;
        }
    }

    /* * * * * * * * * * * * * * * * * * *
     *  deprecated methods               *
     * * * * * * * * * * * * * * * * * * */

    /**
     * returns all entries grouped together in a map by their response-code.
     * The map will be sorted according to the number of occurences (the size of the list).
     * The number of occurences will be the key of the map as well.
     *
     * @return
     */
    private TreeMap<Integer, List<LogEntry>> groupByResponseSorted_DEP() {
        // first, the entries will be grouped
        long currentTimeMillis = System.currentTimeMillis();
        Map<Integer, List<LogEntry>> map = new HashMap<Integer, List<LogEntry>>();
        for (LogEntry entry : entries) {
            int key = entry.getResponse();
            if (map.get(key) == null) {
                map.put(key, new ArrayList<LogEntry>());
            }
            map.get(key).add(entry);
        }

        // then, they will be sorted by occurence
        TreeMap<Integer, List<LogEntry>> sortedMap = new TreeMap<Integer, List<LogEntry>>(integerComparator);
        Iterator<List<LogEntry>> it = map.values().iterator();
        while (it.hasNext()) {
            List<LogEntry> next = it.next();
            sortedMap.put(next.size(), next);
        }
        LEUtil.printInformation(" grouped and sorted in " + (System.currentTimeMillis() - currentTimeMillis));
        return sortedMap;
    }
}
