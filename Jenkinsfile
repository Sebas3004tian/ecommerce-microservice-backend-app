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
        WAIT_TIMEOUT_SECONDS = 300  // Timeout para esperar pods Running (5 minutos)
        WAIT_INTERVAL_SECONDS = 10  // Intervalo para volver a chequear pods
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

        stage('Wait for Core Services Ready') {
            steps {
                script {
                    def coreServices = [
                        "cloud-config",
                        "service-discovery",
                        "proxy-client"
                    ]

                    // Función para esperar que los pods estén Running
                    def waitForPodsRunning = { service ->
                        echo "Esperando que pods de ${service} estén Running..."
                        def waited = 0
                        while(waited < env.WAIT_TIMEOUT_SECONDS.toInteger()) {
                            def runningPods = sh (
                                script: "kubectl get pods -n ${K8S_NAMESPACE} -l app=${service} --field-selector=status.phase=Running --no-headers | wc -l",
                                returnStdout: true
                            ).trim().toInteger()
                            if (runningPods > 0) {
                                echo "Servicio ${service} ya tiene pods Running."
                                return true
                            }
                            sleep env.WAIT_INTERVAL_SECONDS.toInteger()
                            waited += env.WAIT_INTERVAL_SECONDS.toInteger()
                        }
                        error("Timeout esperando que los pods de ${service} estén Running")
                    }

                    for (service in coreServices) {
                        waitForPodsRunning(service)
                    }
                }
            }
        }

        stage('Check Active Services') {
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

                    def notRunning = []

                    for (service in allServices) {
                        def pods = sh(
                            script: "kubectl get pods -n ${K8S_NAMESPACE} -l app=${service} --field-selector=status.phase=Running --no-headers | wc -l",
                            returnStdout: true
                        ).trim()

                        if (pods == '0') {
                            notRunning.add(service)
                        }
                    }

                    if (notRunning.size() == 0) {
                        echo "Todos los servicios están activos"
                        env.SERVICES_TO_DEPLOY = ""
                    } else {
                        echo "Servicios no activos: ${notRunning.join(', ')}"
                        env.SERVICES_TO_DEPLOY = notRunning.join(',')
                    }
                }
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
                        diffOutput.split('\n').any { it.startsWith(service + "/") }
                    }

                    if (changedServices.isEmpty()) {
                        echo "No microservices changed. Skipping build and deploy."
                        env.CHANGED_SERVICES = ""
                    } else {
                        echo "Changed services: ${changedServices.join(', ')}"
                        env.CHANGED_SERVICES = changedServices.join(',')
                    }
                }
            }
        }

        stage('Build Changed Services') {
            when {
                expression { return env.CHANGED_SERVICES?.trim() }
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

        stage('Deploy Services (Changed or Not Running)') {
            steps {
                script {
                    def toDeploy = []

                    // Combina servicios cambiados y servicios no activos sin repetir
                    if (env.CHANGED_SERVICES?.trim()) {
                        toDeploy.addAll(env.CHANGED_SERVICES.split(','))
                    }
                    if (env.SERVICES_TO_DEPLOY?.trim()) {
                        toDeploy.addAll(env.SERVICES_TO_DEPLOY.split(','))
                    }

                    toDeploy = toDeploy.unique()

                    if (toDeploy.isEmpty()) {
                        echo "No hay servicios para desplegar."
                        return
                    }

                    echo "Servicios a desplegar: ${toDeploy.join(', ')}"

                    // Función para esperar que los pods estén Running
                    def waitForPodsRunning = { service ->
                        echo "Esperando que pods de ${service} estén Running..."
                        def waited = 0
                        while(waited < env.WAIT_TIMEOUT_SECONDS.toInteger()) {
                            def runningPods = sh (
                                script: "kubectl get pods -n ${K8S_NAMESPACE} -l app=${service} --field-selector=status.phase=Running --no-headers | wc -l",
                                returnStdout: true
                            ).trim().toInteger()
                            if (runningPods > 0) {
                                echo "Servicio ${service} ya tiene pods Running."
                                return true
                            }
                            sleep env.WAIT_INTERVAL_SECONDS.toInteger()
                            waited += env.WAIT_INTERVAL_SECONDS.toInteger()
                        }
                        error("Timeout esperando que los pods de ${service} estén Running")
                    }

                    for (service in toDeploy) {
                        def deploymentPath = "k8s/dev/${service}-deployment.yaml"
                        if (fileExists(deploymentPath)) {
                            echo "Desplegando servicio ${service}..."
                            sh "kubectl apply -f ${deploymentPath} -n ${K8S_NAMESPACE}"
                            waitForPodsRunning(service)
                        } else {
                            echo "WARNING: No se encontró archivo deployment para ${service}, se omite."
                        }
                    }
                }
            }
        }
    }
}
