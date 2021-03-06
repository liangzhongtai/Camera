package com.chinamobile.gdwy;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by liangzhongtai on 2018/5/21.
 * 方向传感器
 * 注释部分为ios算法：通过磁场向量和加速度获取的方向角度。
 */

public class SensorUtil {
    private volatile static SensorUtil uniqueInstance;
    private SensorManager sensorManager;
    //private Sensor sensor;
    private Sensor aSensor;
    private Sensor mSensor;
    private Context mContext;
    public static CameraService.SensorListener listener;

    private SensorUtil(Context context) {
        mContext = context;
        getAngle();
    }

    //采用Double CheckLock(DCL)实现单例
    public static SensorUtil getInstance(Context context) {
        if (uniqueInstance == null) {
            synchronized (SensorUtil.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new SensorUtil(context);
                }
            }
        }
        return uniqueInstance;
    }

    private void getAngle(){
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        //sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //sensorManager.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        aSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor=sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(mSensorEventListener, aSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        LogUtil.d(Camera.TAG,"方向传感器初始化完毕");
    }

    public int showAngle(){
        LogUtil.d(Camera.TAG,"拍照的角度="+angle);
        return angle;
    }

    //恢复方向传感器监听
    public void start(){
        if(sensorManager!=null){
            sensorManager.registerListener(mSensorEventListener, aSensor, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        distance = System.currentTimeMillis();
        //sensorManager.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    //停止方向传感器监听
    public void stop(){
        if(sensorManager!=null) {
            sensorManager.unregisterListener(mSensorEventListener);
        }
    }

    //移除传感器监听
    public void removeSensorListener(){
        if(sensorManager!=null){
            uniqueInstance = null;
            //sensorManager.unregisterListener(mSensorEventListener,sensor);
            sensorManager.unregisterListener(mSensorEventListener,aSensor);
            sensorManager.unregisterListener(mSensorEventListener,mSensor);
        }
    }

    //角度,手机头部向前，背部向下，与地面水平，则为0度,手机头部向上，与地面垂直，则为90度，手机头部
    //向内，背部向上，与地面水平，则为180度，反正角度为负。
    private int angle;
    //罗盘方向：具体参照MD文档说明
    public float x;
    public float y;
    public float z;

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float[] values = new float[3];
    private float[] rotate = new float[9];

    private static long distance;
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            //Log.d(Camera.TAG,"方向传感器1");
            long nowTime = System.currentTimeMillis();
            if(nowTime-distance<100){
                return;
            }
            distance = nowTime;
            //Log.d(Camera.TAG,"方向传感器2="+sensorEvent.sensor.getType());
            if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                accelerometerValues=sensorEvent.values;
            }
            if(sensorEvent.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
                magneticFieldValues=sensorEvent.values;
            }

            SensorManager.getRotationMatrix(rotate, null, accelerometerValues, magneticFieldValues);
            SensorManager.getOrientation(rotate, values);
            //Log.d(Camera.TAG,"方向传感器3");
            //经过SensorManager.getOrientation(rotate, values);得到的values值为弧度
            //转换为角度
            //-180~180转成0-360
            //Log.d(Camera.TAG,"方向传感器4="+ Arrays.toString(values));
            x = values[0]=(float)Math.toDegrees(values[0])/*+180*/;
            if(x<0){
                x = x+360;
            }
            y = values[1]=(float)Math.toDegrees(values[1]);
            z = values[2]=(float)Math.toDegrees(values[2]);
            angle = -(int)y;
            //Log.d(Camera.TAG,"方向传感器5="+listener);
            if(listener!=null){
                //Log.d(Camera.TAG,"方向传感器6="+x);
                listener.sendSemsor(x,y,z);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

}
