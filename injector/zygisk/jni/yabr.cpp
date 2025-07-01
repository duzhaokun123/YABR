#include <stdlib.h>
#include <unistd.h>

#include "utils.h"
#include "zygisk.hpp"

using zygisk::Api;
using zygisk::AppSpecializeArgs;
using zygisk::ServerSpecializeArgs;

class YABR_zygsik_loader : public zygisk::ModuleBase {
public:
    void onLoad(zygisk::Api* api, JNIEnv* env) override {
        _api = api;
        _env = env;
        api->setOption(zygisk::DLCLOSE_MODULE_LIBRARY);
    }

    void preAppSpecialize(zygisk::AppSpecializeArgs* args) override {
        auto niceName = jstr2cstr(_env, args->nice_name);

        if (cstrEquals(niceName, "tv.danmaku.bili")) {
            LOGD("load for tv.danmaku.bili");
            shouldLoad = true;
        }

        delete[] niceName;
    }

    void postAppSpecialize(const zygisk::AppSpecializeArgs* args) override {
        if (not shouldLoad) return;
        auto dexPath = "/system/framework/yabr_loader.dex";
        if (access(dexPath, 0) != 0) {
            LOGE("dexPath: %s unaccessible", dexPath);
            return;
        }
        auto cacheDir = new char[PATH_MAX];
        auto appDataDir = jstr2cstr(_env, args->app_data_dir);
        snprintf(cacheDir, PATH_MAX - 1, "%s/cache", appDataDir);
        delete[] appDataDir;
        LOGD("call loader");
        loadDex(_env, _env->NewStringUTF(dexPath), _env->NewStringUTF(cacheDir),
                _env->NewStringUTF("io.github.duzhaokun123.yabr.zygisk.Loader"),
                "load",
                _env->NewStringUTF("io.github.duzhaokun123.yabr"),
                nullptr);
    }

private:
    Api* _api = nullptr;
    JNIEnv* _env = nullptr;
    bool shouldLoad = false;
};

REGISTER_ZYGISK_MODULE(YABR_zygsik_loader)

