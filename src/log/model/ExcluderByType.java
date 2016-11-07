package log.model;


import log.Settings;
import log.LEUtil;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Aziz on 30/12/2015.
 */
public class ExcluderByType {
 enum Type{
        BROWSER,
        IP,
        REFERER,
        REQUEST,
        RESPONSE
    }

 private Map<Type,List<String>>  excludeSimple;
 private Map<Type,List<Pattern>> excludePattern;

    private static class ExcluderByTypeHolder {
        private static final ExcluderByType INSTANCE = new ExcluderByType();
    }

    public static ExcluderByType getInstance() {
        return ExcluderByTypeHolder.INSTANCE;
    }
 private int errors=0;
 private int lines=0;


    private void loadPattern(File excludeLineFile, Type key, boolean isToPattern) {

        BufferedReader br = null;
        FileReader fr = null;
        try {
            try {
                fr = new FileReader(excludeLineFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            br = new BufferedReader(fr);
            String read;
            try {
                while ((read = br.readLine()) != null) {
                    if (!read.contains("#") && !read.equals("")) {
                        lines++;
                        if (isToPattern) {
                            try {
                                addPattern(key, read);
                            } catch (Exception ex) {
                                // malformed entry. log and ignore it.
                                LEUtil.writeToFile(Settings.getInstance().getInvalidExcludePatternLog(),
                                        "file: " + excludeLineFile
                                                + " pattern: " + read + " error : " + ex);
                                errors++;
                            }
                        } else {
                            addSimple(key, read);
                        }
                    }
                }
            } catch (IOException ex) {
                LEUtil.logError("ExcluderByType:loadPattern(): " + ex);
                ex.printStackTrace();
            }
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException ex) {
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private void initMaps(){
        this.excludeSimple=new HashMap<Type,List<String>>();
        this.excludePattern=new HashMap<Type,List<Pattern>>();

        for(Type type : Type.values()){
            excludeSimple.put(type,new ArrayList<>());
            excludePattern.put(type,new ArrayList<>());
        }
    }

    private ExcluderByType(){
        initMaps();
        Settings settings=Settings.getInstance();
        try {
            loadPattern(settings.getExcludeBrowserFile(), Type.BROWSER, true);
        } catch (IOException ex) { // file does not exist. too bad, no exlude pattern
        }
        try {
            loadPattern(settings.getExcludeBrowserSimpleFile(), Type.BROWSER, false);
        } catch (IOException ex) { // file does not exist. too bad, no exlude pattern
        }

        try {
            loadPattern(settings.getExcludeIPFile(), Type.IP, true);
        } catch (IOException ex) { // file does not exist. too bad, no exlude pattern
        }
        try {
            loadPattern(settings.getExcludeIPSimpleFile(), Type.IP, false);
        } catch (IOException ex) { // file does not exist. too bad, no exlude pattern
        }

        try {
            loadPattern(settings.getExcludeRefererFile(), Type.REFERER, true);
        } catch (IOException ex) { // file does not exist. too bad, no exlude pattern
        }
        try {
            loadPattern(settings.getExcludeRefererSimpleFile(), Type.REFERER, false);
        } catch (IOException ex) { // file does not exist. too bad, no exlude pattern
        }

        try {
            loadPattern(settings.getExcludeRequestFile(), Type.REQUEST, true);
        } catch (IOException ex) { // file does not exist. too bad, no exlude pattern
        }
        try {
            loadPattern(settings.getExcludeRequestSimpleFile(), Type.REQUEST, false);
        } catch (IOException ex) { // file does not exist. too bad, no exlude pattern
        }

        try {
            loadPattern(settings.getExcludeResponseFile(), Type.RESPONSE, true);
        } catch (IOException ex) { // file does not exist. too bad, no exlude pattern
        }
        try {
            loadPattern(settings.getExcludeResponseSimpleFile(), Type.RESPONSE, false);
        } catch (IOException ex) { // file does not exist. too bad, no exlude pattern
        }
        LEUtil.printInformation(lines + " type patterns read (total)");
        LEUtil.printInformation(errors + " errors");

    }

    private void addSimple(Type type,String exclude){
        excludeSimple.get(type).add(exclude);
    }

    private void addPattern(Type type,String exclude) throws Exception{
        excludePattern.get(type).add(Pattern.compile(exclude));
    }

    public List<String> getBrowserSimpleExclude() {
        return excludeSimple.get(Type.BROWSER);
    }

    public List<Pattern> getBrowserRegexExclude() {
        return excludePattern.get(Type.BROWSER);
    }

    public List<String> getIPSimpleExclude() {
        return excludeSimple.get(Type.IP);
    }

    public List<Pattern> getIPRegexExclude() {
        return excludePattern.get(Type.IP);
    }
    public List<String> getRefererSimpleExclude() {
        return excludeSimple.get(Type.REFERER);
    }

    public List<Pattern> getRefererRegexExclude() {
        return excludePattern.get(Type.REFERER);
    }
    public List<String> getRequestSimpleExclude() {
        return excludeSimple.get(Type.REQUEST);
    }

    public List<Pattern> getRequestRegexExclude() {
        return excludePattern.get(Type.REQUEST);
    }
    public List<String> getResponseSimpleExclude() {
        return excludeSimple.get(Type.RESPONSE);
    }

    public List<Pattern> getResponseRegexExclude() {
        return excludePattern.get(Type.RESPONSE);
    }








}
