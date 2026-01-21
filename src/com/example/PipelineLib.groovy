package com.example

class PipelineLib implements Serializable {
    def script

    // Constructor to pass the Jenkins 'script' context
    PipelineLib(script) {
        this.script = script
    }

    def checkoutGit(String url, String branch) {
        script.git url: url, branch: branch
    }

    def mavenBuild(String goals) {
        script.sh "mvn ${goals}"
    }

    def runSonarScan(String key, String url, String token) {
        script.sh """
            mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.5.0.6356:sonar \
                -Dsonar.projectKey=${key} \
                -Dsonar.host.url=${url} \
                -Dsonar.login=${token}
        """
    }

    def trivyScan(String type, String target) {
        if (type == "fs") {
            script.sh "trivy fs --exit-code 0 --severity MEDIUM,HIGH,CRITICAL ${target}"
        } else {
            script.sh "trivy image --timeout 15m --exit-code 0 --severity HIGH,CRITICAL ${target}"
        }
    }

    def dockerBuild(String imagePath) {
        script.sh "docker build -t ${imagePath} ."
    }

    def pushImage(String registry, String imagePath, String credsId) {
        script.withCredentials([script.usernamePassword(credentialsId: credsId, passwordVariable: 'PASS', usernameVariable: 'USER')]) {
            script.sh "echo \$PASS | docker login -u \$USER --password-stdin ${registry}"
            script.sh "docker push ${imagePath}"
            script.sh "docker logout ${registry}"
        }
    }
}