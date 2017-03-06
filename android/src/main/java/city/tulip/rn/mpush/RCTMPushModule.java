package city.tulip.rn.mpush;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.provider.Settings;
import android.text.TextUtils;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.mpush.api.Constants;
import com.mpush.api.http.HttpCallback;
import com.mpush.api.http.HttpMethod;
import com.mpush.api.http.HttpRequest;
import com.mpush.api.http.HttpResponse;
import com.mpush.client.ClientConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import city.tulip.mpush.BuildConfig;
import city.tulip.mpush.MPush;
import city.tulip.mpush.MPushLog;
import city.tulip.mpush.MPushReceiver;

//MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB

/**
 *
 * @author tangzehua
 * @since 2017-03-07
 */
public class RCTMPushModule extends ReactContextBaseJavaModule {

    private String allocServer;

    public RCTMPushModule(ReactApplicationContext reactContext) {
        super(reactContext);
        initReceiver(reactContext);

    }

    private void initReceiver (ReactApplicationContext reactContext){
        new RCTMPushReceiver(reactContext);

        IntentFilter mPushFilter = new IntentFilter();
        mPushFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mPushFilter.addAction("com.mpush.HEALTH_CHECK");
        mPushFilter.addAction("com.mpush.NOTIFY_CANCEL");
        reactContext.registerReceiver(new MPushReceiver(), mPushFilter);
    }

    @Override
    public String getName() {
        return "MPush";
    }

    @ReactMethod
    public void initPush(final ReadableMap options){
        allocServer = options.getString("allocServer");
        String userId = options.getString("userId");
        String tags = options.getString("tags");
        String publicKey = options.getString("publicKey");
        String version = options.hasKey("version") ? options.getString("version") : "1.0.0";

        if(TextUtils.isEmpty(allocServer) || TextUtils.isEmpty(userId) || TextUtils.isEmpty(publicKey)) return;

        //公钥有服务端提供和私钥对应
        @SuppressLint("HardwareIds")
        String deviceId = Settings.Secure.getString(getReactApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        ClientConfig cc = ClientConfig.build()
                .setPublicKey(publicKey)
                .setAllotServer(allocServer)
                .setDeviceId(deviceId)
                .setClientVersion(version)
                .setLogger(new MPushLog())
                .setLogEnabled(BuildConfig.DEBUG)
                .setEnableHttpProxy(true)
                .setUserId(userId).setTags(tags);
        MPush.I.checkInit(getReactApplicationContext()).setClientConfig(cc);
    }

    @ReactMethod
    public void startPush(final ReadableMap options){
        initPush(options);
        MPush.I.checkInit(getReactApplicationContext()).startPush();
    }

    @ReactMethod
    public void sendPush(final ReadableMap options, final Promise promise){
        if( !MPush.I.hasInit()) {
            promise.reject("MPush is not init!");
            return;
        }
        if( !options.hasKey("userId")) {
            promise.reject("Params userId is null!");
            return;
        }
        if( !options.hasKey("hello")) {
            promise.reject("Params hello is null!");
            return;
        }

        final Activity activity = getCurrentActivity();
        if (activity == null) {
            promise.reject("Current Activity is null!");
            return;
        }

        JSONObject params = new JSONObject();
        try {
            params.put("userId", options.getString("userId"));
            params.put("hello", options.getString("hello"));
        } catch (JSONException e) {
            promise.reject("SendPush For Exception: " + e.getMessage());
            return;
        }

        final Context context = getReactApplicationContext();
        HttpRequest request = new HttpRequest(HttpMethod.POST, allocServer + "/push");
        byte[] body = params.toString().getBytes(Constants.UTF_8);
        request.setBody(body, "application/json; charset=utf-8");
        request.setTimeout((int) TimeUnit.SECONDS.toMillis(10));
        request.setCallback(new HttpCallback() {
            @Override
            public void onResponse(final HttpResponse httpResponse) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (httpResponse.statusCode == 200) {
                            promise.resolve(new String(httpResponse.body, Constants.UTF_8));
                        } else {
                            promise.reject(httpResponse.reasonPhrase);
                        }
                    }
                });
            }

            @Override
            public void onCancelled() {
                promise.reject("SendPush on cancelled");
            }
        });
        MPush.I.sendHttpProxy(request);
    }

    @ReactMethod
    public void bindUser (String userId, String tags){
        if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(tags)){
            MPush.I.bindAccount(userId, tags);
        }
    }

    @ReactMethod
    public void unbindUser(){
        MPush.I.unbindAccount();
    }

    @ReactMethod
    public void stopPush(){
        MPush.I.stopPush();
    }

    @ReactMethod
    public void pausePush(){
        MPush.I.pausePush();
    }

    @ReactMethod
    public void resumePush(){
        MPush.I.resumePush();
    }
}
