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
  serviceAccountName: jenkins
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

        stage('Build All Services') {
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
                    for (service in allServices) {
                        dir(service) {
                            sh "docker build -t ${DOCKERHUB_USER}/${service}:${IMAGE_TAG} ."
                        }
                    }
                }
            }
        }

        stage('Push All Images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
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
                        for (service in allServices) {
                            sh "docker push ${DOCKERHUB_USER}/${service}:${IMAGE_TAG}"
                        }
                    }
                }
            }
        }

        stage('Deploy Core Services') {
            steps {
                echo "Desplegando servicios core..."
                sh "kubectl apply -f k8s/dev/core/ -n ${K8S_NAMESPACE}"
            }
        }

        stage('Wait 3.5 Minutes') {
            steps {
                echo "Esperando 3 minutos y medio para que los core services estén listos..."
                sleep time: 210, unit: 'SECONDS'
            }
        }

        stage('Deploy Other Services') {
            steps {
                script {
                    def otherServices = [
                        "api-gateway",
                        "favourite-service",
                        "order-service",
                        "payment-service",
                        "product-service",
                        "shipping-service",
                        "user-service"
                    ]
                    for (service in otherServices) {
                        def path = "k8s/dev/${service}-deployment.yaml"
                        if (fileExists(path)) {
                            echo "Desplegando ${service}..."
                            sh "kubectl apply -f ${path} -n ${K8S_NAMESPACE}"
                        } else {
                            echo "WARNING: No se encontró deployment para ${service}"
                        }
                    }
                }
            }
        }
    }
}
