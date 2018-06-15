package com.conways.hooklogin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 通过动态代理的方式实现登录跳转页面
 */

class HookManager {
    private static HookManager ourInstance = null;
    private Context context;

    static HookManager getInstance(Context context) {
        if (null == ourInstance) {
            ourInstance = new HookManager(context);
        }
        return ourInstance;
    }


    private boolean isLogin = true;

    private HookManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void startHook() {
        try {

            Class<?> ActivityManagerNativecls = Class.forName("android.app.ActivityManagerNative");
            Field gDefault = ActivityManagerNativecls.getDeclaredField("gDefault");

            Method m;
            gDefault.setAccessible(true);
            Object defaultValue = gDefault.get(null);

            Class<?> SingletonClass = Class.forName("android.util.Singleton");
            Field mInstance = SingletonClass.getDeclaredField("mInstance");
            mInstance.setAccessible(true);


            Object iActivityManagerObject = mInstance.get(defaultValue);

            Class<?> IActivityManagerIntercept = Class.forName("android.app.IActivityManager");
            Object iActivityManagerProxy = Proxy.newProxyInstance(Thread.currentThread()
                    .getContextClassLoader(), new Class[]{IActivityManagerIntercept}, new
                    StartActivityInvocationHandle(iActivityManagerObject));

            mInstance.set(defaultValue, iActivityManagerProxy);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    class StartActivityInvocationHandle implements InvocationHandler {

        private Object iActivityManagerObject;

        public StartActivityInvocationHandle(Object iActivityManagerObject) {
            this.iActivityManagerObject = iActivityManagerObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            Log.d("zzzzzzzzzz", "invoke: 登录");
            Intent intent = null;
            int index = 0;
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Intent) {
                    intent = (Intent) args[i];
                    index = i;
                }
            }

            if (null != intent) {
                if (isLogin) {
                    Log.d("zzzzzzzzzz", "invoke: 登录");
                    return method.invoke(iActivityManagerObject, args);
                } else {
                    Log.d("zzzzzzzzzz", "invoke: 没有登录");
                    ComponentName componentName = new ComponentName(context, LoginActivity.class);
                    Intent intent1 = new Intent();
                    intent1.putExtra("extraIntent", intent.getComponent().getClassName());
                    intent1.setComponent(componentName);
                    args[index] = intent1;
                    return method.invoke(iActivityManagerObject, args);
                }
            }

            return method.invoke(iActivityManagerObject, args);
        }
    }


}
