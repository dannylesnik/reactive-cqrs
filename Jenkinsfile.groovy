pipeline {
    agent {
        label any
    }
    stages{
        stage('Init'){
            steps{
                sh "mvn clean"
            }
        }
    }

}