QT       += core gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

CONFIG += c++11
QT += androidextras

TARGET = DeviceUUIDs
TEMPLATE = app

ANDROID_ABIS = arm64-v8a

CONFIG(debug, debug|release){
    DEFINES += DEBUG_LOGOUT_ON
    message("Debug Build")
}

CONFIG(release, debug|release){
    message("Release Build")
}

# You can make your code fail to compile if it uses deprecated APIs.
# In order to do so, uncomment the following line.
#DEFINES += QT_DISABLE_DEPRECATED_BEFORE=0x060000    # disables all the APIs deprecated before Qt 6.0.0

SOURCES += \
    main.cpp \
    qdeviceuuid.cpp

HEADERS += \
    qdeviceuuid.h

FORMS += \
    qdeviceuuid.ui

# Default rules for deployment.
qnx: target.path = /tmp/$${TARGET}/bin
else: unix:!android: target.path = /opt/$${TARGET}/bin
!isEmpty(target.path): INSTALLS += target

DISTFILES += \
    android/AndroidManifest.xml \
    android/build.gradle \
    android/gradle/wrapper/gradle-wrapper.jar \
    android/gradle/wrapper/gradle-wrapper.properties \
    android/gradlew \
    android/gradlew.bat \
    android/res/values/libs.xml \
    android/src/org/qtproject/example/testactivity/MyActivity.java \
    android/src/org/qtproject/example/testactivity/NativeFunctions.java

contains(ANDROID_TARGET_ARCH,arm64-v8a) {
    ANDROID_PACKAGE_SOURCE_DIR = \
        $$PWD/android
}
