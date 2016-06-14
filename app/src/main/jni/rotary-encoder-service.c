#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_google_hal_rotaryencoderservice_EncoderService_getMsgFromJni(JNIEnv *env, jclass type) {

    // TODO


    return (*env)->NewStringUTF(env, "it works");
}