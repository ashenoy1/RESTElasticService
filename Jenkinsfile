def label = "jenkins-slave-${UUID.randomUUID().toString()}"

properties([
    buildDiscarder(logRotator(numToKeepStr: '10')),
    parameters([string(defaultValue: '1.0-SNAPSHOT', description: 'Please enter version: ', name: 'APP_VERSION')])
])

podTemplate(label: label, containers: [
containerTemplate(
    name: 'docker',
    image: 'docker:latest',
    ttyEnabled: true,
    command: 'cat'
)],
volumes: [
    hostPathVolume(
        mountPath: '/var/run/docker.sock',
        hostPath: '/var/run/docker.sock'
    )
])
{

    node(label) {
        withCredentials([[$class: 'UsernamePasswordMultiBinding',
            credentialsId: 'artifactory_auth',
            usernameVariable: 'NEXUS_USERNAME',
            passwordVariable: 'NEXUS_PASSWORD']]) {

            stage('Checkout') {
                checkout scm
            }

            stage('Gradle config') {
                sh 'wget https://services.gradle.org/distributions/gradle-4.7-bin.zip'
                sh 'unzip -q gradle-4.7-bin.zip -d /home/jenkins/'
            }

            stage('Dependencies') {
                container('jnlp') {
                    sh '/home/jenkins/gradle-4.7/bin/gradle --refresh-dependencies -b build.gradle'
                }
            }

            stage('Build') {
                container('jnlp') {
                    sh '/home/jenkins/gradle-4.7/bin/gradle build -b build.gradle'
                }
            }

            stage('Package') {
                container('jnlp') {
                    sh '/home/jenkins/gradle-4.7/bin/gradle copyDeps copyConfig -b build.gradle'
                }
            }

            stage('Publish') {
                sh '/home/jenkins/gradle-4.7/bin/gradle artifactoryPublish -b build.gradle'
            }

            stage('Docker') {
                container('docker') {
                    dir ('docker') {
                        docker.withRegistry('https://bams-aws.refinitiv.com:5001', 'artifactory_auth') {
                            newImage = docker.build("atr/restelasticservice:${params.APP_VERSION}", '.')
                            newImage.push()
                        }
                    }
                }
            }
        }
    }
}
