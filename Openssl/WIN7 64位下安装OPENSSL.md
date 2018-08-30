# WIN7 64位下安装OPENSSL

## 一、基础环境

1、系统：Win7 x64位操作系统
2、开发软件：Visual Studio 2017
3、软件版本、安装及下载位置

```
下载并安装ActivePerl：
https://www.activestate.com/activeperl/downloads/thank-you?dl=http://downloads.activestate.com/ActivePerl/releases/5.24.3.2404/ActivePerl-5.24.3.2404-MSWin32-x64-404865.exe
下载并安装Visual Studio 2017 community：
https://visualstudio.microsoft.com/zh-hans/thank-you-downloading-visual-studio/?sku=Community&rel=15
下载并解压openssl：
https://www.openssl.org/source/openssl-1.0.2p.tar.gz
解压openssl-1.0.2p.tar.gz时可以使用git bash工具：
$ tar -zxvf openssl-1.0.2p.tar.gz
```

验证perl安装：

 ![perl验证](E:\个人文件\openssl\perl验证.png)

## 二、配置 Visual Studio的环境变量

```
点击桌面上的计算机（我的电脑）-> 右键属性 -> 高级系统设置 -> 环境变量 -> path最后增加
;C:\Program Files (x86)\Microsoft Visual Studio\Preview\Community\VC\Tools\MSVC\14.14.26428\bin\Hostx64\x64
（路径以实际安装路径为准）
```

## 三、运行VS2017 x64 工具命令提示符

新版本：点击Visual Studio 2017 -> 工具 ->Visual Studio 命令提示，并在命令行中转到OpenSSL解压的目录 cd C:\openssl-1.0.2p

老版本：点击Visual Studio 2015 -> Visual Studio Tools -> Windows Desktop Command Prompts -> VS2015 x64 本机工具命令提示符，并在命令行中转到OpenSSL解压的目录

```
执行命令1：C:\"Program Files (x86)"\"Microsoft Visual Studio"\Preview\Community\VC\Auxiliary\Build\vcvars64.bat
#以实际路径为准
执行命令2：perl Configure VC-WIN64 no-asm --prefix=C:\openssl-1.0.2p
#加上no-asm，表示不使用汇编
执行命令3：ms\do_nasm
执行命令4：ms\do_win64a
执行命令5：C:\"Program Files (x86)"\"Microsoft Visual Studio"\Preview\Community\VC\Auxiliary\Build\vcvars64.bat
#配置环境
执行命令6：nmake -f ms\nt.mak
执行命令7：nmake -f ms\nt.mak test
#执行速度比较慢，会检查很多配置项是不是ok，最后出现“passed all tests”
执行命令8：nmake -f ms\nt.mak install

```

 ![openssl](E:\个人文件\openssl\openssl.png)

## 四、配置openssl环境变量

```
点击桌面上的计算机（我的电脑）-> 右键属性 -> 高级系统设置 -> 环境变量 -> path最后增加
;C:\usr\local\ssl\bin
（路径以实际安装路径为准）
```



主要参考链接：https://blog.csdn.net/pingyan158/article/details/79214843

