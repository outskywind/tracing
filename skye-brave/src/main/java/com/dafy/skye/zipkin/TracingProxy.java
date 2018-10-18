package com.dafy.skye.zipkin;

import brave.Tracer;
import brave.Tracing;
import com.dafy.skye.util.IDGenerator;
//import org.springframework.cglib.proxy.*;
// do not couple with spring framework at this low tier
import javassist.*;
import net.sf.cglib.proxy.*;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 * 拦截 Tracing 的 tracer() 返回自定义的 tracer
 * 自定义的tracer 完成ID的自定义生成
 * Created by quanchengyun on 2018/10/16.
 */
public class TracingProxy {

    public static Tracing getProxy(String serviceName, Reporter<Span> reporter){
        //字节码是框架中可用的最后的手段，设计模式优先
        try{
            TracingProxy.generate("brave.Tracer",TracingProxy.class.getClassLoader());
        }catch (Exception ex){
            //...nothing to do
        }
        Tracing target =  Tracing.newBuilder().localServiceName(serviceName).spanReporter(reporter).build();
        try{
            Field tracer = target.getClass().getDeclaredField("tracer");
            tracer.setAccessible(true);
            tracer.set(target, proxyTracer(target.tracer()));
        }catch (Exception e){
            //nothing to do
        }
        return target;
    }

    /**
     * 首先生成新的字节码类，并加载到ClassLoader中
     * 注意此方法必须在该类被隐式加载即 new 之前调用
     * @param clazz
     * @return
     */
    public static Class generate(String clazz,ClassLoader loader) throws Exception{
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get(clazz);
        cc.addConstructor(CtNewConstructor.defaultConstructor(cc));
        return cc.toClass(loader,null);
    }


    /**
     * @param origin
     * @return
     * @throws Exception
     */
    protected static  Tracer proxyTracer(Tracer origin) throws Exception{
        Enhancer enhancer=new Enhancer();
        enhancer.setSuperclass(Tracer.class);
        CallbackFilter filter=new TracerCallbackFilter();
        enhancer.setCallbackFilter(filter);
        enhancer.setCallbacks(new Callback[]{NoOp.INSTANCE,new Tracer$nextContext()});
        //只能自定义生成的继承类的字节码，不会修改父类的字节码
        /*enhancer.setStrategy(new DefaultGeneratorStrategy(){
            protected byte[] transform(byte[] b) {
                // do something with bytes here
                return b;
            }
            protected ClassGenerator transform(ClassGenerator cg) throws Exception{
                return new TransformingClassGenerator(cg,
                        new AddInitTransformer(TracingProxy.class.getDeclaredMethod("init",new Class[]{Object.class})));
            }
        });*/
        //构造时会调用父类的构造函数，所以修改了父类字节码添加了一个默认构造器
        try{
            Tracer proxy =  (Tracer)enhancer.create();
            copyFields(Tracer.class,origin,proxy);
            return proxy;
        }catch(Exception e){
            e.printStackTrace();
            return origin;
        }
    }


    protected static <T> void copyFields(Class<T> clazz,T origin ,T target) throws Exception{
        Field[] fields = clazz.getDeclaredFields();
        for(Field f : fields){
            f.setAccessible(true);
            f.set(target,f.get(origin));
        }
    }

    public static  class TracerCallbackFilter implements CallbackFilter {
        @Override
        public int accept(Method method) {
            if("nextContext".equals(method.getName()) && method.getParameterTypes().length==5){
                return 1;
            }
            return 0;
        }
    }

    public static  class Tracer$nextContext implements MethodInterceptor {

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            long traceId = (long)args[2];
            if(traceId==0L){
                args[2] = IDGenerator.getId();
                args[1] = args[2];
            }
            return methodProxy.invokeSuper(obj,args);
        }
    }
}
