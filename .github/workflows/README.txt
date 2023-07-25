схема зависимостей CI

APPLICATION_Build_Android_artifacts (application_build.yml) :  debug-build     release-build

BALANCER___API_client_generation_using_swagger (balancer_api_codegen.yml) :   generate-swagger ->  generate-code -> balancer-push

BALANCER___Publish_docker_image_to_Docker_Hub (balancer_build_docker.yaml)  зависит от BALANCER___API_client_generation_using_swagger (balancer_api_codegen.yml)
BALANCER___Publish_docker_image_to_Docker_Hub (balancer_build_docker.yaml) : balancer-makemigartions -> balancer-test -> publish-to-dockerhub

BALANCER___Django_tests (balancer_django_tests.yaml) : test

SERVICE___API_service_generation_using_swagger (service_api_codegen.yml) : service-generate-swagger -> service-generate-code -> service-push -> service-test

SERVICE___Publish_docker_image_to_Docker_Hub (service_build_docker.yml) зависит от SERVICE___API_service_generation_using_swagger (service_api_codegen.yml) 
SERVICE___Publish_docker_image_to_Docker_Hub (service_build_docker.yml) :  build-iperf -> speedtest-service -> publish-to-dockerhub -> 

artifact (making_artifacts.yml) зависит от BALANCER___Publish_docker_image_to_Docker_Hub (balancer_build_docker.yaml), SERVICE___Publish_docker_image_to_Docker_Hub (service_build_docker.yml) 
artifact (making_artifacts.yml) : make_artifacts
