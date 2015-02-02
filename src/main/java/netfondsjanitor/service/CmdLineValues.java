package netfondsjanitor.service;

import oahu.financial.janitors.JanitorContext;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;

public class CmdLineValues implements JanitorContext {
    /*@Argument(required = true, index = 1, metaVar = "XML",
            usage = "Spring XML file name")*/

    @Option(name = "-x", aliases = { "--xml" }, required = false, usage = "Spring XML file name" )
    private String xml;

    @Option(name = "-t", aliases = { "--tickers" }, required = false, usage = "Manually supplied Stock Tickers (comma-separated)")
    private String tickers;

    @Option(name = "-o", aliases = { "--open" }, required = false, usage = "Opening time for the market (hh:mm). Default: 9:30")
    private String open;

    @Option(name = "-c", aliases = { "--close" }, required = false, usage = "Closing time for the market (hh:mm). Default: 17:20")
    private String close;

    @Option(name = "-p", aliases = { "--paper" }, required = false, usage = "Download paper history" )
    private boolean paperHistory;

    @Option(name = "-f", aliases = { "--feed" }, required = false, usage = "Update stockprices from feed" )
    private boolean feed;

    @Option(name = "-s", aliases = { "--spot" }, required = false, usage = "Update todays stockprices" )
    private boolean spot;

    @Option(name = "-i", aliases = { "--ivharvest" }, required = false, usage = "Implied volatility ivHarvest" )
    private boolean ivHarvest;

    @Option(name = "-A", aliases = { "--ivharvestFrom" }, required = false, usage = "Implied volatility ivHarvest start date (yyyy-mm-dd)" )
    private String ivHarvestFrom;

    @Option(name = "-B", aliases = { "--ivharvestTo" }, required = false, usage = "Implied volatility ivHarvest end date (yyyy-mm-dd)" )
    private String ivHarvestTo;

    @Option(name = "-e", aliases = { "--test-run" }, required = false, usage = "Harvest test run" )
    private boolean harvestTestRun;

    @Option(name = "-q", aliases = { "--query" }, required = false, usage = "Show active tickers and quit" )
    private boolean query;


    @Option(name = "-U", aliases = { "--upd-options" }, required = false, usage = "Update database with new options")
    private boolean updateDbOptions;

    @Option(name = "-O", aliases = { "--one-time" }, required = false, usage = "One-time download options" )
    private boolean oneTimeDownloadOptions;

    @Option(name = "-S", aliases = { "--spot-dl-opx" }, required = false, usage = "Update spots in database from downloaded derivatives (--one-time)" )
    private boolean spotFromDownloadedOptions;

    @Option(name = "-R", aliases = { "--rolling" }, required = false, usage = "Rolling download of options" )
    private boolean rollingOptions;

    @Option(name = "-T", aliases = { "--rolling-interval" }, required = false, usage = "Rolling download time interval in minutes. Default: 30" )
    private int rollingInterval = 30;

    @Option(name = "-h", aliases = { "--help" }, required = false, usage = "Print usage and quit" )
    private boolean help;


    public CmdLineValues(String... args) throws CmdLineException  {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(80);
        parser.parseArgument(args);

        if (help == true) {
            parser.printUsage(System.err);
            System.exit(0);
        }

        /*if (getXml() == null) throw new CmdLineException(parser, "Spring XML not set.");*/

        if (xml == null) {
            xml = "netfondsjanitor.xml";
        }
        if (open == null) {
            open = "9:30";
        }
        if (close == null) {
            close = "17:20";
        }
    }



    //-------------------------------------------------
    //-------------- Interface JanitorContext ---------
    //-------------------------------------------------
    @Override
    public String getXml() {
        return xml;
    }

    @Override
    public boolean isUpdateDbOptions() {
        return updateDbOptions;
    }

    @Override
    public List<String> getTickers() {
        if (tickers == null) return null;
        return Arrays.asList(tickers.split(","));
    }

    @Override
    public boolean isPaperHistory() {
        return paperHistory;
    }

    @Override
    public boolean isFeed() {
        return feed;
    }

    @Override
    public String getOpen() {
        return open;
    }

    @Override
    public String getClose() {
        return close;
    }

    @Override
    public boolean isSpot() {
        return spot;
    }

    @Override
    public boolean isQuery() {
        return query;
    }

    @Override
    public boolean isRollingOptions() {
        return rollingOptions;
    }
    @Override
    public int getRollingInterval() {
        return rollingInterval;
    }

    @Override
    public boolean isOneTimeDownloadOptions() {
        return oneTimeDownloadOptions;
    }

    @Override
    public boolean isSpotFromDownloadedOptions() {
        return spotFromDownloadedOptions;
    }

    @Override
    public boolean isHarvestTestRun() {
        return harvestTestRun;
    }

    @Override
    public boolean isIvHarvest() {
        return ivHarvest;
    }
    @Override
    public String ivHarvestFrom() {
        return ivHarvestFrom;
    }
    @Override
    public String ivHarvestTo() {
        return ivHarvestTo;
    }

    @Override
    public String toString() {
        return String.format("xml: %s",getXml());
    }
}
