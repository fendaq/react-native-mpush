package city.tulip.rn.mpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;
import com.mpush.api.Constants;

import city.tulip.mpush.MPush;
import city.tulip.mpush.MPushService;

import static android.R.id.message;


/**
 *
 * @author tangzehua
 * @since 2017-03-07
 */
public class RCTMPushReceiver extends BroadcastReceiver {

    private final static String MPUSH_EVENT_MESSAGE = "MPushEventMessage";
    private final static String MPUSH_EVENT = "MPushEvent";

    private ReactApplicationContext reactContext;
    public RCTMPushReceiver(ReactApplicationContext reactApplicationContext){
        super();
        this.reactContext = reactApplicationContext;
        initReceiver();
    }

    private void initReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mpush.MESSAGE_RECEIVED");
        filter.addAction("com.mpush.KICK_USER");
        filter.addAction("com.mpush.CONNECTIVITY_CHANGE");
        filter.addAction("com.mpush.HANDSHAKE_OK");
        filter.addAction("com.mpush.BIND_USER");
        filter.addAction("com.mpush.UNBIND_USER");
        filter.addCategory(reactContext.getPackageName());
        reactContext.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (MPushService.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            byte[] bytes = intent.getByteArrayExtra(MPushService.EXTRA_PUSH_MESSAGE);
            int messageId = intent.getIntExtra(MPushService.EXTRA_PUSH_MESSAGE_ID, 0);
            String message = new String(bytes, Constants.UTF_8);

//           Toast.makeText(context, "收到新的通知：" + message, Toast.LENGTH_SHORT).show();

            if (messageId > 0) MPush.I.ack(messageId);
            if (TextUtils.isEmpty(message)) return;
            reactContext.getJSModule(RCTNativeAppEventEmitter.class).emit(MPUSH_EVENT_MESSAGE, message);

        } else {
            //reactContext.getJSModule(RCTNativeAppEventEmitter.class).emit(MPUSH_EVENT, intent.getAction());
        }
        Log.d("MPush", intent.toString());
        if (MPushService.ACTION_KICK_USER.equals(intent.getAction())) {

            Toast.makeText(context, "用户被踢下线了", Toast.LENGTH_SHORT).show();
        } else if (MPushService.ACTION_BIND_USER.equals(intent.getAction())) {

            Toast.makeText(context, "绑定用户:"
                            + intent.getStringExtra(MPushService.EXTRA_USER_ID)
                            + (intent.getBooleanExtra(MPushService.EXTRA_BIND_RET, false) ? "成功" : "失败")
                    , Toast.LENGTH_SHORT).show();

        } else if (MPushService.ACTION_UNBIND_USER.equals(intent.getAction())) {
            Toast.makeText(context, "解绑用户:"
                            + (intent.getBooleanExtra(MPushService.EXTRA_BIND_RET, false)
                            ? "成功"
                            : "失败")
                    , Toast.LENGTH_SHORT).show();

        } else if (MPushService.ACTION_CONNECTIVITY_CHANGE.equals(intent.getAction())) {
            Toast.makeText(context, intent.getBooleanExtra(MPushService.EXTRA_CONNECT_STATE, false)
                            ? "MPUSH连接建立成功"
                            : "MPUSH连接断开"
                    , Toast.LENGTH_SHORT).show();
        } else if (MPushService.ACTION_HANDSHAKE_OK.equals(intent.getAction())) {
            Toast.makeText(context, "MPUSH握手成功, 心跳:" + intent.getIntExtra(MPushService.EXTRA_HEARTBEAT, 0)
                    , Toast.LENGTH_SHORT).show();
        }
    }

   /* private NotificationDO fromJson(String message) {
        try {
            JSONObject messageDO = new JSONObject(message);
            if (messageDO != null) {
                JSONObject jo = new JSONObject(messageDO.optString("content"));
                NotificationDO ndo = new NotificationDO();
                ndo.setContent(jo.optString("content"));
                ndo.setTitle(jo.optString("title"));
                ndo.setTicker(jo.optString("ticker"));
                ndo.setNid(jo.optInt("nid", 1));
                ndo.setExtras(jo.optJSONObject("extras"));
                return ndo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/
}
