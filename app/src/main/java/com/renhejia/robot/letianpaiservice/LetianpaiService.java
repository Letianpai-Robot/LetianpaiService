package com.renhejia.robot.letianpaiservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.letianpai.robot.letianpaiservice.LtpAppCmdCallback;
import com.letianpai.robot.letianpaiservice.LtpAudioEffectCallback;
import com.letianpai.robot.letianpaiservice.LtpBleCallback;
import com.letianpai.robot.letianpaiservice.LtpBleResponseCallback;
import com.letianpai.robot.letianpaiservice.LtpExpressionCallback;
import com.letianpai.robot.letianpaiservice.LtpIdentifyCmdCallback;
import com.letianpai.robot.letianpaiservice.LtpLongConnectCallback;
import com.letianpai.robot.letianpaiservice.LtpMcuCommandCallback;
import com.letianpai.robot.letianpaiservice.LtpMiCmdCallback;
import com.letianpai.robot.letianpaiservice.LtpRobotStatusCallback;
import com.letianpai.robot.letianpaiservice.LtpSensorResponseCallback;
import com.letianpai.robot.letianpaiservice.LtpSpeechCallback;
import com.letianpai.robot.letianpaiservice.LtpTTSCallback;
import com.renhejia.robot.letianpaiservice.consts.RobotAidlConsts;
import com.renhejia.robot.letianpaiservice.parser.CmdInfo;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author liujunbin
 */
public class LetianpaiService extends Service {
    private static final String TAG = "LetianpaiService";
    private int robotStatus = 0;
    private int ROBOT_OTA_STATUS = 1;
    private final RemoteCallbackList<LtpCommandCallback> RemoteCallbackList = new RemoteCallbackList<>();
    private final RemoteCallbackList<LtpLongConnectCallback> ltpLongConnectCallback = new RemoteCallbackList<>();
    private final RemoteCallbackList<LtpMcuCommandCallback> ltpMcuCommandCallback = new RemoteCallbackList<>();
    //
    private final RemoteCallbackList<LtpAudioEffectCallback> ltpAudioEffectCallback = new RemoteCallbackList<>();
    private final RemoteCallbackList<LtpExpressionCallback> ltpExpressionCallback = new RemoteCallbackList<>();
    private final RemoteCallbackList<LtpAppCmdCallback> ltpAppCmdCallback = new RemoteCallbackList<>();
    private final RemoteCallbackList<LtpRobotStatusCallback> ltpRobotStatusCallback = new RemoteCallbackList<>();
    private final RemoteCallbackList<LtpTTSCallback> ltpTTSCallback = new RemoteCallbackList<>();
    private final RemoteCallbackList<LtpSpeechCallback> ltpSpeechCallback = new RemoteCallbackList<>();
    private final RemoteCallbackList<LtpSensorResponseCallback> ltpSensorResponseCallback = new RemoteCallbackList<>();
    private final RemoteCallbackList<LtpMiCmdCallback> ltpMiCmdCallback = new RemoteCallbackList<>();
    private final RemoteCallbackList<LtpIdentifyCmdCallback> ltpIdentifyCmdCallback = new RemoteCallbackList<>();
    private final RemoteCallbackList<LtpBleCallback> ltpBleCallback = new RemoteCallbackList<>();
    private final RemoteCallbackList<LtpBleResponseCallback> ltpBleResponseCallback = new RemoteCallbackList<>();

    private RobotLongConnectHandler robotLongConnectHandler;
    private RobotMcuHandler robotMcuHandler;
    private RobotAudioEffectHandler robotAudioEffectHandler;
    private RobotExpressionHandler robotExpressionHandler;
    private RobotAppCmdHandler robotAppCmdHandler;
    private RobotRobotStatusHandler robotRobotStatusHandler;
    private RobotTTSHandler robotTTSHandler;
    private RobotSpeechCmdHandler robotSpeechCmdHandler;
    private RobotSensorHandler robotSensorHandler;
    private RobotMiHandler robotMiHandler;
    private RobotIdentifyHandler robotIdentifyHandler;
    private RobotBleHandler robotBleHandler;
    private RobotBleResponseHandler robotBleResponseHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iLetianpaiService;
    }

    public LetianpaiService() {
        initHandler();
    }

    private void initHandler() {
        robotLongConnectHandler = new RobotLongConnectHandler(LetianpaiService.this);
        robotMcuHandler = new RobotMcuHandler(LetianpaiService.this);
        robotAudioEffectHandler = new RobotAudioEffectHandler(LetianpaiService.this);
        robotExpressionHandler = new RobotExpressionHandler(LetianpaiService.this);
        robotAppCmdHandler = new RobotAppCmdHandler(LetianpaiService.this);
        robotRobotStatusHandler = new RobotRobotStatusHandler(LetianpaiService.this);
        robotTTSHandler = new RobotTTSHandler(LetianpaiService.this);
        robotSpeechCmdHandler = new RobotSpeechCmdHandler(LetianpaiService.this);
        robotSensorHandler = new RobotSensorHandler(LetianpaiService.this);
        robotMiHandler = new RobotMiHandler(LetianpaiService.this);
        robotIdentifyHandler = new RobotIdentifyHandler(LetianpaiService.this);
        robotBleHandler = new RobotBleHandler(LetianpaiService.this);
        robotBleResponseHandler = new RobotBleResponseHandler(LetianpaiService.this);

    }

    private final Lock mLock = new ReentrantLock();
    private final Lock mMcuLock = new ReentrantLock();
    private final Lock mAeLock = new ReentrantLock();
    private final Lock mAppLock = new ReentrantLock();
    private final Lock mExpressionLock = new ReentrantLock();
    private final Lock mLongConnectLock = new ReentrantLock();
    private final Lock mStatusLock = new ReentrantLock();
    private final Lock mTTSLock = new ReentrantLock();
    private final Lock mSpeechSLock = new ReentrantLock();
    private final Lock mMiLock = new ReentrantLock();
    private final Lock mIdentifyLock = new ReentrantLock();
    private final Lock mSensorLock = new ReentrantLock();
    private final Lock mBleLock = new ReentrantLock();
    private final Lock mBleResponseLock = new ReentrantLock();

    private final ILetianpaiService.Stub iLetianpaiService = new ILetianpaiService.Stub() {
        @Override
        public int getRobotStatus() throws RemoteException {
            return robotStatus;
        }

        @Override
        public void setCommand(LtpCommand ltpCommand) throws RemoteException {
            if (ltpCommand == null) {
                Log.i("letianpai_server", "ltpCommand is null");
            } else {
                Log.i("letianpai_server", "ltpCommand is not null");
            }
            if (robotStatus == ROBOT_OTA_STATUS) {

            } else {
                responseCommand(ltpCommand);
            }
        }

        @Override
        public void setRobotStatus(int status) throws RemoteException {
            robotStatus = status;
            setLTPRobotStatus(robotStatus);
            // TODO 分发命令回调
        }

        @Override
        public void registerCallback(LtpCommandCallback cc) throws RemoteException {
            RemoteCallbackList.register(cc);
        }

        @Override
        public void unregisterCallback(LtpCommandCallback cc) throws RemoteException {
            RemoteCallbackList.unregister(cc);
        }

        @Override
        public void setLongConnectCommand(String command, String data) throws RemoteException {
//            responseLongConnectCommand(command, data);
            sendLongConnectCmd(command,data);

        }

        @Override
        public void registerLCCallback(LtpLongConnectCallback lcCallback) throws RemoteException {
            ltpLongConnectCallback.register(lcCallback);
        }

        @Override
        public void unregisterLCCallback(LtpLongConnectCallback lcCallback) throws RemoteException {
            ltpLongConnectCallback.unregister(lcCallback);
        }


        @Override
        public void setMcuCommand(String command, String data) throws RemoteException {
            sendMcuCmd(command,data);
        }

        @Override
        public void registerMcuCmdCallback(LtpMcuCommandCallback mcuCallback) throws RemoteException {
            ltpMcuCommandCallback.register(mcuCallback);
        }

        @Override
        public void unregisterMcuCmdCallback(LtpMcuCommandCallback mcuCallback) throws RemoteException {
            ltpMcuCommandCallback.unregister(mcuCallback);
        }

        @Override
        public void setAudioEffect(String command, String data) throws RemoteException {
            sendAudioEffectCmd(command, data);

        }

        @Override
        public void registerAudioEffectCallback(LtpAudioEffectCallback aeCallback) throws RemoteException {
            ltpAudioEffectCallback.register(aeCallback);

        }

        @Override
        public void unregisterAudioEffectCallback(LtpAudioEffectCallback aeCallback) throws RemoteException {
            ltpAudioEffectCallback.unregister(aeCallback);
        }

        @Override
        public void setExpression(String command, String data) throws RemoteException {
            sendExpressionCmd(command, data);
        }

        @Override
        public void registerExpressionCallback(LtpExpressionCallback expressionCallback) throws RemoteException {
            ltpExpressionCallback.register(expressionCallback);
        }

        @Override
        public void unregisterExpressionCallback(LtpExpressionCallback expressionCallback) throws RemoteException {
            ltpExpressionCallback.unregister(expressionCallback);
        }

        @Override
        public void setAppCmd(String command, String data) throws RemoteException {
            sendAppCmd(command,data);
        }

        @Override
        public void registerAppCmdCallback(LtpAppCmdCallback appCallback) throws RemoteException {
            ltpAppCmdCallback.register(appCallback);
        }

        @Override
        public void unregisterAppCmdCallback(LtpAppCmdCallback appCallback) throws RemoteException {
            ltpAppCmdCallback.unregister(appCallback);
        }

        @Override
        public void setRobotStatusCmd(String command, String data) throws RemoteException {
            sendRobotStatus(command, data);
        }

        @Override
        public void registerRobotStatusCallback(LtpRobotStatusCallback statusCallback) throws RemoteException {
            ltpRobotStatusCallback.register(statusCallback);

        }

        @Override
        public void unregisterRobotStatusCallback(LtpRobotStatusCallback statusCallback) throws RemoteException {
            ltpRobotStatusCallback.unregister(statusCallback);

        }

        @Override
        public void setTTS(String command, String data) throws RemoteException {
            sendTTS(command,data);
        }

        @Override
        public void registerTTSCallback(LtpTTSCallback ttsCallback) throws RemoteException {
            ltpTTSCallback.register(ttsCallback);
        }

        @Override
        public void unregisterTTSCallback(LtpTTSCallback ttsCallback) throws RemoteException {
            ltpTTSCallback.unregister(ttsCallback);
        }


        @Override
        public void setSpeechCmd(String command, String data) throws RemoteException {
            sendSpeechCmd(command,data);

        }

        @Override
        public void registerSpeechCallback(LtpSpeechCallback speechCallback) throws RemoteException {
            ltpSpeechCallback.register(speechCallback);
        }

        @Override
        public void unregisterSpeechCallback(LtpSpeechCallback speechCallback) throws RemoteException {
            ltpSpeechCallback.unregister(speechCallback);

        }

        @Override
        public void setSensorResponse(String command, String data) throws RemoteException {
            sendSensorCmd(command,data);
        }

        @Override
        public void registerSensorResponseCallback(LtpSensorResponseCallback sensorCallback) throws RemoteException {
            ltpSensorResponseCallback.register(sensorCallback);

        }

        @Override
        public void unregisterSensorResponseCallback(LtpSensorResponseCallback speechCallback) throws RemoteException {
            ltpSensorResponseCallback.unregister(speechCallback);
        }

        @Override
        public void setMiCmd(String command, String data) throws RemoteException {
            sendMiCmd(command,data);
        }

        @Override
        public void registerMiCmdResponseCallback(LtpMiCmdCallback miCmdCallback) throws RemoteException {
            ltpMiCmdCallback.register(miCmdCallback);
        }

        @Override
        public void unregisterMiCmdResponseCallback(LtpMiCmdCallback miCmdCallback) throws RemoteException {
            ltpMiCmdCallback.unregister(miCmdCallback);

        }

        @Override
        public void setIdentifyCmd(String command, String data) throws RemoteException {
            sendIdentifyCmd(command,data);
        }

        @Override
        public void registerIdentifyCmdCallback(LtpIdentifyCmdCallback identifyCmdCallback) throws RemoteException {
            ltpIdentifyCmdCallback.register(identifyCmdCallback);
        }

        @Override
        public void unregisterIdentifyCmdCallback(LtpIdentifyCmdCallback identifyCmdCallback) throws RemoteException {
            ltpIdentifyCmdCallback.unregister(identifyCmdCallback);

        }

        @Override
        public void setBleCmd(String command, String data, boolean isNeedResponse) throws RemoteException {
            sendBleCmd(command,data,isNeedResponse);
        }

        @Override
        public void registerBleCmdCallback(LtpBleCallback bleCallback) throws RemoteException {
            ltpBleCallback.register(bleCallback);
        }

        @Override
        public void unregisterBleCmdCallback(LtpBleCallback bleCallback) throws RemoteException {
            ltpBleCallback.unregister(bleCallback);
        }

        @Override
        public void setBleResponse(String command, String data) throws RemoteException {
            sendBleResponseCmd(command, data);
        }

        @Override
        public void registerBleResponseCallback(LtpBleResponseCallback bleResponseCallback) throws RemoteException {
            ltpBleResponseCallback.register(bleResponseCallback);
        }

        @Override
        public void unregisterBleResponseCmdCallback(LtpBleResponseCallback bleResponseCallback) throws RemoteException {
            ltpBleResponseCallback.unregister(bleResponseCallback);
        }

    };

    private void responseBleResponse(String command, String data) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return;
        }
        mBleResponseLock.lock();
        try {
            int N = ltpBleResponseCallback.beginBroadcast();
            if (N > 0) {
                for (int i = 0; i < N; i++) {
                    try {
                        if (ltpBleResponseCallback.getBroadcastItem(i) != null) {
                            ltpBleResponseCallback.getBroadcastItem(i).onBleCmdResponsReceived(command, data);
                        }
                        Log.e(TAG, "responseBleResponse:=== count:" + N + "  current:" + i);
                    } catch (Exception e) {
                        Log.e(TAG, "responseBleResponse:  异常" + i);
                        e.printStackTrace();
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ltpBleResponseCallback.finishBroadcast();
            mBleResponseLock.unlock();
        }
    }

    private void responseBleCmd(String command, String data, boolean isNeedResponse) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return;
        }
        mBleLock.lock();
        try {
            int N = ltpBleCallback.beginBroadcast();
            if (N > 0) {
                for (int i = 0; i < N; i++) {
                    try {
                        if (ltpBleCallback.getBroadcastItem(i) != null) {
                            ltpBleCallback.getBroadcastItem(i).onBleCmdReceived(command, data, isNeedResponse);
                        }
                        Log.e(TAG, "responseBleCmd:=== count:" + N + "  current:" + i);
                    } catch (Exception e) {
                        Log.e(TAG, "responseBleCmd:  异常" + i);
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ltpBleCallback.finishBroadcast();
            mBleLock.unlock();
        }

    }

    private void responseIdentifyCmd(String command, String data) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return;
        }
        mIdentifyLock.lock();
        try {
            int N = ltpIdentifyCmdCallback.beginBroadcast();
            if (N > 0) {
                for (int i = 0; i < N; i++) {
                    try {
                        if (ltpIdentifyCmdCallback.getBroadcastItem(i) != null) {
                            ltpIdentifyCmdCallback.getBroadcastItem(i).onIdentifyCommandReceived(command, data);
                        }
                        Log.e(TAG, "responseMiCmd:=== count:" + N + "  current:" + i);
                    } catch (Exception e) {
                        Log.e(TAG, "responseMiCmd:  异常" + i);
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ltpIdentifyCmdCallback.finishBroadcast();
            mIdentifyLock.unlock();
        }

    }

    private void responseMiCmd(String command, String data) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return;
        }
        mMiLock.lock();
        try {
            int N = ltpMiCmdCallback.beginBroadcast();
            if (N == 0) {
                return;
            }
            for (int i = 0; i < N; i++) {
                try {
                    if (ltpMiCmdCallback.getBroadcastItem(i) != null) {
                        ltpMiCmdCallback.getBroadcastItem(i).onMiCommandReceived(command, data);
                    }
                    Log.e(TAG, "responseMiCmd:=== count:" + N + "  current:" + i);
                } catch (Exception e) {
                    Log.e(TAG, "responseMiCmd:  异常" + i);
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ltpMiCmdCallback.finishBroadcast();
            mMiLock.unlock();
        }


    }

    private void responseSpeechCmds(String command, String data) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return;
        }
        mSpeechSLock.lock();
        try {
            int N = ltpSpeechCallback.beginBroadcast();
            if (N == 0) {
                return;
            }
            for (int i = 0; i < N; i++) {
                try {
                    if (ltpSpeechCallback.getBroadcastItem(i) != null) {
                        ltpSpeechCallback.getBroadcastItem(i).onSpeechCommandReceived(command, data);
                    }
                    Log.e(TAG, "setSpeechCmds:=== count:" + N + "  current:" + i);
                } catch (Exception e) {
                    Log.e(TAG, "setSpeechCmds:  异常" + i);
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ltpSpeechCallback.finishBroadcast();
            mSpeechSLock.unlock();
        }

    }

    private void responseRobotTTS(String command, String data) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return;
        }
        mTTSLock.lock();
        try {
            int N = ltpTTSCallback.beginBroadcast();
            if (N == 0) {
                return;
            }
            for (int i = 0; i < N; i++) {
                if (ltpTTSCallback.getBroadcastItem(i) != null) {
                    ltpTTSCallback.getBroadcastItem(i).onTTSCommand(command, data);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ltpTTSCallback.finishBroadcast();
            mTTSLock.unlock();
        }

    }

    private void responseRobotStatusCmds(String command, String data) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return;
        }
        mStatusLock.lock();
        try {
            int N = ltpRobotStatusCallback.beginBroadcast();
            if (N == 0) {
                return;
            }
            for (int i = 0; i < N; i++) {
                if (ltpRobotStatusCallback.getBroadcastItem(i) != null) {
                    ltpRobotStatusCallback.getBroadcastItem(i).onRobotStatusChanged(command, data);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ltpRobotStatusCallback.finishBroadcast();
            mStatusLock.unlock();
        }
    }

    private void responseAppCommand(String command, String data) {
        Log.d("-------", "command: " + command);
        Log.d("-------", "data: " + data);
        synchronized (this) {

            if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
                return;
            }
            mAppLock.lock();
            try {
                int N = ltpAppCmdCallback.beginBroadcast();
                if (N > 0) {
                    for (int i = 0; i < N; i++) {
                        Log.d("-------", "--" + N);
                        Log.d("-------", "--" + i);
                        if (ltpAppCmdCallback.getBroadcastItem(i) != null) {
                            ltpAppCmdCallback.getBroadcastItem(i).onAppCommandReceived(command, data);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ltpAppCmdCallback.finishBroadcast();
                mAppLock.unlock();
            }
        }
    }

    private void responseExpressionChange(String command, String data) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return;
        }
        mExpressionLock.lock();
        try {
            int N = ltpExpressionCallback.beginBroadcast();
            if (N == 0) {
                return;
            }
            for (int i = 0; i < N; i++) {
                if (ltpExpressionCallback.getBroadcastItem(i) != null) {
                    ltpExpressionCallback.getBroadcastItem(i).onExpressionChanged(command, data);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ltpExpressionCallback.finishBroadcast();
            mExpressionLock.unlock();
        }

    }

    private void responseAudioEffectCmd(String command, String data) {
        Log.e("letianpai_sound0", "==== lettianpaiservice_2");
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return;
        }
        Log.e("letianpai_sound0", "==== lettianpaiservice_3");
        mAeLock.lock();
        try {
            int N = ltpAudioEffectCallback.beginBroadcast();
            if (N == 0) {
                return;
            }
            for (int i = 0; i < N; i++) {
                Log.e("letianpai_sound0", "==== lettianpaiservice_4");
                if (ltpAudioEffectCallback.getBroadcastItem(i) != null) {
                    ltpAudioEffectCallback.getBroadcastItem(i).onAudioEffectCommand(command, data);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.e("letianpai_sound0", "==== lettianpaiservice_5");
            ltpAudioEffectCallback.finishBroadcast();
            mAeLock.unlock();
        }
    }

    private void responseMcuCommand(String command, String data) {
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return;
        }
        mMcuLock.lock();
        try {
            int N = ltpMcuCommandCallback.beginBroadcast();
            if (N == 0) {
                return;
            }
            for (int i = 0; i < N; i++) {
                if (ltpMcuCommandCallback.getBroadcastItem(i) != null) {
                    ltpMcuCommandCallback.getBroadcastItem(i).onMcuCommandCommand(command, data);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ltpMcuCommandCallback.finishBroadcast();
            mMcuLock.unlock();
        }
    }

    private void setLTPRobotStatus(int robotStatus) {
        mLock.lock();

        try {
            int N = RemoteCallbackList.beginBroadcast();
            if (N == 0) {
                return;
            }
            for (int i = 0; i < N; i++) {
                if (RemoteCallbackList.getBroadcastItem(i) != null) {
                    RemoteCallbackList.getBroadcastItem(i).onRobotStatusChanged(robotStatus);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RemoteCallbackList.finishBroadcast();
            mLock.unlock();
        }

    }

    private void responseCommand(LtpCommand ltpCommand) {
        mLock.lock();
        try {
            int N = RemoteCallbackList.beginBroadcast();
            if (N == 0) {
                return;
            }
            for (int i = 0; i < N; i++) {
                if (RemoteCallbackList.getBroadcastItem(i) != null) {
                    RemoteCallbackList.getBroadcastItem(i).onCommandReceived(ltpCommand);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RemoteCallbackList.finishBroadcast();
            mLock.unlock();
        }
    }

    private void responseLongConnectCommand(String command, String data) {
        Log.d("<<<<", "responseLongConnectCommand: command--" + command + "-----" + "data::" + data);
        if (TextUtils.isEmpty(command) || (TextUtils.isEmpty(data))) {
            return;
        }
        mLongConnectLock.lock();
        try {
            int N = ltpLongConnectCallback.beginBroadcast();
            if (N == 0) {
                return;
            }
            for (int i = 0; i < N; i++) {
                if (ltpLongConnectCallback.getBroadcastItem(i) != null) {
                    ltpLongConnectCallback.getBroadcastItem(i).onLongConnectCommand(command, data);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ltpLongConnectCallback.finishBroadcast();
            mLongConnectLock.unlock();
        }
    }

    private void responseSensorCommand(String command, String data) {
        if (TextUtils.isEmpty(command)) {
            return;
        }
        mSensorLock.lock();
        try {
            int N = ltpSensorResponseCallback.beginBroadcast();
            if (N == 0) {
                return;
            }
            for (int i = 0; i < N; i++) {
                if (ltpSensorResponseCallback.getBroadcastItem(i) != null) {
                    ltpSensorResponseCallback.getBroadcastItem(i).onSensorResponse(command, data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ltpSensorResponseCallback.finishBroadcast();
            mSensorLock.unlock();
        }
    }


    private void sendLongConnectCmd(String cmd, String data) {
        sendRobotCmd(robotLongConnectHandler, RobotAidlConsts.CMD_LONG_CONNECT,new CmdInfo(cmd,data));
    }

    private void sendMcuCmd(String cmd, String data) {
        sendRobotCmd(robotMcuHandler,RobotAidlConsts.CMD_MCU,new CmdInfo(cmd,data));
    }

    private void sendAudioEffectCmd(String cmd, String data) {
        sendRobotCmd(robotAudioEffectHandler,RobotAidlConsts.CMD_AUDIO_EFFECT,new CmdInfo(cmd,data));
    }

    private void sendExpressionCmd(String cmd, String data) {
        sendRobotCmd(robotExpressionHandler,RobotAidlConsts.CMD_EXPRESSION,new CmdInfo(cmd,data));
    }

    private void sendAppCmd(String cmd, String data) {
        sendRobotCmd(robotAppCmdHandler,RobotAidlConsts.CMD_APP_CMD,new CmdInfo(cmd,data));
    }

    private void sendRobotStatus(String cmd, String data) {
        sendRobotCmd(robotRobotStatusHandler,RobotAidlConsts.CMD_ROBOT_STATUS,new CmdInfo(cmd,data));
    }

    private void sendTTS(String cmd, String data) {
        sendRobotCmd(robotTTSHandler,RobotAidlConsts.CMD_TTS,new CmdInfo(cmd,data));
    }

    private void sendSpeechCmd(String cmd, String data) {
        sendRobotCmd(robotSpeechCmdHandler,RobotAidlConsts.CMD_SPEECH,new CmdInfo(cmd,data));
    }

    private void sendSensorCmd(String cmd, String data) {
        sendRobotCmd(robotSensorHandler,RobotAidlConsts.CMD_SENSOR,new CmdInfo(cmd,data));
    }

    private void sendMiCmd(String cmd, String data) {
        sendRobotCmd(robotMiHandler,RobotAidlConsts.CMD_MI,new CmdInfo(cmd,data));
    }

    private void sendIdentifyCmd(String cmd, String data) {
        sendRobotCmd(robotIdentifyHandler,RobotAidlConsts.CMD_IDENTIFY,new CmdInfo(cmd,data));
    }

    private void sendBleCmd(String cmd, String data,boolean isNeedResponse) {
        sendRobotCmd(robotBleHandler,RobotAidlConsts.CMD_BLE,new CmdInfo(cmd,data),isNeedResponse);
    }

    private void sendBleResponseCmd(String cmd, String data) {
        sendRobotCmd(robotBleResponseHandler,RobotAidlConsts.CMD_BLE_RESPONSE,new CmdInfo(cmd,data));
    }


    private void sendRobotCmd(Handler handler, int cmdType, CmdInfo cmdInfo) {
        Message message = new Message();
        message.what = cmdType;
        message.obj = cmdInfo;
        handler.sendMessage(message);
    }

    private void sendRobotCmd(Handler handler, int cmdType, CmdInfo cmdInfo,boolean isNeedResponse) {
        Message message = new Message();
        message.what = cmdType;
        message.obj = cmdInfo;
        if (isNeedResponse){
           message.arg2 = 1;
        }
        handler.sendMessage(message);

    }


    private class RobotLongConnectHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotLongConnectHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RobotAidlConsts.CMD_LONG_CONNECT) {
                if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                    String command = ((CmdInfo)(msg.obj)).getCommand();
                    String data = ((CmdInfo)(msg.obj)).getData();
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                        responseLongConnectCommand(command, data);
                    }
                }
            }
        }
    }


    private class RobotMcuHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotMcuHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RobotAidlConsts.CMD_MCU) {
                if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                    String command = ((CmdInfo)(msg.obj)).getCommand();
                    String data = ((CmdInfo)(msg.obj)).getData();
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                        responseMcuCommand(command, data);
                    }
                }
            }
        }
    }

    private class RobotAudioEffectHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotAudioEffectHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RobotAidlConsts.CMD_AUDIO_EFFECT) {
                if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                    String command = ((CmdInfo)(msg.obj)).getCommand();
                    String data = ((CmdInfo)(msg.obj)).getData();
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                        responseAudioEffectCmd(command, data);
                    }
                }

            }
        }
    }

    private class RobotExpressionHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotExpressionHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RobotAidlConsts.CMD_EXPRESSION) {
                if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                    String command = ((CmdInfo)(msg.obj)).getCommand();
                    String data = ((CmdInfo)(msg.obj)).getData();
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                        responseExpressionChange(command, data);
                    }
                }

            }
        }
    }


    private class RobotAppCmdHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotAppCmdHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                String command = ((CmdInfo)(msg.obj)).getCommand();
                String data = ((CmdInfo)(msg.obj)).getData();
                if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                    responseAppCommand(command, data);
                }
            }
        }
    }

    private class RobotRobotStatusHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotRobotStatusHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RobotAidlConsts.CMD_ROBOT_STATUS) {
                if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                    String command = ((CmdInfo)(msg.obj)).getCommand();
                    String data = ((CmdInfo)(msg.obj)).getData();
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                        responseRobotStatusCmds(command, data);
                    }
                }

            }
        }
    }

    private class RobotTTSHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotTTSHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RobotAidlConsts.CMD_TTS) {
                if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                    String command = ((CmdInfo)(msg.obj)).getCommand();
                    String data = ((CmdInfo)(msg.obj)).getData();
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                        responseRobotTTS(command, data);
                    }
                }
            }
        }
    }


    private class RobotSpeechCmdHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotSpeechCmdHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RobotAidlConsts.CMD_SPEECH) {
                if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                    String command = ((CmdInfo)(msg.obj)).getCommand();
                    String data = ((CmdInfo)(msg.obj)).getData();
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                        responseSpeechCmds(command, data);
                    }
                }
            }
        }
    }


    private class RobotSensorHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotSensorHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RobotAidlConsts.CMD_SENSOR) {
                if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                    String command = ((CmdInfo)(msg.obj)).getCommand();
                    String data = ((CmdInfo)(msg.obj)).getData();
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                        responseSensorCommand(command, data);
                    }
                }

            }
        }
    }

    private class RobotMiHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotMiHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RobotAidlConsts.CMD_MI) {
                if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                    String command = ((CmdInfo)(msg.obj)).getCommand();
                    String data = ((CmdInfo)(msg.obj)).getData();
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                        responseMiCmd(command, data);
                    }
                }
            }
        }
    }


    private class RobotIdentifyHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotIdentifyHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RobotAidlConsts.CMD_IDENTIFY) {
                if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                    String command = ((CmdInfo)(msg.obj)).getCommand();
                    String data = ((CmdInfo)(msg.obj)).getData();
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                        responseIdentifyCmd(command, data);
                    }
                }
            }
        }
    }

    private class RobotBleHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotBleHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == RobotAidlConsts.CMD_BLE) {
                if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                    String command = ((CmdInfo)(msg.obj)).getCommand();
                    String data = ((CmdInfo)(msg.obj)).getData();
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                        if (msg.arg2 == 1){
                            responseBleCmd(command, data,true);
                        }else{
                            responseBleCmd(command,data,false);
                        }

                    }
                }
            }
        }
    }

    private class RobotBleResponseHandler extends Handler {

        private final WeakReference<Context> context;

        public RobotBleResponseHandler(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == RobotAidlConsts.CMD_BLE_RESPONSE) {
                if (msg.obj != null && ((CmdInfo)(msg.obj)) != null){
                    String command = ((CmdInfo)(msg.obj)).getCommand();
                    String data = ((CmdInfo)(msg.obj)).getData();
                    if (!TextUtils.isEmpty(command) && !TextUtils.isEmpty(data)){
                        responseBleResponse(command, data);
                    }
                }
            }
        }
    }





}
