package cn.linked.router.apt;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import cn.linked.router.common.Const;
import cn.linked.router.common.MateData;
import cn.linked.router.common.Route;
import cn.linked.router.common.RouteMappingLoader;

// 谷歌的帮助我们快速实现注解处理器
// 注册自定义的Processor类
@AutoService(Processor.class)
// 设置由该Processor处理的注解
@SupportedAnnotationTypes({"cn.linked.router.common.Route"})
// Java版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RouterProcessor extends AbstractProcessor {
    // 文件相关辅助类
    private Filer mFiler;
    // 元素相关辅助类
    private Elements mElements;
    // 日志相关辅助类
    private Log log;
    // 模块索引
    private int moduleIndex;
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations!=null&&annotations.size()>0) {

            try {
                initFields();
            }catch (Exception e) {
                return false;
            }

            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Route.class);

            // 创建参数类型 Map<String,MateData>
            ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(MateData.class));
            // 生成参数 Map<String,MateData> map
            ParameterSpec parameter = ParameterSpec.builder(parameterizedTypeName, "map").build();

            // 生成函数 public void loadInto(Map<String,MateData> map)
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("loadInto")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(parameter);

            // 生成函数体
            for (Element ele : elements) {
                String path=ele.getAnnotation(Route.class).path();
                TypeName className=ClassName.get(ele.asType());
                methodBuilder.addStatement("map.put($S,$T.build($S,$S))",path,MateData.class,path,className);
            }

            String simpleClassName=Const.GENERATE_FILE_PREFIX+moduleIndex;
            TypeSpec clazz = TypeSpec.classBuilder(simpleClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(RouteMappingLoader.class)
                    .addMethod(methodBuilder.build())
                    .build();

            JavaFile javaFile = JavaFile.builder(Const.PACKAGE_PATH, clazz)
                    .build();

            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private void initFields(){
        mFiler=processingEnv.getFiler();
        mElements=processingEnv.getElementUtils();
        log=new Log(processingEnv.getMessager());
        moduleIndex=Integer.parseInt(processingEnv.getOptions().get("MODULE_INDEX"));
    }
}