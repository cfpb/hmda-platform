## OpenJDK 13

1. Install asdf `brew install asdf`
2. Add java plugin to asdf `asdf plugin-add java https://github.com/halcyon/asdf-java.git`
3. Install jdk13 `asdf install java adoptopenjdk-13.0.2+8.1`
4. Set global jdk installation `asdf global java adoptopenjdk-13.0.2+8.1`
5. Run asdf script to update JAVA_HOME env variable `. ~/.asdf/plugins/java/set-java-home.bash`
6. Copy java home path `echo $JAVA_HOME`
7. Add local installation of java to sdkman `sdk install java 13-adpotopenjdk <Path from step 6>`
8. Set sdkman java version `sdk use java 13-adpotopenjdk`
9. Verify installation `java --version`