package log.model;

import log.Settings;
import org.joda.time.format.DateTimeFormat;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class LogEntry {
    private static final org.joda.time.format.DateTimeFormatter dateFormatter;
    private static final Pattern logEntryPattern;

    private final String ipAddress;
    private final long date;
    private final String request ;
    private final int response ;
    private final int bytesSend ;
    private final String browser ;
    private final String referer ;

    enum filesExtension {
        js,gif,png,jpg,jpeg,css,pdf,txt,xml
    }

    enum TypeFile{
        js,gif,png,jpg,jpeg,css,pdf,txt,xml,html,php,asp,jsp
    }

    static {
        Settings settings=Settings.getInstance();
      //  logEntryPattern=Pattern.compile("^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"");
        dateFormatter= DateTimeFormat.forPattern(settings.getDateFormat()).withLocale(Locale.ENGLISH);
        logEntryPattern=Pattern.compile(settings.getLogFormat());
    }

    boolean exclude=false;
    public boolean isExclude(){
        return exclude;
    }

    public LogEntry(String entryString) throws Exception {
        //parse log file line
        Matcher matcher = logEntryPattern.matcher(entryString);
        if (!matcher.matches() || 7 != matcher.groupCount()) {
            throw new Exception("invalid log entry: " + entryString);
        }

        ExcluderByType excluder=ExcluderByType.getInstance();

        ipAddress = matcher.group(1);
        request = matcher.group(3);
        response = Integer.valueOf(matcher.group(4));
        referer = matcher.group(6);
        browser = matcher.group(7);

        Iterator<String> browserSimpleExclude = excluder.getBrowserSimpleExclude().iterator();
        Iterator<String> ipSimpleExclude = excluder.getIPSimpleExclude().iterator();
        Iterator<String> requestSimpleExclude = excluder.getRequestSimpleExclude().iterator();
        Iterator<String> responseSimpleExclude = excluder.getResponseSimpleExclude().iterator();
        Iterator<String> refererSimpleExclude = excluder.getRefererSimpleExclude().iterator();

        checkSimple(browserSimpleExclude, browser);
        checkSimple(ipSimpleExclude, ipAddress);
        checkSimple(requestSimpleExclude, request);
        checkSimple(responseSimpleExclude, "" + response);
        checkSimple(refererSimpleExclude, referer);

        System.out.println("l'etat simple  "+exclude);

        if (!exclude) {
            Iterator<Pattern> browserRegexExclude = excluder.getBrowserRegexExclude().iterator();
            Iterator<Pattern> ipRegexExclude = excluder.getIPRegexExclude().iterator();
            Iterator<Pattern> requestRegexExclude = excluder.getRequestRegexExclude().iterator();
            Iterator<Pattern> responseRegexExclude = excluder.getResponseRegexExclude().iterator();
            Iterator<Pattern> refererRegexExclude = excluder.getRefererRegexExclude().iterator();

            checkPattern(browserRegexExclude, browser);
            checkPattern(ipRegexExclude, ipAddress);
            checkPattern(requestRegexExclude, request);
            checkPattern(responseRegexExclude, "" + response);
            checkPattern(refererRegexExclude, referer);
        }

        System.out.println("l'etat pattern  "+exclude);

        bytesSend = Integer.valueOf(matcher.group(5));

        if (!exclude) {
            date = parseDate(matcher.group(2));
        } else {
            date = -1; // if entry is excluded, do not parse date
        }



    }

    private void checkSimple(Iterator<String> simple,String input){
        while (simple.hasNext() && !exclude){
            if(input.contains(simple.next())){
                exclude=true;
            }
        }
    }

    private void checkPattern(Iterator<Pattern> pattern, String input) {
        // check if line should be excluded (regex, slow)
        while (pattern.hasNext() && !exclude) {
            if (pattern.next().matcher(input).matches()) {
                exclude = true;
            }
        }
    }

    private long parseDate(String date) throws ParseException {
        // TODO parse is slow (takes about 10% of total time and 50% of LogEntry init time)
        return dateFormatter.parseDateTime(date).getMillis();
    }

    public boolean isPage(){
        if(getExtensionRequest().isEmpty() || getExtensionRequest().equals("php") || getExtensionRequest().equals("html") || getExtensionRequest().equals("jsp") ||  getExtensionRequest().equals("asp")){
            return true;
        }
        return false;
    }

    public boolean isHits(){

        for(filesExtension ext:filesExtension.values()){

            if(getExtensionRequest().equals(ext.name()) || isPage()){
                return true;
            }
        }
        return false;
    }


    /*
    * utiliser pour retourner l'extension
    * */
    private String getExtensionRequest() {
        String ext;
        ext=getRequestPathNice().substring(getRequestPathNice().lastIndexOf(".") + 1);
        if(ext.equals(getRequestPathNice())){
            return "";
        }
        return ext;
    }

    public String getTypeFile(){
        for(TypeFile ext:TypeFile.values()){

            if(getExtensionRequest().equals(ext.name()) || isPage()){
                return ext.name();
            }
        }
        return "inconnu";
    }






    public String getGoogleSearchTerm(){
        if(!getRefererHost().contains("google")){
            return "";
        }

        try {
            String args=referer.split("\\?")[1];
            String[] argsArray=args.split("&");
            for(int i=0;i<argsArray.length;i++){
                String tmp=argsArray[i];
                if(tmp.startsWith("q=")){
                    return tmp.replace("q=","").replace("%20"," ").replace("+"," ").replace("%2B"," ");
                }
            }
        }catch (Exception e){
            return "";
        }

        return "";


    }

    public String getRefererHost(){
        String referer=getReferer();

        if(referer.length() >4){
            try {
                URI uri=new URI(referer);
                return uri.getHost().replace("www.","");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }catch (NullPointerException npe){
                return referer;
            }

        }
            return referer;

    }

    public String getRequestPathNice() {
        String path = request;
        // extract path
        int startOfPath = path.indexOf("/");
        int endOfPath = path.lastIndexOf(" ");
        try {
            path = path.substring(startOfPath, endOfPath);
        } catch (StringIndexOutOfBoundsException ioe) {
            // simple pattern did not match path. try removing everything else
            path = path.replace("HTTP/1.0", "");
            path = path.replace("HTTP/1.1", "");
            path = path.replace("GET", "");
            path = path.replace("HEAD", "");
            path = path.replace("POST", "");
            path = path.replace(" ", "");
        }

        // remove trailing slash (if it is not the only character in path)
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        // remove arguments (if they exist and settings say they should be removeD)
        else if (path.contains("?") && Settings.getInstance().isRemoveArgsFromReqPath()) {
            int end = path.indexOf("?");
            path = path.substring(0, end);
        }
        return path;
    }

    public String getBrowserSimple() {
        String simpleBrowser = getBrowser();
        // no browser, return
        if (simpleBrowser.equals("-") || simpleBrowser.equals("")) {
            return simpleBrowser;
        }

        // check standart browser
        String toLowerCase = simpleBrowser.toLowerCase();
        if (toLowerCase.contains("opera")) {
            return "Opera";
        } else if (toLowerCase.contains("firefox")) {
            return "Firefox";
        } else if (toLowerCase.contains("chrome")) {
            return "Chrome";
        } else if (toLowerCase.contains("safari")) {
            return "Safari";
        } else if (toLowerCase.contains("msie")) {
            return "Internet Explorer";
        }else if (toLowerCase.contains("Netscape")){
            return "Netscape";
        }else if (toLowerCase.contains("edge")){
            return "Microsoft edge";
        }

        // return the first part of the browser (up to the first slash or first
        // space, whichever comes first).
        int firstSlash = simpleBrowser.indexOf("/");
        int firstSpace = simpleBrowser.indexOf(" ");
        try {
            simpleBrowser = simpleBrowser.substring(0, Math.min(firstSlash, firstSpace));
        } catch (StringIndexOutOfBoundsException ioe) {
            // just return full browser
            return "inconnu";
        }
        return simpleBrowser;
    }


    public boolean isViewable(){
        if(getBrowser().contains("/") && getBrowser().contains(".") && (response==200 || response==304)) {
            return true;
        }
        return false;
    }


    /**
     * @return the ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * @return the date
     */
    public long getDate() {
        return date;
    }

    /**
     * @return the request
     */
    public String getRequest() {
        return request;
    }

    /**
     * @return the response
     */
    public int getResponse() {
        return response;
    }

    /**
     * @return the bytesSend
     */
    public int getBytesSend() {
        return bytesSend;
    }

    /**
     * @return the browser
     */
    public String getBrowser() {
        return browser;
    }

    /**
     * @return the referer
     */
    public String getReferer() {
        return referer;
    }

    @Override
    public String toString() {
        return "LogEntry{" + "ipAddress=" + ipAddress + ", date=" + dateFormatter.print(date) + ", request=" + request + ", response=" + response + ", bytesSend=" + bytesSend + ", browser=" + browser + ", referer=" + referer + '}';
    }











}
