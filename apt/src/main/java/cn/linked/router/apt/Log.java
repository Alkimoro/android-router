package cn.linked.router.apt;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

public class Log {
    private Messager msg;

    public Log(Messager messager) {
        msg = messager;
    }

    private boolean isNotEmpty(String info){
        if(info!=null&&!"".equals(info.replaceFirst("\\s*",""))){
            return true;
        }
        return false;
    }

    public void i(String info) {
        if (isNotEmpty(info)) {
            msg.printMessage(Diagnostic.Kind.NOTE, info);
        }
    }

    public void e(String error) {
        if (isNotEmpty(error)) {
            msg.printMessage(Diagnostic.Kind.ERROR, error);
        }
    }

    public void w(String warning) {
        if (isNotEmpty(warning)) {
            msg.printMessage(Diagnostic.Kind.WARNING, warning);
        }
    }
}
