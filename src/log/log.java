package log;
import log.model.LogEntry;

import java.net.URI;
import java.util.*;

public class log {

   // public static org.joda.time.format.DateTimeFormatter dateFormatter;





    public static void main(String[] args) throws Exception {



        global global=new global("logfile.log");
        global.writeGroupedIP();



   //     String[] argsArray=request.split("\\.")
     /*   if(argsArray.length==2){

            for(PagesExtension ext :PagesExtension.values()){
                if(argsArray[1].equals(ext.name())){
                    System.out.println("1 "+ext);
                }
            }

            for(filesExtension ext :filesExtension.values()){
                if(argsArray[1].equals(ext.name())){
                    System.out.println("2 "+ext);
                }
            }

        }*/
    //  LocalTime localTime=new LocalTime();



      //  DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM");

    //    System.out.println(fmt.parseDateTime("december").getMillis());

       // String date = "29/Oct/2000";


    //   dateFormatter = DateTimeFormat.forPattern(Settings.getInstance().getDateFormat());

    //  dateFormatter.parseDateTime(date).getMillis();

        //SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss");
       // Date date= formatter.parse("27/Oct/2000:09:27:09");

    //LogEntry logEntry=new LogEntry("123.45.67.89 - - [27/Oct/2000:09:27:09 -0400] \"GET /java/javaResources.html HTTP/1.0\" 200 10450 \"-\" \"Mozilla/4.6 [en] (X11; U; OpenBSD 2.8 i386; Nav)\"");



       // LogEntry logEntry1=new LogEntry("80.8.55.1 - - [01/Jan/2001:00:00:00 +0100] \"GET /images/image2.png HTTP/1.0\" 200 7009 \"http://www.google.com/cgi-bin/search.pl?q=a\" \"Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)\"");
      // System.out.println(logEntry1.isViewable());
    //LogEntry logEntry=new LogEntry("127.0.0.1 - - [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326");
    //    LogEntry logEntry=new LogEntry("127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326 \"http://www.example.com/start.html\" \"Mozilla/4.08 [en] (Win98; I ;Nav)\"");
      //  System.out.println(logEntry.getBrowser());
       // System.out.println( logEntry.getRequestPathNice());





    }
}



