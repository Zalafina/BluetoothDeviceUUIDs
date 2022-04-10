#include "qdeviceuuid.h"
#include "ui_qdeviceuuid.h"

#include <QAndroidJniObject>
#include <QAndroidJniEnvironment>
#include <QtAndroid>

QDeviceUUID *QDeviceUUID::m_instance = nullptr;

static void callFromJava(JNIEnv *env, jobject /*thiz*/, jstring value)
{
    emit QDeviceUUID::instance()->messageFromJava(env->GetStringUTFChars(value, nullptr));
}

QDeviceUUID::QDeviceUUID(QWidget *parent)
    : QWidget(parent)
    , ui(new Ui::QDeviceUUID)
{
    m_instance = this;

#if 1
    JNINativeMethod methods[] {{"callFromJava", "(Ljava/lang/String;)V", reinterpret_cast<void *>(callFromJava)}};
    QAndroidJniObject javaClass("org/qtproject/example/testactivity/NativeFunctions");

    QAndroidJniEnvironment env;
    jclass objectClass = env->GetObjectClass(javaClass.object<jobject>());
    env->RegisterNatives(objectClass,
                         methods,
                         sizeof(methods) / sizeof(methods[0]));
    env->DeleteLocalRef(objectClass);
#endif

    QObject::connect(this, &QDeviceUUID::messageFromJava, this, &QDeviceUUID::messageFromJavaSlot);

    ui->setupUi(this);
    ui->tabWidget->setStyleSheet("QTabBar::tab { height: 50px; width: 185px; }");
}

QDeviceUUID::~QDeviceUUID()
{
    delete ui;
}

void QDeviceUUID::messageFromJavaSlot(const QString &message)
{
    qDebug() << "messageFromJavaSlot() -> " << message;
}

void QDeviceUUID::startBluetoothDiscovery()
{
    QAndroidJniObject::callStaticMethod<void>(
                "org/qtproject/example/testactivity/MyActivity",
                "startBluetoothDiscovery");
}

void QDeviceUUID::getPairedDeviceUUIDs()
{
    QAndroidJniObject::callStaticMethod<void>(
                "org/qtproject/example/testactivity/MyActivity",
                "getPairedDeviceUUIDs");
}

void QDeviceUUID::on_ButtonStartDiscovery_clicked()
{
    startBluetoothDiscovery();
}

void QDeviceUUID::on_ButtonGetPairedDevUUID_clicked()
{
    getPairedDeviceUUIDs();
}
