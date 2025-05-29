pipeline {
    agent {
        kubernetes {
            defaultContainer 'jenkins-agent-k8s'
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkins/label: jenkins-agent-k8s
spec:
  containers:
  - name: jenkins-agent-k8s
    image: sebas3004tian/jenkins-agent-k8s:latest
    command:
    - cat
    tty: true
"""
        }
    }

    environment {
        DOCKERHUB_USER = "sebas3004tian"
        IMAGE_TAG = "latest"
        REPO_URL = "https://github.com/Sebas3004tian/ecommerce-microservice-backend-app.git"
        K8S_NAMESPACE = "ecommerce"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'develop-taller2-Ingesoft', url: "${REPO_URL}"
            }
        }

        stage('Deploy Core Services') {
            steps {
                echo "Deploying core services..."
                sh "kubectl apply -f k8s/dev/core/ -n ${K8S_NAMESPACE}"
            }
        }

        stage('Detect Changed Services') {
            steps {
                script {
                    def allServices = [
                        "api-gateway",
                        "favourite-service",
                        "order-service",
                        "payment-service",
                        "product-service",
                        "shipping-service",
                        "user-service",
                        "cloud-config",
                        "service-discovery",
                        "proxy-client"
                    ]

                    def diffOutput = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
                    def changedServices = allServices.findAll { service ->
                        diffOutput.split('\\n').any { it.startsWith(service + "/") }
                    }

                    if (changedServices.isEmpty()) {
                        echo "No microservices changed. Skipping build and deploy."
                        currentBuild.result = 'SUCCESS'
                        skipRemainingStages = true
                    } else {
                        echo "Changed services: ${changedServices.join(', ')}"
                        env.CHANGED_SERVICES = changedServices.join(',')
                    }
                }
            }
        }

        stage('Build Changed Services') {
            when {
                expression { return !env.CHANGED_SERVICES?.isEmpty() }
            }
            steps {
                script {
                    env.CHANGED_SERVICES.split(',').each { service ->
                        dir(service) {
                            sh "docker build -t ${DOCKERHUB_USER}/${service}:${IMAGE_TAG} ."
                        }
                    }
                }
            }
        }

        stage('Push Images') {
            when {
                expression { return !env.CHANGED_SERVICES?.isEmpty() }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                    script {
                        env.CHANGED_SERVICES.split(',').each { service ->
                            sh "docker push ${DOCKERHUB_USER}/${service}:${IMAGE_TAG}"
                        }
                    }
                }
            }
        }

        stage('Deploy Changed Services') {
            when {
                expression { return !env.CHANGED_SERVICES?.isEmpty() }
            }
            steps {
                script {
                    env.CHANGED_SERVICES.split(',').each { service ->
                        def deploymentPath = "k8s/dev/${service}-deployment.yaml"
                        if (fileExists(deploymentPath)) {
                            sh "kubectl apply -f ${deploymentPath} -n ${K8S_NAMESPACE}"
                        } else {
                            echo "WARNING: No deployment file found for ${service}"
                        }
                    }
                }
            }
        }
    }
}
