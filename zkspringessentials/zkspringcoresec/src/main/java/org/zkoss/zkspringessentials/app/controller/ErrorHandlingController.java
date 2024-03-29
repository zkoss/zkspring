package org.zkoss.zkspringessentials.app.controller;

import org.springframework.security.access.AccessDeniedException;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import java.util.Arrays;

public class ErrorHandlingController extends SelectorComposer {
    @Wire
    private Window errorModal;

    @Wire
    private Label errorMessage;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Execution exec = Executions.getCurrent();
        Throwable exception = (Throwable) exec.getAttribute("javax.servlet.error.exception");
        if (exception.getCause().getCause() instanceof AccessDeniedException){
            errorModal.setTitle("Access Denied");
            errorMessage.setValue("Ajax Access Denied");
        }else{
            errorModal.setTitle(exception.getMessage());
            errorMessage.setValue(Arrays.stream(exception.getStackTrace()).toString());
        }
    }
}
