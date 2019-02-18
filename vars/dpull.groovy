def call(image_name) {
    sh """
        docker pull ge-registry/${image_name}
        docker tag ge-registry/${image_name} ${image_name}
    """
}
