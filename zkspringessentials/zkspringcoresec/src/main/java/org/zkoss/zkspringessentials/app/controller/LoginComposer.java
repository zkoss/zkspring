package org.zkoss.zkspringessentials.app.controller;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.*;
import org.zkoss.zul.*;

import java.util.List;

public class LoginComposer extends SelectorComposer {

    @Wire
    private Textbox u;
    @Wire
    private Textbox p;

    @Listen("onClick = .autofill")
    public void autoFillCredentials(Event event){
        List<Component> children = event.getTarget().getParent().getChildren();
        String userName = ((Label)children.get(0)).getValue();
        String password = ((Label)children.get(1)).getValue();
        u.setValue(userName);
        p.setValue(password);
    }
}
