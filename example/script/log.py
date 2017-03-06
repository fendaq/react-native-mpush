#coding=utf-8

import os

# 解决RN console 中文问题.
# 请用webstorm 设置External Tools 运行

if __name__ == "__main__":
    command = "adb logcat *:S ReactNative:V ReactNativeJS:V)"
    os.system(command)