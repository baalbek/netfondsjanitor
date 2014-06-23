package netfondsjanitor.service;

import oahu.financial.janitors.JanitorContext;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.Arrays;
import java.util.List;

public class CmdLineValues implements JanitorContext {
    /*@Argument(required = true, index = 1, metaVar = "XML",
            usage = "Spring XML file name")*/

    @Option(name = "-x", aliases = { "--xml" }, required = false, usage = "Spring XML file name" )
    private String xml;

    @Option(name = "-t", aliases = { "--tickers" }, required = false, usage = "Stock tickers" )
    private String tickers;

    @Option(name = "-p", aliases = { "--paper" }, required = false, usage = "Download paper history" )
    private boolean paperHistory;

    @Option(name = "-f", aliases = { "--feed" }, required = false, usage = "Update stockprices from feed" )
    private boolean feed;

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
    }

    public String getXml() {
        return xml;
    }

    public List<String> getTickers() {
        if (tickers == null) return null;
        return Arrays.asList(tickers.split(","));

    }

    //-------------------------------------------------
    //-------------- Interface JanitorContext ---------
    //-------------------------------------------------
    @Override
    public boolean isPaperHistory() {
        return paperHistory;
    }

    @Override
    public boolean isFeed() {
        return feed;
    }
}
