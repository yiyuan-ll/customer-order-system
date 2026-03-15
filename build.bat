@echo off
echo 正在构建客户订购管理系统 v4.0
call mvn clean package -q
if %errorlevel%==0 (
    echo 构建成功！运行 run.bat 启动程序。
) else (
    echo 构建失败，请检查 Java 17+ 和 Maven 是否已安装。
)
pause
