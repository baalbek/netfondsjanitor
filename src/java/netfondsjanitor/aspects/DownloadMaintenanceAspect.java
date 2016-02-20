package netfondsjanitor.aspects;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.TextPage;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import oahu.annotations.StoreHtmlPage;
import oahu.annotations.StoreTxtPage;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 5/10/13
 * Time: 10:20 AM
 */

@Aspect
public class DownloadMaintenanceAspect {

    private int jax;
    private Logger log = Logger.getLogger(getClass().getPackage().getName());

    private String htmlFileStoreDir;
    private String htmlFileStoreFormat;

    private String feedStoreDir;


    @Pointcut("execution(@oahu.annotations.StoreTxtPage * *(..))")
    public void storeTxtPagePointcut() {
    }

    @Pointcut("execution(@oahu.annotations.StoreHtmlPage * *(..))")
    public void storeHtmlPagePointcut() {
    }

    private void htmlPage2file(HtmlPage pg, ProceedingJoinPoint jp, String storeFormat) {
        //HtmlPage pg = (HtmlPage)result;

        String fn = fileNameFor(jp,storeFormat);

        log.info(String.format("Downloaded: %s, saving to: %s", pg.getUrl(), fn));

        File out = new File(fn);
        try (FileOutputStream fop = new FileOutputStream(out)) {

            // if file doesn't exists, then create it
            if (!out.exists()) {
                out.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = null;

            switch (storeFormat) {
                case "html": contentInBytes = pg.getWebResponse().getContentAsString().getBytes();
                    break;
                case "xml": contentInBytes = pg.asXml().getBytes();
                    break;
                case "txt": contentInBytes = pg.asText().getBytes();
                    break;
            }

            if (contentInBytes == null) {
                log.warn(String.format("Could not get bytes from %s with the %s extension",pg.getUrl(), getHtmlFileStoreFormat()));
            }
            else {
                fop.write(contentInBytes);
                fop.flush();
                fop.close();
            }
        }
        catch (IOException e) {
            log.warn(String.format("Could not save: %s", fn));
        }

    }

    private void textPage2file(TextPage pg, ProceedingJoinPoint jp) {
        String fn = fileNameFor(jp,"txt",getFeedStoreDir());

        log.info(String.format("Downloaded: %s, saving to: %s", pg.getUrl(), fn));

        File out = new File(fn);
        try (FileOutputStream fop = new FileOutputStream(out)) {

            // if file doesn't exists, then create it
            if (!out.exists()) {
                out.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = null;

            contentInBytes = pg.getWebResponse().getContentAsString().getBytes();

            if (contentInBytes == null) {
                log.warn(String.format("Could not get bytes from %s with the %s extension",pg.getUrl(),"txt"));
            }
            else {
                fop.write(contentInBytes);
                fop.flush();
                fop.close();
            }
        }
        catch (IOException e) {
            log.warn(String.format("Could not save: %s", fn));
        }
    }

    @Around("storeTxtPagePointcut()")
    public Object store2txtPointcutMethod(ProceedingJoinPoint jp) throws Throwable {
        Object result = jp.proceed();
        textPage2file((TextPage)result, jp);
        return result;
    }

    @Around("storeHtmlPagePointcut()")
    public Object store2htmlPointcutMethod(ProceedingJoinPoint jp) throws Throwable {
        Object result = jp.proceed();
        htmlPage2file((HtmlPage)result,jp,"html");
        /*
        int storeFormatInt = storeFormat.storeFormat();
        switch (storeFormatInt) {
            case StoreHtmlPage.HTML:
                log.info(String.format("Store will be performed for type: %d (html)", storeFormat));
                break;
            case StoreHtmlPage.XML:
                htmlPage2file((HtmlPage)result,jp,"xml");
                log.info(String.format("Store will be performed for type: %d (xml)", storeFormat));
                break;
            case StoreHtmlPage.TXT:
                textPage2file((TextPage)result,jp);
                log.info(String.format("Store will be performed for type: %d (txt)", storeFormat));
                break;
            default:
                log.warn(String.format("No store will be performed for type %d!", storeFormat));
                break;
        }
        */
        return result;
    }

    //region Private Methods
    private String fileNameFor(ProceedingJoinPoint jp, String storeFormat)  {
        return  fileNameFor(jp, storeFormat, getHtmlFileStoreDir());
    }

    private String fileNameFor(ProceedingJoinPoint jp, String storeFormat, String storeDir) {
        Object[] args = jp.getArgs();
        return String.format("%s/%s.%s", storeDir, args[0], storeFormat);
    }
    //endregion Private Methods

    //region Properties
    public String getHtmlFileStoreDir() {
        return htmlFileStoreDir;
    }

    public void setHtmlFileStoreDir(String htmlFileStoreDir) {
        this.htmlFileStoreDir = htmlFileStoreDir;
    }


    public String getHtmlFileStoreFormat() {
        return htmlFileStoreFormat;
    }

    public void setHtmlFileStoreFormat(String htmlFileStoreFormat) {
        this.htmlFileStoreFormat = htmlFileStoreFormat;
    }

    public String getFeedStoreDir() {
        return feedStoreDir;
    }

    public void setFeedStoreDir(String feedStoreDir) {
        this.feedStoreDir = feedStoreDir;
    }
    //endregion Properties



}
