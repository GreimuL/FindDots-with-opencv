#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <vector>
#include <cmath>
#include <random>
#include <algorithm>

using namespace std;
using namespace cv;

//find minimum enclosing circle
double square(double a) {
    return a * a;
}

double distance2Points(pair<double,double> a, pair<double,double> b) {
    return sqrtl(square(a.first - b.first) + square(a.second - b.second));
}

pair<double, double> findCenterDoubleDot(pair<double, double> a, pair<double, double> b) {
    return make_pair((a.first + b.first) / 2, (a.second + b.second) / 2);
}

pair<double, double> findCenterTripleDot(pair<double, double> a, pair<double, double> b, pair<double, double> c) {
    double x1 = a.first;
    double y1 = a.second;
    double x2 = b.first;
    double y2 = b.second;
    double x3 = c.first;
    double y3 = c.second;

    double centerX = ((x2 * x2 - x1 * x1 + y2 * y2 - y1 * y1) * (y3 - y2) - (x2 * x2 - x3 * x3 + y2 * y2 - y3 * y3) * (y1 - y2)) / (2 * (x1 - x2) * (y3 - y2) - 2 * (x3 - x2) * (y1 - y2));
    double centerY = ((y2 * y2 - y1 * y1 + x2 * x2 - x1 * x1) * (x3 - x2) - (y2 * y2 - y3 * y3 + x2 * x2 - x3 * x3) * (x1 - x2)) / (2 * (y1 - y2) * (x3 - x2) - 2 * (y3 - y2) * (x1 - x2));
    return make_pair(-centerX, -centerY);
}

pair<pair<double, double>, double> findCircleWith2Points(pair<double,double> p1, pair<double,double> p2,int idx, int idx2,vector<pair<double,double>>& v) {
    pair<pair<double, double>, double> currentCircle;
    pair<double, double> tmpCenter = findCenterDoubleDot(p1, p2);
    currentCircle = make_pair(tmpCenter, distance2Points(tmpCenter, p1));

    for (int i = 0;i < idx2;i++) {
        if (distance2Points(v[i], currentCircle.first) > currentCircle.second) {
            pair<double, double> tmp = findCenterTripleDot(p1, p2, v[i]);
            currentCircle = make_pair(tmp, distance2Points(tmp, v[i]));
        }
    }
    return currentCircle;
}

pair<pair<double, double>, double> findCircleWithPoint(pair<double,double> p1,int idx,vector<pair<double,double>>& v) {

    pair<pair<double, double>, double> currentCircle;
    pair<double, double> tmpCenter = findCenterDoubleDot(p1, v[0]);
    currentCircle = make_pair(tmpCenter, distance2Points(tmpCenter, p1));

    for (int i = 1;i <idx;i++) {
        if (distance2Points(v[i], currentCircle.first) > currentCircle.second) {
            currentCircle = findCircleWith2Points(p1, v[i], idx, i,v);
        }
    }

    return currentCircle;
}

pair<pair<double, double>,double> findCircle(vector<pair<double,double>>& v) {
    if (v.size() == 1||v.size()==0) {
        return make_pair(make_pair(0,0),0);
    }
    else {
        pair<pair<double, double>,double> currentCircle;
        pair<double, double> tmpCenter = findCenterDoubleDot(v[0], v[1]);
        currentCircle = make_pair(tmpCenter, distance2Points(tmpCenter, v[0]));

        for (int i = 2;i < v.size();i++) {
            if (distance2Points(v[i], currentCircle.first) > currentCircle.second) {
                currentCircle = findCircleWithPoint(v[i],i,v);
            }
        }

        return currentCircle;
    }
}
//algorithm end

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_greimul_finddots_MainActivity_findDots(JNIEnv *env, jobject thiz, jlong input) {

    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    vector<vector<Point>> points;
    vector<pair<double,double>> dots;

    Mat &inputMat = *(Mat*)input;
    Mat grayMat = Mat();
    Mat resMat = Mat();

    cvtColor(inputMat,grayMat,COLOR_RGB2GRAY);
    adaptiveThreshold(grayMat,resMat,255,ADAPTIVE_THRESH_MEAN_C,THRESH_BINARY,55,10);

    findContours(resMat,contours,hierarchy,RETR_LIST,CHAIN_APPROX_SIMPLE);

    int minSize = 1;
    int maxSize = 500;

    for(vector<Point> i:contours){
        if(contourArea(i)>minSize&&contourArea(i)<maxSize){
            dots.push_back(make_pair(i[i.size()/2].x,i[i.size()/2].y));
        }
    }

    random_device rd;
    mt19937 gen(rd());
    shuffle(dots.begin(),dots.end(), gen);

    pair<pair<double,double>,double> circle = findCircle(dots);

    jdoubleArray retData;
    retData = env->NewDoubleArray(3);

    jdouble tmpData[3];

    tmpData[0] = circle.first.first;
    tmpData[1] = circle.first.second;
    tmpData[2] = circle.second;

    env->SetDoubleArrayRegion(retData,0,3,tmpData);
    return retData;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_greimul_finddots_CameraActivity_findDots(JNIEnv *env, jobject thiz, jlong input) {

    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    vector<vector<Point>> points;
    vector<pair<double,double>> dots;

    Mat &inputMat = *(Mat*)input;
    Mat grayMat = Mat();
    Mat resMat = Mat();

    cvtColor(inputMat,grayMat,COLOR_RGB2GRAY);
    adaptiveThreshold(grayMat,resMat,255,ADAPTIVE_THRESH_MEAN_C,THRESH_BINARY,55,10);

    findContours(resMat,contours,hierarchy,RETR_LIST,CHAIN_APPROX_SIMPLE);

    int minSize = 1;
    int maxSize = 500;

    for(vector<Point> i:contours){
        if(contourArea(i)>minSize&&contourArea(i)<maxSize){
            dots.push_back(make_pair(i[i.size()/2].x,i[i.size()/2].y));
        }
    }

    random_device rd;
    mt19937 gen(rd());
    shuffle(dots.begin(),dots.end(), gen);

    pair<pair<double,double>,double> circle = findCircle(dots);

    jdoubleArray retData;
    retData = env->NewDoubleArray(3);

    jdouble tmpData[3];

    tmpData[0] = circle.first.first;
    tmpData[1] = circle.first.second;
    tmpData[2] = circle.second;

    env->SetDoubleArrayRegion(retData,0,3,tmpData);
    return retData;
}