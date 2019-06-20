pipeline {
    agent {
        label any
    }

    tools {
        jdk 'jdk11'
    }

    stages{
        stage('Init'){
            steps{
                sh "mvn clean"
            }
        }
    }

}