# AppInstallMonitorDemo

监听应用安装可以通过注册android广播的方式，不过这种方式是在应用安装成功后才会接到通知的。本demo通过Process命令的方式来监听应用安装，在应用过安装时就可以知道，方便在这个时候做某些处理。