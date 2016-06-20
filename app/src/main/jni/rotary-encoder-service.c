/**
* ====================================================================
* rotary-encoder-service.c:
*   Used to receive interrupts on GPIO using poll() and handle
*   the interrupt with a finite state machine in order to
*   determine the direction of rotation of the encoder.
*   Each change of state triggers a call to fsm.
*   Therefore, to use detents, place desired fn call by fsm call.
* ====================================================================
* IMPORTANT NOTE:
*   This code assumes that gpio pins have been exported
*   and their edges set like so:
*
*   "echo XX  >/sys/class/gpio/export"
*   "echo in >/sys/class/gpio/gpioXX/direction"
*   "echo both >/sys/class/gpio/gpioXX/edge"
*
*   "echo YY  >/sys/class/gpio/export"
*   "echo in >/sys/class/gpio/gpioYY/direction"
*   "echo both >/sys/class/gpio/gpioYY/edge"
*
* ====================================================================
* author(s): Stephan Greto-McGrath (with a couple lines from SO)
* ====================================================================
*/

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <poll.h>
#include <pthread.h>
#include <unistd.h>

#include <android/log.h>
#define LOG_TAG "GPIO"
#ifndef EXEC
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#else
#define LOGD(...) printf(">==< %s >==< ",LOG_TAG),printf(__VA_ARGS__),printf("\n")
#endif

int fsm(int previousState, int currentState);
int concantenate(int x, int y);
void get_direction(char buf1[8], char buf2[8], JNIEnv *env, jclass type,jmethodID mid);
void test(JNIEnv *env, jclass type);
void *routine(void *gpios);

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

/* define arguments to pass to the thread */
struct thread_data{
    int gpio1;
    int gpio2;
};

//TODO re-comment this section
/**
* ====================================================================
* gpio_interrupt fn:
*   Uses poll() to wait on interrupt from gpio pins
*   then call get_direction to determine rotation of encoder
* ====================================================================
* authors(s): Stephan Greto-McGrath
* ====================================================================
*/
static JavaVM *jvm;

JNIEXPORT jint JNICALL
Java_com_google_hal_rotaryencoderservice_EncoderService_startRoutine(JNIEnv *env, jclass type,
                                                                     jint gpio1, jint gpio2) {

    LOGD("function begins");
//    jclass classClass = (*env)->GetObjectClass(env,type);
    /* cache JVM to attach native thread */
    int status = (*env)->GetJavaVM(env, &jvm);
    if (status != 0) {
        LOGD("failed to retrieve *env");
        exit(1);
    }


//    gpio1 = (int) gpio1;
//    gpio2 = (int) gpio2;

    struct thread_data gpios;
    gpios.gpio1 = (int) gpio1; /* set them as the given gpio numbers */
    gpios.gpio2 = (int) gpio2;
    pthread_t routine_thread; /*create routine thread */
    if (pthread_create( &routine_thread, NULL, routine, (void *) &gpios)) {
        LOGD("Error creating thread");
        exit(1);
    }
    pthread_exit(NULL);
//    routine(&gpios);
}


/**
* ====================================================================
* routine fn:
*   Configures poll(), sets up infinite loop to run on new thread
*   in jvm and then uses poll() to determine if a change has
*   taken place on a gpio pin. If it has, it calls get_direction
*   to determine which way the encoder was turned.
* ====================================================================
* authors(s): Stephan Greto-McGrath
* ====================================================================
*/
 void *routine(void *gpios){

    /* get variable from struct passed to this thread */
    struct thread_data *my_gpios;
    my_gpios = (struct thread_data *) gpios;
    int gpio1 = my_gpios->gpio1;
    int gpio2 = my_gpios->gpio2;

    /* get a new environment and attach this new thread to jvm */
    JNIEnv* newEnv;
    JavaVMAttachArgs args;
    args.version = JNI_VERSION_1_6; // choose your JNI version
    args.name = NULL; // if you want to give the java thread a name
    args.group = NULL; // you can assign the java thread to a ThreadGroup
    (*jvm)->AttachCurrentThread(jvm,&newEnv,&args);
    jclass cls = (*newEnv)->FindClass(newEnv,"com/google/hal/rotaryencoderservice/EncoderService");
    jmethodID mid = (*newEnv)->GetStaticMethodID(newEnv, cls, "handleStateChange", "(I)V");


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
    //TODO change POLLIN to POLLPRI after testing on shitty malfunctioning device is done
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
        poll(pfd, 2, 1000000);
        if ((pfd[0].revents & POLLIN) | (pfd[1].revents & POLLIN)) {
//            LOGD("interrupt received");
            /*interrupt received */
            /* consume interrupts & read values */
            if (lseek(fd1, 0, SEEK_SET) == -1) break;
            if (read(fd1, buf1, sizeof buf1) == -1) break;
            if (lseek(fd2, 0, SEEK_SET) == -1) break;
            if (read(fd2, buf2, sizeof buf2) == -1) break;
            get_direction(buf1,buf2,newEnv,cls,mid);
        }
//        LOGD("Interrupt not received");
    }
    LOGD("Reading Terminated");
     (*jvm)->DetachCurrentThread(jvm);
    close(fd1);
    close(fd2);
    exit(0);
}


/**
* ====================================================================
* get_direction fn:
*   assigns state based on sentinel var and calls the fsm to determine
*   direction
* ====================================================================
* authors(s): Stephan Greto-McGrath
* ====================================================================
*/
void get_direction(char buf1[8], char buf2[8], JNIEnv *newEnv, jclass cls, jmethodID mid){
    if (sentinel != true) {  /* we already have a prev state */
        previousState = currentState;
        currentState = concantenate(atoi(buf1), atoi(buf2));
        /* In case poll returns constantly, this statement below
         * ensures that we only consider a change in state.
         * Unnecessary if poll() is working properly with interrupts
         */
        if (previousState != currentState) {
            LOGD("direction: %d\n", fsm(currentState, previousState));
            (*newEnv)->CallStaticVoidMethod(newEnv, cls, mid, (jint)10); /* call back to java */
                                                                    /* act on state change */
        }
    } else { /* just starting -> need prev state */
        LOGD("sentinel = false");
        currentState = concantenate(atoi(buf1), atoi(buf2));
        sentinel = false;
    }
}


/**
* ====================================================================
* fsm fn:
*     Finite State Machine to decode the output of a
*     rotary encoder implemented as a pulldown switch
* ====================================================================
* Details:
*
* Rotary Encoder Output: cw 1, ccw 0, invalid -1
* How it works:
*     CW  ---->
*     CCW <----
*
*          +-----+     +-----+     +-----+
* Ch X     |     |     |     |     |     |
*       ---+     +-----+     +-----+     +-----
*
*             +-----+     +-----+     +-----+
* Ch Y        |     |     |     |     |     |
*       ------+     +-----+     +-----+     +-----
*
*       ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^  ^
* X    0| 1| 1| 0| 0| 1| 1| 0| 0| 1| 1| 0| 0|
* Y    0| 0| 1| 1| 0| 0| 1| 1| 0| 0| 1| 1| 0|
*
*     State: XY
*     ccw  prev.  cw    ccw  prev.  cw
*     01 <- 00 -> 10    B  <- A ->  D
*     11 <- 01 -> 00    C  <- B ->  A
*     10 <- 11 -> 01    D  <- C ->  B
*     00 <- 10 -> 11    A  <- D ->  C
*
*     All state transitions not defined are invalid
* ====================================================================
* author(s):  Stephan Greto-McGrath
* ====================================================================
*/
int fsm(int previousState, int currentState) {
    int direction;
    switch (previousState) {
        case stateA:
            if (currentState == stateD) {
                direction = 1; /* cw */
            } else if (currentState == stateB) {
                direction = 0; /* ccw */
            } else {
                direction = -1; /* not a valid state */
                sentinel = true; /* restart state machine */
            }
            break;
        case stateB:
            if (currentState == stateA) {
                direction = 1; /* cw */
            } else if (currentState == stateC) {
                direction = 0; /* ccw */
            } else {
                direction = -1; /* not a valid state */
                sentinel = true; /* restart state machine */
            }
            break;
        case stateC:
            if (currentState == stateB) {
                direction = 1; /* cw */
            } else if (currentState == stateD) {
                direction = 0; /* ccw */
            } else {
                direction = -1; /* not a valid state */
                sentinel = true; /* restart state machine */
            }
            break;
        case stateD:
            if (currentState == stateC) {
                direction = 1; /* cw */
            } else if (currentState == stateA) {
                direction = 0; /* ccw */
            } else {
                direction = -1; /* not a valid state */
                sentinel = true; /* restart state machine */
            }
            break;
        default:
            direction = -1; /* not a valid state */
            sentinel = true; /* restart state machine */
    }
    return direction;
}

/**
* ====================================================================
* concantenate fn: (int x, int y) -> int xy
* ====================================================================
* authors(s): Tavis Bohne (http://stackoverflow.com/a/12700533/3885972)
* ====================================================================
*/
int concantenate(int x, int y) {
    int pow = 10;
    while (y >= pow)
        pow *= 10;
    return x * pow + y;
}

