#ifndef QDEVICEUUID_H
#define QDEVICEUUID_H

#include <QWidget>
#include <QAndroidJniObject>
#include <QHeaderView>
#include <QTabWidget>
#include <QTableView>
#include <QDebug>

QT_BEGIN_NAMESPACE
namespace Ui { class QDeviceUUID; }
QT_END_NAMESPACE

class QDeviceUUID : public QWidget
{
    Q_OBJECT

public:
    QDeviceUUID(QWidget *parent = nullptr);
    static QDeviceUUID *instance() { return m_instance; }
    ~QDeviceUUID();

signals:
    void messageFromJava(const QString &message);

public slots:
    void messageFromJavaSlot(const QString &message);

private slots:
    void on_ButtonStartDiscovery_clicked();

    void on_ButtonGetPairedDevUUID_clicked();

private:
    void startBluetoothDiscovery();
    void getPairedDeviceUUIDs();

private:
    Ui::QDeviceUUID *ui;
    static QDeviceUUID *m_instance;
};
#endif // QDEVICEUUID_H
