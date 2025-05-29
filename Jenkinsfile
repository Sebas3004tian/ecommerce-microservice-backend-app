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
  securityContext:
    runAsUser: 0
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
      type: Socket

  containers:
  - name: jenkins-agent-k8s
    image: sebas3004tian/jenkins-agent-k8s:latest
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
    command:
    - cat
    tty: true
"""
        }
    }

    environment {
        DOCKERHUB_USER = "sebas3004tian"
        IMAGE_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
        REPO_URL = "https://github.com/Sebas3004tian/ecommerce-microservice-backend-app.git"
        K8S_NAMESPACE = "ecommerce"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'develop-taller2-Ingesoft', url: "${REPO_URL}"
            }
        }

        stage('Build JARs with Maven') {
            steps {
                container('jenkins-agent-k8s') {
                    sh './mvnw clean package -DskipTests'
                }
            }
        }


        stage('Build & Push All Services') {
            steps {
                script {
                docker.withRegistry('https://registry.hub.docker.com', 'docker-hub') {
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
                        def customImage = docker.build("${DOCKERHUB_USER}/${service}:${IMAGE_TAG}")
                        customImage.push()
                    }
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
