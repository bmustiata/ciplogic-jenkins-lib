def call(image_name) {
    sh """
        docker pull ge-registry:80/${image_name}
        docker tag ge-registry:80/${image_name} ${image_name}
    """
}
