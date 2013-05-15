package netfondsjanitor.aspects;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

    @Pointcut("execution(* oahu.financial.EtradeDownloader.download*(String))")
    public void downloadPointcut() {
    }

    /*
    @AfterReturning(pointcut = "downloadPointcut()", returning="retVal")
    public void downloadPointcutMethod(JoinPoint jp, Object retval) {
        System.out.println(retval);
    }
    */

    //@Around("@annotation(com.x.y.MethodExecutionTime)")

    @Around("downloadPointcut()")
    public Object tracePointcutMethod(ProceedingJoinPoint jp) throws Throwable {
        Object result = jp.proceed();

        HtmlPage pg = (HtmlPage)result;

        String fn = fileNameFor(jp);

        log.info(String.format("Downloaded: %s, saving to: %s", pg.getUrl(), fn));

        File out = new File(fn);
        try (FileOutputStream fop = new FileOutputStream(out)) {

            // if file doesn't exists, then create it
            if (!out.exists()) {
                out.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = null;

            switch (getFileStoreFormat()) {
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

        return result;
    }

    //region Private Methods
    private String fileNameFor(ProceedingJoinPoint jp) {
        Object[] args = jp.getArgs();
        return String.format("%s/%sy.%s", getFileStoreDir(), args[0], getFileStoreFormat());
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
