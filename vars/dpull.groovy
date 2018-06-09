def call(image_name) {
    sh """
        docker pull sami:5000/${image_name}
        docker tag sami:5000/${image_name} ${image_name}
    """
}
