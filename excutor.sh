export JAVA_OPTS="-Xms512m -Xmx4G"
export JSBT_OPTS="-Xms512M -Xmx4G -Xss2M -XX:MaxMetaspaceSize=1024M"
sbt "run $1"
