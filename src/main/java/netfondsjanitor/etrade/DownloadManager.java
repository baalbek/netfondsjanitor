package netfondsjanitor.etrade;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Created by rcs on 16.07.2014.
 */
public interface DownloadManager {
    HtmlPage getLastDownloaded(String ticker);
}
