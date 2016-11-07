package log.model;

import java.io.*;
import log.LEUtil;
import log.Settings;


/**
 * Created by Aziz on 30/12/2015.
 */
public abstract class AbstractExcluder {

    protected void loadPattern(File excludeLineFile){
        BufferedReader bufferedReader =null;
        FileReader fileReader=null;
        int errors=0;
        int lines=0;

        try{
            fileReader=new FileReader(excludeLineFile);
            bufferedReader=new BufferedReader(fileReader);
            String read;
            while ((read= bufferedReader.readLine()) !=null){
                if(!read.contains("#") && !read.equals("")){
                    lines++;
                    try {
                        addPattern(read);
                    }catch (Exception e){
                          LEUtil.writeToFile(Settings.getInstance().getInvalidExcludePatternLog(),"file: " + excludeLineFile
                                  + " pattern: " + read + " error : " + e);
                            errors++;
                    }
                }
            }
            LEUtil.printInformation(lines+" line patterns read (totale");
            LEUtil.printInformation(errors+" erreurs");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    protected abstract void addPattern(String phrase);
}
