package cn.linked.router.common;

public class MateData {
    private String path;
    private String fullClassName;

    private MateData(){
    }

    public static MateData build(String path,String fullClassName){
        MateData obj=new MateData();
        obj.path=path;
        obj.fullClassName=fullClassName;
        return obj;
    }

    public String getFullClassName(){
        return fullClassName;
    }

    public String getPath(){
        return path;
    }

    @Override
    public String toString() {
        return "[ path="+path+";class="+fullClassName+" ]";
    }
}
