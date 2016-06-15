#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <poll.h>
#include <unistd.h>

#include <android/log.h>
#define LOG_TAG "GPIO"
#ifndef EXEC
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#else
#define LOGD(...) printf(">==< %s >==< ",LOG_TAG),printf(__VA_ARGS__),printf("\n")
#endif

typedef enum {
    false,
    true
}
        bool;
enum {
    stateA = 00,
    stateB = 01,
    stateC = 11,
    stateD = 10
};
bool sentinel = true;   /* indicates 1st run -> get initial (previous) state */
                        /* if state is invalid sentinel = true i.e restart */
int currentState;
int previousState;



JNIEXPORT jint JNICALL
Java_com_google_hal_rotaryencoderservice_EncoderService_getInterrupt(JNIEnv *env, jclass type,
                                                                     jint gpio1, jint gpio2) {

    LOGD("function begins");
    gpio1 = (int) gpio1;
    gpio2 = (int) gpio2;
    struct pollfd pfd[2];
    int fd1, fd2;
    char str1[256], str2[256];
    char buf1[8], buf2[8];
    sprintf(str1, "/sys/class/gpio/gpio%d/value", gpio1);
    sprintf(str2, "/sys/class/gpio/gpio%d/value", gpio2);
    /* open file descriptors */
    if ((fd1 = open(str1, O_RDONLY)) < 0) {
        LOGD("failed on 1st open");
        exit(1);
    }
    if ((fd2 = open(str2, O_RDONLY)) < 0) {
        LOGD("failed on 2nd open");
        exit(1);
    }
    /* configure poll struct */
    pfd[0].fd = fd1;
    pfd[1].fd = fd2;
    pfd[0].events = POLLIN;
    pfd[1].events = POLLIN;

    lseek(fd1, 0, SEEK_SET); /* consume any prior interrupt on ch1*/
    read(fd1, buf1, sizeof buf1);
    lseek(fd2, 0, SEEK_SET); /* consume any prior interrupt on ch2*/
    read(fd2, buf2, sizeof buf2);

    for (;;) {
        /* wait for interrupt */
        poll(pfd, 2, -1);
        if ((pfd[0].revents & POLLIN) | (pfd[1].revents & POLLIN)) {
            LOGD("interrupt received");
            /*interrupt received */
            /* consume interrupts & read values */
            if (lseek(fd1, 0, SEEK_SET) == -1) break;
            if (read(fd1, buf1, sizeof buf1) == -1) break;
            if (lseek(fd2, 0, SEEK_SET) == -1) break;
            if (read(fd2, buf2, sizeof buf2) == -1) break;
        }
        LOGD("Interrupt not received");
    }
    LOGD("Leaving interrupt detection");
    close(fd1);
    close(fd2);
    exit(0);
}