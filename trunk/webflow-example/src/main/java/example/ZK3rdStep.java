/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 *
 * @author mschroen
 */
@Service("ZK3rdStep")
@Scope("prototype")
public class ZK3rdStep extends SelectorComposer<Component> {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ZK3rdStep.class.getName());
    @WireVariable
    private FlowHelper impHelper;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        if (impHelper != null) {
            logger.info("impHelper.getMyint() = " + impHelper.getMyint());
            logger.info("impHelper.getMyList().size() = " + impHelper.getMyList().size());
            logger.info("impHelper.isMybool() = " + impHelper.isMybool());
        } else {
            logger.error("can't resolve impHelper");
        }
    }
}
