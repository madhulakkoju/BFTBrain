# exit on failure
set -e


# 1) Purge existing Java, Maven, and BFTBrain folder
echo ">> Removing existing Java, Maven, and BFTBrain directory…"
sudo apt-get remove --purge -y openjdk-* oracle-java* maven || true
sudo rm -rf /opt/jdk-* /opt/apache-maven-*
rm -rf "$HOME/BFTBrain"


# install jdk
wget "https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz"
tar -xvf jdk-21_linux-x64_bin.tar.gz
sudo mv jdk-21.0.7 /opt/

JAVA_HOME='/opt/jdk-21.0.7'
PATH="$JAVA_HOME/bin:$PATH"
export PATH

# install maven
wget https://downloads.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
tar -xzf apache-maven-3.9.9-bin.tar.gz
sudo mv apache-maven-3.9.9 /opt/
M2_HOME='/opt/apache-maven-3.9.9'
PATH="$M2_HOME/bin:$PATH"
export PATH
# update path in .bashrc
echo "export JAVA_HOME='/opt/jdk-21.0.7'
export M2_HOME='/opt/apache-maven-3.9.9'
export PATH=\"\$M2_HOME/bin:\$JAVA_HOME/bin:\$PATH\"" | cat - .bashrc > .bashrc_temp && mv .bashrc_temp .bashrc
# clone repo
git clone https://github.com/madhulakkoju/BFTBrain
# install pip3 and dependencies
sudo apt-get update && sudo apt-get install -y python3-pip
pip3 install -r ./BFTBrain/scripts/requirements.txt
# compile
cd BFTBrain/code
mvn package
mvn dependency:copy-dependencies
# exit
echo "BFTBrain deployment success"