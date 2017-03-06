let MPushNative = require('react-native').NativeModules.MPush;

export default MPush = {
    initPush: MPushNative.initPush,
    startPush: MPushNative.startPush,
    sendPush: MPushNative.sendPush,
    bindUser: MPushNative.bindUser,
    unbindUser: MPushNative.unbindUser,
    stopPush: MPushNative.stopPush,
    pausePush: MPushNative.pausePush,
    resumePush: MPushNative.resumePush,
};

export const MPushConst = {
    test: "1"
};