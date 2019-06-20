pipeline {
    agent any
    stages{
        stage('Init'){
            steps{
                sh "mvn clean"
            }
        }
    }

}