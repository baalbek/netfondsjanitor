package netfondsjanitor.aspects;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.Page;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import oahu.annotations.StoreHtmlPage;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 5/10/13
 * Time: 10:20 AM
 */

@Aspect
public class DownloadMaintenanceAspect {

    private Logger log = Logger.getLogger(getClass().getPackage().getName());

    private String fileStoreDir;
    private String fileStoreFormat;

    //@Pointcut("execution(* oahu.financial.EtradeDownloader.download*(String))")
    //public void downloadPointcut() {
    //}
    //

    /*
    @AfterReturning(pointcut = "downloadPointcut()", returning="retVal")
    public void downloadPointcutMethod(JoinPoint jp, Object retval) {
        System.out.println(retval);
    }
    */

    //@Around("@annotation(com.x.y.MethodExecutionTime)")

    //@Pointcut("@annotation(oahu.annotations.StoreHtmlPage)")
    @Pointcut("execution(@oahu.annotations.StoreHtmlPage * *(..)) && @annotation(annot)")
    public void storeHtmlPagePointcut(StoreHtmlPage annot) {
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
                log.warn(String.format("Could not get bytes from %s with the %s extension",pg.getUrl(),getFileStoreFormat()));
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
        String fn = fileNameFor(jp,"txt");

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

    @Around("storeHtmlPagePointcut(annot)")
    public Object store2htmlPointcutMethod(ProceedingJoinPoint jp, StoreHtmlPage annot) throws Throwable {
        Object result = jp.proceed();
        //htmlPage2file((Page)result,jp,"txt");
        int storeFormat = annot.storeFormat();
        switch (storeFormat) {
            case StoreHtmlPage.HTML:
            case StoreHtmlPage.XML:
                log.info(String.format("Store will be performed for type: %d", storeFormat));
                break;
            case StoreHtmlPage.TXT:
                textPage2file((TextPage)result,jp);
                log.info(String.format("Store will be performed for type: %d", storeFormat));
                break;
            default:
                log.warn(String.format("No store will be performed for type %d!", storeFormat));
                break;
        }
        return result;
    }

    //region Private Methods
    private String fileNameFor(ProceedingJoinPoint jp, String storeFormat) {
        Object[] args = jp.getArgs();
        return String.format("%s/%sy.%s", getFileStoreDir(), args[0], storeFormat);
    }
    //endregion Private Methods

    //region Properties
    public String getFileStoreDir() {
        return fileStoreDir;
    }

    public void setFileStoreDir(String fileStoreDir) {
        this.fileStoreDir = fileStoreDir;
    }


    public String getFileStoreFormat() {
        return fileStoreFormat;
    }

    public void setFileStoreFormat(String fileStoreFormat) {
        this.fileStoreFormat = fileStoreFormat;
    }
    //endregion Properties



}
