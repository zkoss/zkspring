package example.simple;

import java.util.ArrayList;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Panel;

import example.bean.FlowHelper;

public class FirstStateComposer extends SelectorComposer<Component> {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(FirstStateComposer.class.getName());
    @WireVariable
    private FlowHelper helper;
    
    
    @Listen("onClick = #add")
    public void add(){
    	helper.getMyList().add("test");
    }
}
