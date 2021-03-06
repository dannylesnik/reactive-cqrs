pipeline {
    agent any
    stages{
        stage('Build'){
            steps {
                echo "Compiling..."
                sh "${tool name: 'sbt', type:'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt compile"
            }
        }
        stage('Unit Test') {
            steps {
                echo "Testing..."
                sh "${tool name: 'sbt', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt test"
            }
        }
        stage('Publish'){
            steps{
                    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'AWS Credentials User', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                        timeout(time: 4, unit: 'MINUTES') {
                            retry(3) {
                                sh "${tool name: 'sbt', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin/sbt ecr:login"

                            }
                        }
                    }

            }
        }
    }

}