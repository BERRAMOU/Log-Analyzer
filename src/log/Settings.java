package log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

public final class Settings {

    private static SortedProperties settings = new SortedProperties();

    private static class SettingsHolder {
        private static final Settings INSTANCE = new Settings();
    }

    public static Settings getInstance() {
        return SettingsHolder.INSTANCE;
    }

    private Settings(){
        FileInputStream in = null;
        try{
            in = new FileInputStream("log.log.properties");
            settings.load(in);

        } catch (Exception e) {
            createDefaultSettingsFile();
        }finally {
            if(in !=null){
                try{
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createDefaultSettingsFile() {
         /* General */
        settings.put("logfile","logfile.log.log");
        settings.put("writeLogfileNotExcluded", "true");
        settings.put("readLogfileNotExcluded", "false");
        settings.put("logFormat", "^([\\d\\w.:]+) \\S+ \\S+ \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]*+)\" \"([^\"]+)\"");
        settings.put("logDateFormat", "dd/MMM/yyyy:HH:mm:ss Z");
        settings.put("excludeFromRefererResults", "aziz.com");

         /* what to create */
        settings.put("doWriteBrowser", "true");
        settings.put("doWriteIP", "false");
        settings.put("doWriteReferer", "true");
        settings.put("doWriteRequest", "true");
        settings.put("doWriteResponse", "true");
        settings.put("doWriteRefererHost", "true");
        settings.put("doWriteDay", "true");

        /* Pathes */
        settings.put("dirResult", System.getProperty("user.dir")
                + System.getProperty("file.separator") + "results" + System.getProperty("file.separator"));

        settings.put("dirExclude", System.getProperty("user.dir")
                + System.getProperty("file.separator") + "exclude" + System.getProperty("file.separator"));

        settings.put("dirError", System.getProperty("user.dir")
                + System.getProperty("file.separator") + "error" + System.getProperty("file.separator"));
        /* Error files */
        settings.put("errorLog", "error.log.log");
        settings.put("invalidLog", "invalid.log.log");
        settings.put("invalidExcludePattern", "invalidExcludePattern.log.log");
         //Exclude Files
        settings.put("excludeFileExtentionRegex", ".regex");
        settings.put("excludeFileExtentionSimple", ".simple");
        settings.put("excludeLineFile", "line");
        settings.put("excludeBrowserFile", "browser");
        settings.put("excludeIPFile", "ip");
        settings.put("excludeRefererFile", "referer");
        settings.put("excludeRequestFile", "request");
        settings.put("excludeResponseFile", "response");

        //create File

        FileOutputStream out=null;
        try{
            out=new FileOutputStream("log.log.properties");
            settings.store(out,"les proprietes de log.log");
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out !=null){
                try {
                    out.close();
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }


    }


  /*
  * return true si les ligne de logfile not excluded seront ecrite dans une fchier
  * dans le cas ou <code>writeLogfileNotExcluded</code> est true et le fichier logfileNotExcluded.log.log n'existe pas
  */

    public boolean isWriteLogfileNotExcluded(){
        return (Boolean.valueOf(settings.getProperty("writeLogfileNotExcluded","true"))) && !(new File(getLogFileNotExcluded()).exists());
    }

    public String getLogFileNotExcluded(){
        return settings.getProperty("logfileNotExcluded", "logfileNotExcluded.log.log");
    }

    /*
    return le nom de logfile
    si  <code>readLogfileNotExcluded</code> est enable alors on va tenter de lire le log.log file
    qui contient les ligne qui n'ont pas été exclus par aucunes des regles
    sinon ils va retourner le nom de fichier specifier ici <code>logfile</code>
     */

    public String getLogFile(){
        String logfile;

        if(Boolean.valueOf(settings.getProperty("readLogfileNotExcluded", "false")) && new File(getLogFileNotExcluded()).exists()){
            logfile=getLogFileNotExcluded();
        }else{
            logfile=settings.getProperty("logfile", "logfile.log.log");
        }

        return logfile;
    }

    /*
    return le chemain absolu de dossier de tous les resultats enregistrer
    si le dossier n'exsiste pas on le cree
     */

    public String getResultDir(){
        String dir=settings.getProperty("dirResult",System.getProperty("user.dir")+System.getProperty("file.separator")+"resilts"+System.getProperty("file.separator"));

        makeDirs(dir);

        return dir;
    }

    private void makeDirs(String directory){
        File f=new File(directory);
        makeDirs(f);
    }

    /*
    cree tous les dossier qui sont decrit dans le constricteur de file
     */

    private void makeDirs(File file){
        if(!file.exists()){
            file.mkdirs();
        }
    }

    public String getLogFormat() {
        return settings.getProperty("logFormat", "^([\\d\\w.:]+) \\S+ \\S+ \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]*+)\" \"([^\"]+)\"");
    }

    public String getDateFormat() {
        return settings.getProperty("logDateFormat", "dd/MMM/yyyy:HH:mm:ss Z");
    }

  /*
   return a le reference qui le faut l'exclure des resultats
   */

    public String excludefromRefererResult() {
        return settings.getProperty("excludeFromRefererResults", "");
    }

    /*
    return le chemin absolu de dossier de tous les fichier des regles d'exclusion
    s'il n'exsist pas on le cree
     */

    private String getExcludeDir(){
        String excludeDir = settings.getProperty("dirExclude", System.getProperty("user.dir")
                + System.getProperty("file.separator") + "exclude" + System.getProperty("file.separator"));
        makeDirs(excludeDir);
        return excludeDir;
    }

    /*
    return l'extension de tous les fichiers contenant les strings
    et regix  utiliser pour exclure les ligne du fichier log.log
     */

    private String getExcludeSimpleExtention() {
        return settings.getProperty("excludeFileExtentionSimple", ".simple");
    }

    private String getExcludeRegexExtention() {
        return settings.getProperty("excludeFileExtentionRegex", ".regex");
    }

    /*
    return le fichier decrit par le filename et l'extention de dossier exclude
    s'il n'exsiste pas on le cree
     */

    private File getExcludeFile(String filename,String fileExtension) throws IOException {
        File excludeLineFile = new File(getExcludeDir()+filename+fileExtension);
        if(!excludeLineFile.exists()){
            excludeLineFile.createNewFile();
            if(fileExtension.equals(getExcludeSimpleExtention())){
                LEUtil.writeToFile(excludeLineFile,"#explication");
            }else {
                LEUtil.writeToFile(excludeLineFile, "#explication");
            }
        }
        return excludeLineFile;
    }

    /*
    return le fichier contient les regles d'exclusion de ligne
    si n'existe pas on le cree
    si la ligne matched aucun regix sera ignorer
     */

    public  File getExcludeLineFile() throws IOException {
        return getExcludeFile(settings.getProperty("excludeLineFile","line"),getExcludeRegexExtention());
    }

    public File getExcludeLineSimpleFile() throws IOException {
        return getExcludeFile(settings.getProperty("excludeLineFile", "line") ,
                getExcludeSimpleExtention());
    }

    public File getExcludeBrowserFile() throws IOException {
        return getExcludeFile(settings.getProperty("excludeBrowserFile", "browser") ,
                getExcludeRegexExtention());
    }

    public File getExcludeBrowserSimpleFile() throws IOException {
        return getExcludeFile(settings.getProperty("excludeBrowserFile", "browser"),
                getExcludeSimpleExtention());
    }

    public File getExcludeIPFile() throws IOException {
        return getExcludeFile(settings.getProperty("excludeIPFile", "ip"),
                getExcludeRegexExtention());
    }

    public File getExcludeIPSimpleFile() throws IOException {
        return getExcludeFile(settings.getProperty("excludeIPFile", "ip"),
                getExcludeSimpleExtention());
    }

    public File getExcludeRefererFile() throws IOException {
        return getExcludeFile(settings.getProperty("excludeRefererFile", "referer"),
                getExcludeRegexExtention());
    }

    public File getExcludeRefererSimpleFile() throws IOException {
        return getExcludeFile(settings.getProperty("excludeRefererFile", "referer"),
                getExcludeSimpleExtention());
    }

    public File getExcludeRequestFile() throws IOException {
        return getExcludeFile(settings.getProperty("excludeRequestFile", "request"),
                getExcludeRegexExtention());
    }

    public File getExcludeRequestSimpleFile() throws IOException {
        return getExcludeFile(settings.getProperty("excludeRequestFile", "request"),
                getExcludeSimpleExtention());
    }

    public File getExcludeResponseFile() throws IOException {
        return getExcludeFile(settings.getProperty("excludeResponseFile", "response"),
                getExcludeRegexExtention());
    }

    public File getExcludeResponseSimpleFile() throws IOException {
        return getExcludeFile(settings.getProperty("excludeResponseFile", "response"),
                getExcludeSimpleExtention());
    }

    /*
    return dossier des erreurs
     */

    public String getErrorDir() {
        String errorDir = settings.getProperty("dirError", System.getProperty("user.dir")
                + System.getProperty("file.separator") + "error" + System.getProperty("file.separator"));
        makeDirs(errorDir);
        return errorDir;
    }

    /*
    return path log.log
     */

    public String getErrorLog() {
        return getErrorDir()
                + settings.getProperty("errorLog", "error.log.log");
    }

    /*
    path invalid log.log entree
     */

    public String getInvalidLog() {
        return getErrorDir()
                + settings.getProperty("invalidLog", "invalid.log.log");
    }

    /*
    invalid exclude pattern
     */

    public String getInvalidExcludePatternLog() {
        return getErrorDir()
                + settings.getProperty("invalidExcludePattern", "invalidExcludePattern.log");
    }

    /**
     * return whether or not to remove the arguments of a path.
     * <p>
     * For example:
     * <p>
     * /test.php?args1=true&args2=5
     * <p>
     * would become /test.php
     *
     * @return whether or not to remove the arguments of a path
     */
    public boolean isRemoveArgsFromReqPath() {
        return Boolean.valueOf(settings.getProperty("removeArgsFromReqPath", "true"));
    }

    /**
     * returns boolean value of the property for the given string.
     * <p>
     * default is true.
     *
     * @param create property to check
     * @return boolean value of the property for the given string
     */
    private boolean isCreate(String create) {
        return Boolean.valueOf(settings.getProperty(create, "true"));
    }

    /**
     * returns whether to write grouped and sorted browsers to file.
     *
     * @return whether to write grouped and sorted browsers to file
     */
    public boolean isWriteBrowser() {
        return isCreate("doWriteBrowser");
    }

    /**
     * returns whether to write grouped and sorted ips to file.
     *
     * @return whether to write grouped and sorted ips to file
     */
    public boolean isWriteIP() {
        return Boolean.valueOf(settings.getProperty("doWriteIP", "false"));
    }

    /**
     * returns whether to write grouped and sorted referer to file.
     *
     * @return whether to write grouped and sorted referer to file
     */
    public boolean isWriteReferer() {
        return isCreate("doWriteReferer");
    }

    /**
     * returns whether to write grouped and sorted requests to file.
     *
     * @return whether to write grouped and sorted requests to file
     */
    public boolean isWriteRequest() {
        return isCreate("doWriteRequest");
    }

    /**
     * returns whether to write grouped and sorted responses to file.
     *
     * @return whether to write grouped and sorted responses to file
     */
    public boolean isWriteResponse() {
        return isCreate("doWriteResponse");
    }

    /**
     * returns whether to write grouped and sorted referer hosts to file.
     *
     * @return whether to write grouped and sorted referer hosts to file
     */
    public boolean isWriterefererHost() {
        return isCreate("doWriteRefererHost");
    }

    /**
     * returns whether to write grouped and sorted days to file.
     *
     * @return whether to write grouped and sorted days to file
     */
    public boolean isWriteDay() {
        return isCreate("doWriteDay");
    }

































}
