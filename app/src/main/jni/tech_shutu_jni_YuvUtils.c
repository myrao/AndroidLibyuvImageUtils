#include <tech_shutu_jni_YuvUtils.h>

#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <string.h>
#include <stdlib.h>
#define TAG "jni-log-jni" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) // 定义LOGF类型


char *input_src_data, *output_src_data, *input_scale_data,
        *output_processed_data,
        *src_y_data, *src_u_data, *src_v_data,
        *dst_y_data, *dst_u_data, *dst_v_data, *dst_data4,
        *dst_y_processed, *dst_u_processed, *dst_v_processed,
        *dst_y_processed_final, *dst_u_processed_final, *dst_v_processed_final,
        *dst_rgb_y,  *dst_rgb_u, *dst_rgb_v,
        *dst_yuv_data;

int  src_data_width, src_data_height,
     rotation_degree, len_src, len_scale,
     dst_src_data_width, dst_src_data_height,
     dst_scale_src_data_width, dst_scale_src_data_height,
     len_src_rgb;

JNIEXPORT void JNICALL Java_tech_shutu_jni_YuvUtils_allocateMemo
  (JNIEnv *env, jclass jcls, jint srcYuvLength, jint srcArgbLength, jint dstYuvLength) {
    len_src = srcYuvLength;
    len_src_rgb = srcArgbLength;
    len_scale = dstYuvLength;

    input_src_data = malloc(sizeof(char) * len_src);

    src_y_data = malloc(sizeof(char) * (len_src * 2 / 3));
    src_u_data = malloc(sizeof(char) * (len_src / 6));
    src_v_data = malloc(sizeof(char) * (len_src / 6));

    dst_y_data = malloc(sizeof(char) * (len_src * 2 / 3));
    dst_u_data = malloc(sizeof(char) * (len_src / 6));
    dst_v_data = malloc(sizeof(char) * (len_src / 6));

    output_processed_data = malloc(sizeof(char) * len_scale);

    dst_yuv_data = malloc(sizeof(char)* len_src_rgb);

    dst_y_processed = malloc(sizeof(char) * (len_scale * 2 / 3));
    dst_u_processed = malloc(sizeof(char) * len_scale / 6);
    dst_v_processed = malloc(sizeof(char) * len_scale / 6);
    dst_y_processed_final = malloc(sizeof(char) * (len_scale * 2 / 3));
    dst_u_processed_final = malloc(sizeof(char) * len_scale / 6);
    dst_v_processed_final = malloc(sizeof(char) * len_scale / 6);
  }


JNIEXPORT void JNICALL Java_tech_shutu_jni_YuvUtils_rgbToYuvByAlgorithms
  (JNIEnv *env, jclass jcls, jintArray aRGB, jbyteArray yuv, jint width, jint height) {

       int frameSize = width * height;
       int chromasize = frameSize / 4;

       int yIndex = 0;
       int uIndex = frameSize;
       int vIndex = frameSize + chromasize;

       int R, G, B, Y, U, V;
       int index = 0;
       jint *argb_array = (*env)->GetIntArrayElements(env, aRGB, NULL);
       unsigned char *yuvBuffer = (*env)->GetByteArrayElements(env, yuv, NULL);

       if (argb_array == NULL) {
           return;
       }
       if (yuvBuffer == NULL) {
           return;
       }

       for (int j = 0; j < height; j++) {
         for (int i = 0; i < width; i++) {
             //a = (aRGB[index] & 0xff000000) >> 24; //not using it right now
             R = (argb_array[index] & 0xff0000) >> 16;
             G = (argb_array[index] & 0xff00) >> 8;
             B = (argb_array[index] & 0xff) >> 0;

             Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
             U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
             V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

             yuvBuffer[yIndex++] = ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));

             if (j % 2 == 0 && index % 2 == 0) {
                 yuvBuffer[vIndex++] = ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                 yuvBuffer[uIndex++] = ((V < 0) ? 0 : ((V > 255) ? 255 : V));
             }
             index++;
         }
       }

       (*env)->ReleaseIntArrayElements(env, aRGB, argb_array, 0);
       (*env)->ReleaseByteArrayElements(env, yuv, yuvBuffer, 0);
  }


JNIEXPORT void JNICALL Java_tech_shutu_jni_YuvUtils_rgbToYuvBylibyuv
  (JNIEnv *env, jclass jcls, jobject bitmapcolor, jbyteArray dstYuv) {

        AndroidBitmapInfo infocolor;
        int ret;

        LOGI("convertToGray");
        if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
            LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
            return;
        }

        LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is %d",
         infocolor.width, infocolor.height, infocolor.stride, infocolor.format, infocolor.flags);

        if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
            LOGE("Bitmap format is not RGBA_8888 !");
            return;
        }

        unsigned char* argb_array = NULL;

        if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, (unsigned char*)&argb_array)) < 0) {
            LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
            return;
        }


        LOGD("########## start do yuv convert #############\n");

        ARGBToI420(argb_array,        infocolor.stride ,
                    dst_y_data,       infocolor.width,
                    dst_u_data,       infocolor.width / 2,
                    dst_v_data,       infocolor.width / 2,
                    infocolor.width,  infocolor.height);

        // ARGBToI420 function format:
        // int ARGBToI420(const uint8* src_frame, int src_stride_frame,
        //                uint8* dst_y, int dst_stride_y,
        //                uint8* dst_u, int dst_stride_u,
        //                uint8* dst_v, int dst_stride_v,
        //                int width, int height);

        // merge y plane to output_data
        memcpy(output_processed_data, dst_y_data, (len_src * 2 / 3 ) );
        // merge v plane to output_data
        memcpy(output_processed_data + (len_src * 2 / 3), dst_u_data, (len_src / 6 ) );
        // merge u plane to output_data
        memcpy(output_processed_data + (len_src * 5 / 6 ), dst_v_data, (len_src/ 6) );

        LOGI("unlocking pixels");
        AndroidBitmap_unlockPixels(env, bitmapcolor);
        (*env)->SetByteArrayRegion (env, dstYuv, 0, len_src, (jbyte*)(output_processed_data));
  }



JNIEXPORT void JNICALL Java_tech_shutu_jni_YuvUtils_rgbToYuvWidthScaleBylibyuv
  (JNIEnv *env, jclass jcls, jobject bitmapcolor, jbyteArray yuv, jint width, jint height, jint dst_width, jint dst_height) {

        AndroidBitmapInfo infocolor;
        int ret;

        LOGI("convertToGray");
        if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
            LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
            return;
        }

        LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is %d",
         infocolor.width, infocolor.height, infocolor.stride, infocolor.format, infocolor.flags);

        if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
            LOGE("Bitmap format is not RGBA_8888 !");
            return;
        }

        unsigned char* argb_array = NULL;

        if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, (unsigned char*)&argb_array)) < 0) {
            LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
            return;
        }

        LOGD("########## start do yuv scale and convert #############\n");

        int filtering = 3; // 1. linear; 2. bilinear; 3. box

       ARGBScale(argb_array, infocolor.stride,
                 infocolor.width,  infocolor.height,
                 dst_yuv_data, dst_width * 4,
                 dst_width,  dst_height,
                 filtering);

       ARGBToI420(dst_yuv_data,      dst_width * 4 ,
                   dst_y_processed,  dst_width,
                   dst_u_processed,  dst_width / 2,
                   dst_v_processed,  dst_width / 2,
                   dst_width,        dst_height);

       // function format:
       // int ARGBScale(const uint8* src_argb, int src_stride_argb,
       //              int src_width, int src_height,
       //              uint8* dst_argb, int dst_stride_argb,
       //              int dst_width, int dst_height,
       //              enum FilterMode filtering);
       // int ARGBToI420(const uint8* src_frame, int src_stride_frame,
       //                uint8* dst_y, int dst_stride_y,
       //                uint8* dst_u, int dst_stride_u,
       //                uint8* dst_v, int dst_stride_v,
       //                int width, int height);

       // merge y plane to output_data
       memcpy(output_processed_data, dst_y_processed, (len_scale * 2 / 3 ) );
       // merge v plane to output_data
       memcpy(output_processed_data + (len_scale * 2 / 3), dst_u_processed, (len_scale / 6 ) );
       // merge u plane to output_data
       memcpy(output_processed_data + (len_scale * 5 / 6 ), dst_v_processed, (len_scale/ 6) );

       LOGI("unlocking pixels");
       AndroidBitmap_unlockPixels(env, bitmapcolor);
       (*env)->SetByteArrayRegion (env, yuv, 0, len_scale, (jbyte*)(output_processed_data));
  }

JNIEXPORT void JNICALL Java_tech_shutu_jni_YuvUtils_scaleAndRotateYV12ToI420
 (JNIEnv *env, jclass jcls, jbyteArray src_data, jbyteArray dst_data, jint src_width, jint src_height,
       jint rotation_degree, jint dst_width, jint dst_height){

    (*env)->GetByteArrayRegion (env, src_data, 0, len_src, (jbyte*)(input_src_data));

    memcpy(src_y_data, input_src_data , (len_src * 2 /3));
    // get u plane
    memcpy(src_u_data, input_src_data + (len_src * 2 / 3), len_src / 6);
    // get v plane
    memcpy(src_v_data, input_src_data + (len_src * 5 / 6), len_src / 6);

    I420Scale(src_y_data, src_width,
              src_u_data, (src_width + 1) / 2,
              src_v_data, (src_width + 1) / 2,
              src_width, src_height,
              dst_y_data, dst_height,
              dst_u_data, (dst_height + 1) / 2,
              dst_v_data, (dst_height + 1) / 2,
              dst_height, dst_width,
              3);

    /**
     * format: YV12 (YYYYY YYYY VV UU)
     * method: rotate
     */
    I420Rotate( dst_y_data,      dst_height,
                dst_u_data,      (dst_height + 1) / 2,
                dst_v_data,      (dst_height + 1) / 2,
                dst_y_processed, dst_width,
                dst_u_processed, (dst_width + 1) / 2,
                dst_v_processed, (dst_width + 1) / 2,
                dst_height, dst_width,
                rotation_degree);

    //I420Rotate(const uint8* src_y, int src_stride_y,
    //           const uint8* src_u, int src_stride_u,
    //           const uint8* src_v, int src_stride_v,
    //           uint8* dst_y, int dst_stride_y,
    //           uint8* dst_u, int dst_stride_u,
    //           uint8* dst_v, int dst_stride_v,
    //           int width, int height,
    //           RotationMode mode) {

    // merge y plane to output_data
    memcpy(output_processed_data, dst_y_processed, (len_scale * 2 / 3 ) );
    // merge v plane to output_data
    memcpy(output_processed_data+(len_scale * 2 / 3), dst_u_processed, (len_scale / 6 ) );
    // merge u plane to output_data
    memcpy(output_processed_data+(len_scale * 5 / 6 ),dst_v_processed, (len_scale / 6) );
    // output to the dst_data
    (*env)->SetByteArrayRegion (env, dst_data, 0, len_scale, (jbyte*)(output_processed_data));
  }


JNIEXPORT void JNICALL Java_tech_shutu_jni_YuvUtils_releaseMemo
  (JNIEnv *env, jclass jcls) {
    LOGD("########## Release START#############\n");
            free(input_src_data);
            free(src_y_data);
            free(src_u_data);
            free(src_v_data);
            free(dst_y_data);
            free(dst_u_data);
            free(dst_v_data);
            free(output_processed_data);
            free(dst_yuv_data);
            free(dst_y_processed);
            free(dst_u_processed);
            free(dst_v_processed);
            free(dst_y_processed_final);
            free(dst_u_processed_final);
            free(dst_v_processed_final);

            input_src_data         = NULL;
            src_y_data             = NULL;
            src_u_data             = NULL;
            src_v_data             = NULL;
            dst_y_data             = NULL;
            dst_u_data             = NULL;
            dst_v_data             = NULL;
            output_processed_data  = NULL;
            dst_yuv_data           = NULL;
            dst_y_processed        = NULL;
            dst_u_processed        = NULL;
            dst_v_processed        = NULL;
            dst_y_processed_final        = NULL;
            dst_u_processed_final        = NULL;
            dst_v_processed_final        = NULL;

        LOGD("########## Release OVER#############\n");
  }
