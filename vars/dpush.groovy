def call(image_name) {
    sh """
        docker tag ${image_name} sami:5000/${image_name}
        docker push sami:5000/${image_name}
    """
}
