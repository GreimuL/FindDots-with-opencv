#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <vector>

using namespace std;
using namespace cv;

extern "C"
JNIEXPORT jint JNICALL
Java_com_greimul_finddots_MainActivity_findDots(JNIEnv *env, jobject thiz, jlong input) {

    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    vector<vector<Point>> points;

    Mat &inputMat = *(Mat*)input;
    Mat grayMat = Mat();
    Mat resMat = Mat();

    cvtColor(inputMat,grayMat,COLOR_RGB2GRAY);
    adaptiveThreshold(grayMat,resMat,255,ADAPTIVE_THRESH_MEAN_C,THRESH_BINARY,55,10);

    findContours(resMat,contours,hierarchy,RETR_LIST,CHAIN_APPROX_SIMPLE);

    int minSize = 1;
    int maxSize = 300;

    for(vector<Point> i:contours){
        if(contourArea(i)>minSize&&contourArea(i)<maxSize){
            points.push_back(i);
        }
    }

    return points.size();
}