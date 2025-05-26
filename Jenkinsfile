pipeline {
    agent any
    parameters {
        string(name: 'ORIGINAL_BRANCH_NAME', defaultValue: 'master', description: '브랜치 이름')
        string(name: 'BRANCH_NAME', defaultValue: 'master', description: '브랜치 이름')
        string(name: 'PROJECT_ID', defaultValue: '62', description: '프로젝트 ID')
    }
    stages {
        stage('Checkout') {
            steps {
                echo '1. 워크스페이스 정리 및 소스 체크아웃'
                deleteDir()
                withCredentials([usernamePassword(credentialsId: 'gitlab-token', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    git branch: "${params.BRANCH_NAME}", url: "https://${GIT_USER}:${GIT_TOKEN}@lab.ssafy.com/syt05342/apt.git"
                }
            }
        }
        stage('변경 감지') {
            steps {
                script {
                    // 기본 빌드 상태 초기화
                    env.BACKEND_BUILD_STATUS = 'NOT_EXECUTED'
                    env.FRONTEND_BUILD_STATUS = 'NOT_EXECUTED'
                    env.HEALTH_CHECK_STATUS = 'NOT_EXECUTED'
                    
                    // 첫 번째 빌드인지 확인
                    def isFirstBuild = currentBuild.previousBuild == null
                    
                    if (isFirstBuild) {
                        echo "🔵 첫 번째 빌드 → 전체 빌드 실행"
                        env.BACKEND_CHANGED = "true"
                        env.FRONTEND_CHANGED = "true"
                        return
                    }
                    
                    sh "git fetch origin ${params.BRANCH_NAME} --quiet"
                    def hasBase = sh(
                        script: "git merge-base origin/${params.BRANCH_NAME} HEAD > /dev/null 2>&1 && echo yes || echo no",
                        returnStdout: true
                    ).trim()
                    if (hasBase == "no") {
                        echo "🟡 기준 브랜치와 공통 커밋 없음 → 전체 빌드 실행"
                        env.BACKEND_CHANGED = "true"
                        env.FRONTEND_CHANGED = "true"
                        return
                    }
                    def changedFiles = sh(
                        script: "git diff --name-only origin/${params.BRANCH_NAME}...HEAD",
                        returnStdout: true
                    ).trim()
                    echo "🔍 변경된 파일 목록:\n${changedFiles}"
                    env.BACKEND_CHANGED  = changedFiles.contains("backend/")  ? "true" : "false"
                    env.FRONTEND_CHANGED = changedFiles.contains("frontend/") ? "true" : "false"
                    if (env.BACKEND_CHANGED == "false" && env.FRONTEND_CHANGED == "false") {
                        echo "⚠️ 변경된 파일 없음 → 재시도 빌드일 수 있으므로 전체 빌드 강제 실행"
                        env.BACKEND_CHANGED = "true"
                        env.FRONTEND_CHANGED = "true"
                    }
                    echo "🛠️ 백엔드 변경됨: ${env.BACKEND_CHANGED}"
                    echo "🎨 프론트엔드 변경됨: ${env.FRONTEND_CHANGED}"
                }
            }
        }
        stage('Build Backend') {
            when {
                expression { env.BACKEND_CHANGED == "true" }
            }
            steps {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    script {
                        try {
                            env.BACKEND_BUILD_STATUS = 'SUCCESS'
                            withCredentials([file(credentialsId: "backend", variable: 'BACKEND_ENV')]) {
                                sh '''
                                    cp "$BACKEND_ENV" "$WORKSPACE/backend/.env"
                                '''
                            }
                            dir('backend') {
                                sh '''
                                    docker build -t spring .
                                    docker stop spring || true
                                    docker rm spring || true
                                    docker run -d -p 8080:8080 --network mynet --env-file .env --name spring spring
                                '''
                            }
                        } catch (Exception e) {
                            env.BACKEND_BUILD_STATUS = 'FAILED'
                            echo "❌ 백엔드 빌드 실패: ${e.message}"
                            throw e
                        }
                    }
                }
            }
        }   
        stage('Build Frontend') {
            when {
                expression { env.FRONTEND_CHANGED == "true" }
            }
            steps {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    script {
                        try {
                            env.FRONTEND_BUILD_STATUS = 'SUCCESS'
                            withCredentials([file(credentialsId: "frontend", variable: 'FRONTEND_ENV')]) {
                                sh '''
                                    cp "$FRONTEND_ENV" "$WORKSPACE/frontend/.env"
                                '''
                            }
                            dir('frontend') {
                                sh '''
                                    set -e
docker build -f Dockerfile -t vue .
docker stop vue || true
docker rm vue || true
docker run -d --network mynet --env-file .env --restart unless-stopped --name vue -p 3000:3000 vue

                                '''
                            }
                        } catch (Exception e) {
                            env.FRONTEND_BUILD_STATUS = 'FAILED'
                            echo "❌ 프론트엔드 빌드 실패: ${e.message}"
                            throw e
                        }
                    }
                }
            }
        }
        stage('Health Check') {
            steps {
                // Health Check 전에 30초 대기
                echo '⏳ Health Check 전에 30초 대기'
                sleep time: 30, unit: 'SECONDS'
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    script {
                        // 헬스 체크 로직 추가
                        echo '⚕️ 서비스 헬스 체크 실행'
                        env.HEALTH_CHECK_STATUS = 'SUCCESS' // 기본값 설정
                        
                        // Docker API를 통한 컨테이너 상태 확인 URL
                        def dockerApiUrl = 'http://localhost:3789/containers/json?all=true&filters=%7B%22name%22%3A%5B%22spring%22%5D%7D'
                        
                        try {
                            // Docker API 호출
                            def dockerApiResponse = sh(script: """
                                curl -s -X GET '${dockerApiUrl}'
                            """, returnStdout: true).trim()
                            
                            echo "Docker API 응답: ${dockerApiResponse}"
                            
                            // JSON 응답 파싱
                            def jsonSlurper = new groovy.json.JsonSlurper()
                            def containers
                            try {
                                containers = jsonSlurper.parseText(dockerApiResponse)
                            } catch (Exception e) {
                                echo "JSON 파싱 오류: ${e.message}"
                                env.HEALTH_CHECK_STATUS = 'FAILED'
                                error "헬스 체크 실패: JSON 파싱 오류"
                            }
                            
                            // 컨테이너 목록 확인
                            if (containers instanceof List) {
                                if (containers.size() == 0) {
                                    echo "❌ 헬스 체크 실패: spring 컨테이너를 찾을 수 없습니다."
                                    env.HEALTH_CHECK_STATUS = 'FAILED'
                                    error "헬스 체크 실패: spring 컨테이너를 찾을 수 없습니다."
                                }
                                
                                // 컨테이너 상태 확인
                                def springContainer = containers[0]
                                def containerState = springContainer.State
                                def containerStatus = springContainer.Status
                                
                                echo "컨테이너 상태: ${containerState}, 상태 설명: ${containerStatus}"
                                
                                // 'running' 상태인지 확인
                                if (containerState == 'running') {
                                    echo "✅ 헬스 체크 성공: spring 컨테이너가 정상 실행 중입니다."
                                    env.HEALTH_CHECK_STATUS = 'SUCCESS'
                                } else {
                                    echo "❌ 헬스 체크 실패: spring 컨테이너 상태가 '${containerState}'입니다."
                                    env.HEALTH_CHECK_STATUS = 'FAILED'
                                    error "헬스 체크 실패: spring 컨테이너 상태가 '${containerState}'입니다."
                                }
                            } else {
                                echo "❌ 헬스 체크 실패: Docker API 응답이 리스트 형식이 아닙니다."
                                env.HEALTH_CHECK_STATUS = 'FAILED'
                                error "헬스 체크 실패: Docker API 응답이 리스트 형식이 아닙니다."
                            }
                        } catch (Exception e) {
                            echo "❌ 헬스 체크 실행 중 오류 발생: ${e.message}"
                            env.HEALTH_CHECK_STATUS = 'FAILED'
                            throw e
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                // 빌드 결과 상태 가져오기
                def buildStatus = currentBuild.result ?: 'SUCCESS'
                env.SELF_HEALING_APPLIED = 'false'  // 셀프 힐링 적용 여부를 추적하는 변수
                
                // PROJECT_ID 파라미터가 비어있지 않은지 확인
                if (params.PROJECT_ID?.trim()) {
                    withCredentials([usernamePassword(credentialsId: 'gitlab-token', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                        // API 기본 URL 설정
                        def apiBaseUrl = 'https://seedinfra.store/api'
                        
                        // 셀프 힐링 API 호출 함수 정의
                        def callSelfHealingApi = { failType ->
                            def healingApiUrl = "${apiBaseUrl}/self-cicd/resolve"
                            def queryParams = "projectId=${params.PROJECT_ID}&personalAccessToken=${GIT_TOKEN}&failType=${failType}"
                            
                            try {
                                def healingResponse = sh(script: """
                                    curl -X POST \
                                    -H 'Content-Type: application/json' \
                                    -w '\n%{http_code}' \
                                    "${healingApiUrl}?${queryParams}" 
                                """, returnStdout: true).trim()
                                
                                echo "셀프 힐링 API 호출 결과 (${failType}): ${healingResponse}"
                                env.SELF_HEALING_APPLIED = 'true'
                            } catch (Exception e) {
                                echo "셀프 힐링 API 호출 실패 (${failType}): ${e.message}"
                            }
                        }
                        
                        // 셀프 힐링 API 호출 조건 확인
                        if (params.BRANCH_NAME == params.ORIGINAL_BRANCH_NAME && currentBuild.number > 1) {
                            // 빌드 상태 변수 확인 (안전하게 처리)
                            def frontendFailed = (env.FRONTEND_BUILD_STATUS == 'FAILED')
                            def backendFailed = (env.BACKEND_BUILD_STATUS == 'FAILED')
                            def healthCheckFailed = (env.HEALTH_CHECK_STATUS == 'FAILED')
                            
                            // 변경되지 않아 실행되지 않은 경우 처리
                            if (env.FRONTEND_CHANGED == 'false') {
                                frontendFailed = false
                                echo "ℹ️ 프론트엔드는 변경되지 않아 빌드가 실행되지 않았습니다."
                            }
                            if (env.BACKEND_CHANGED == 'false') {
                                backendFailed = false
                                echo "ℹ️ 백엔드는 변경되지 않아 빌드가 실행되지 않았습니다."
                            }
                            
                            echo "📊 빌드 상태 요약:\n- 프론트엔드: ${frontendFailed ? '❌ 실패' : '✅ 성공'}\n- 백엔드: ${backendFailed ? '❌ 실패' : '✅ 성공'}\n- 헬스 체크: ${healthCheckFailed ? '❌ 실패' : '✅ 성공'}"
                            
                            // 케이스 1: 프론트엔드 빌드 실패, 백엔드 빌드 성공, 헬스 체크 성공
                            if (frontendFailed && !backendFailed && !healthCheckFailed) {
                                echo "🛠️ 케이스 1: 프론트엔드 빌드 실패 → 셀프 힐링 API 호출 (BUILD)"
                                callSelfHealingApi('BUILD')
                            }
                            // 케이스 2: 프론트엔드 빌드 실패, 백엔드 빌드 성공, 헬스 체크 실패
                            else if (frontendFailed && !backendFailed && healthCheckFailed) {
                                echo "🛠️ 케이스 2: 프론트엔드 빌드 실패 및 헬스 체크 실패 → 셀프 힐링 API 호출 (RUNTIME)"
                                callSelfHealingApi('RUNTIME')
                            }
                            // 케이스 3: 프론트엔드 빌드 성공, 백엔드 빌드 성공, 헬스 체크 성공
                            else if (!frontendFailed && !backendFailed && !healthCheckFailed) {
                                echo "✅ 케이스 3: 모든 빌드 및 헬스 체크 성공 → 셀프 힐링 필요 없음"
                            }
                            // 케이스 4: 프론트엔드 빌드 성공, 백엔드 빌드 성공, 헬스 체크 실패
                            else if (!frontendFailed && !backendFailed && healthCheckFailed) {
                                echo "🛠️ 케이스 4: 헬스 체크 실패 → 셀프 힐링 API 호출 (RUNTIME)"
                                callSelfHealingApi('RUNTIME')
                            }
                            // 추가 케이스: 백엔드 빌드 실패
                            else if (backendFailed) {
                                echo "🛠️ 추가 케이스: 백엔드 빌드 실패 → 셀프 힐링 API 호출 (BUILD)"
                                callSelfHealingApi('BUILD')
                            }
                            // 예상치 못한 케이스
                            else {
                                echo "⚠️ 예상치 못한 상태: 빌드 상태 ${buildStatus}\n- 정확한 진단을 위해 Jenkins 로그를 확인하세요."
                                if (buildStatus != 'SUCCESS') {
                                    echo "❌ 빌드 실패 (기타 케이스) → 셀프 힐링 API 호출 (BUILD)"
                                    callSelfHealingApi('BUILD')
                                }
                            }
                        } else {
                            echo "💬 원본 브랜치와 다른 브랜치 빌드 또는 첫 빌드 → 셀프 힐링 건너뜀"
                        }
                        
                        // 모든 작업이 완료된 후 마지막으로 빌드 로그 API 호출 (성공/실패 여부 무관)
                        echo "📝 최종 빌드 결과 로깅 API 호출 중: 프로젝트 ID ${params.PROJECT_ID}"
                        
                        // 빌드 로그 API 엔드포인트 구성
                        def logApiUrl = "${apiBaseUrl}/jenkins/${params.PROJECT_ID}/log-last-build"
                        
                        // 빌드 로그 API 호출 (POST 요청, 빈 본문)
                        try {
                            def logResponse = sh(script: """
                                curl -X POST \
                                -H 'Content-Type: application/json' \
                                -w '\n%{http_code}' \
                                ${logApiUrl}
                            """, returnStdout: true).trim()
                            
                            echo "빌드 로그 API 호출 결과: ${logResponse}"
                        } catch (Exception e) {
                            echo "빌드 로그 API 호출 실패: ${e.message}"
                        }
                    }
                } else {
                    echo "PROJECT_ID 파라미터가 비어있어 API를 호출하지 않습니다."
                }
            }
        }
    }
}
