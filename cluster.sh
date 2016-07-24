sudo su
yum install wget
service iptables status
service iptables stop
vi /etc/selinux/config
reboot

sudo su
wget http://archive.cloudera.com/cm5/installer/latest/cloudera-manager-installer.bin
chmod +x cloudera-manager-installer.bin
./cloudera-manager-installer.bin

echo never > /sys/kernel/mm/transparent_hugepage/defrag
vi /etc/rc.local 

sysctl vm.swappiness=10
vi /etc/sysctl.conf

----------------------------------------


sudo vi /etc/yum.repos.d/datastax.repo

[datastax]
name=DataStax Repo for Apache Cassandra
baseurl=http://rpm.datastax.com/community
enabled=1
gpgcheck=0

sudo yum install dsc20


cd /opt/
wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u91-b14/jdk-8u91-linux-x64.tar.gz"
tar xzf jdk-8u91-linux-x64.tar.gz

cd /opt/jdk1.8.0_91/
alternatives --install /usr/bin/java java /opt/jdk1.8.0_91/bin/java 2
alternatives --config java
alternatives --install /usr/bin/jar jar /opt/jdk1.8.0_91/bin/jar 2
alternatives --install /usr/bin/javac javac /opt/jdk1.8.0_91/bin/javac 2
alternatives --set jar /opt/jdk1.8.0_91/bin/jar
alternatives --set javac /opt/jdk1.8.0_91/bin/javac


sudo vi /etc/cassandra/default.conf/cassandra.yaml

sudo cassandra 


