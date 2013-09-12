package netfondsjanitor.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 9/11/13
 * Time: 11:29 PM
 */
public class EtradeJanitorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EtradeJanitorException() {
        super();
    }

    public EtradeJanitorException(String msg) {
        super(msg);
    }
}
