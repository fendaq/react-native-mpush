package city.tulip.rn.mpush;


import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

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

import java.util.concurrent.TimeUnit;

import city.tulip.mpush.MPush;

//MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB
public class RCTMPushModule extends ReactContextBaseJavaModule {

    private boolean isInitMPush = false;
    private String allocServer;

    public RCTMPushModule(ReactApplicationContext reactContext) {
        super(reactContext);
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

        //公钥有服务端提供和私钥对应
        String deviceId = Settings.Secure.getString(getReactApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        ClientConfig cc = ClientConfig.build()
                .setPublicKey(publicKey)
                .setAllotServer(allocServer)
                .setDeviceId(deviceId)
                .setClientVersion(version)
//                .setLogger(new MyLog(this, (EditText) findViewById(R.id.log)))
                .setLogEnabled(BuildConfig.DEBUG)
                .setEnableHttpProxy(true)
                .setUserId(userId).setTags(tags);
        MPush.I.checkInit(getReactApplicationContext()).setClientConfig(cc);
        isInitMPush = true;
    }

    @ReactMethod
    public void startPush(final ReadableMap options){
        initPush(options);
        MPush.I.checkInit(getReactApplicationContext()).startPush();
    }

    @ReactMethod
    public void sendPush(final ReadableMap options, final Promise promise){
        if(isInitMPush) {
            promise.reject("MPush is not init!");
            return;
        }
        if( !options.hasKey("content")) {
            promise.reject("Params content is null!");
            return;
        }

        String params = options.getString("content");
        final Context context = getReactApplicationContext();
        final Activity activity = getCurrentActivity();
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

                            Toast.makeText(context, new String(httpResponse.body, Constants.UTF_8), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, httpResponse.reasonPhrase, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled() {

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
