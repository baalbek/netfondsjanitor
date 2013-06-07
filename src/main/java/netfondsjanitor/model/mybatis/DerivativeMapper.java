package netfondsjanitor.model.mybatis;

import oahu.financial.Derivative;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 4/18/13
 * Time: 8:52 PM
 */

public interface DerivativeMapper {
    void insertDerivative(Derivative d);
}
