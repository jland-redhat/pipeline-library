#!/usr/bin/env groovy

class ImageMirrorInput implements Serializable {
    //Required
    String sourceSecret = ""
    String sourceRegistry = ""
    String destinationSecret = ""
    String destinationRegistry = ""
    String insecure = "false"
    String sourceNamespace = ""
    String destinationNamespace = ""
    String sourceImage = ""
    String destinationImage = ""
    String sourceImageVersion = "latest"
    String destinationImageVersion = "latest"

    //Optional - Platform
    Integer loglevel = 0

    ImageMirrorInput init() {
        if(!destinationImage?.trim()) destinationImage = sourceImage
        if(!destinationNamespace?.trim()) destinationNamespace = sourceNamespace
        return this
    }
}

def call(Map input) {
    call(new ImageMirrorInput(input).init())
} 

def call(ImageMirrorInput input) {
    assert input.sourceSecret?.trim(): "Param sourceSecret should be defined."
    assert input.sourceRegistry?.trim(): "Param sourceRegistry should be defined."
    assert input.destinationSecret?.trim(): "Param destinationSecret should be defined."
    assert input.destinationRegistry?.trim(): "Param destinationRegistry should be defined."
    assert input.sourceNamespace?.trim(): "Param sourceNamespace should be defined."
    assert input.sourceImage?.trim(): "Param sourceImage should be defined."
    assert input.destinationNamespace?.trim(): "Param destinationNamespace should be defined."
    assert input.destinationImage?.trim(): "Param destinationImage should be defined."
    assert input.destinationImageVersion?.trim(): "Param destinationImageVersion should be defined."
    assert input.insecure?.trim(): "Param insecure should be defined."

    openshift.loglevel(input.loglevel)

    String sourceApi = input.sourceRegistry.replaceFirst("^(http[s]?://\\.|http[s]?://)", "")
    String destinationApi = input.destinationRegistry.replaceFirst("^(http[s]?://\\.|http[s]?://)", "")

    withDockerRegistry([credentialsId: "${input.sourceSecret}", url: "${input.sourceRegistry}"]) {
        withDockerRegistry([credentialsId: "${input.destinationSecret}", url: "${input.destinationRegistry}"]) {
            openshift.withCluster() {
                def source = "${sourceApi}/${input.sourceNamespace}/${input.sourceImage}:${input.sourceImageVersion}"
                def destination = "${destinationApi}/${input.destinationNamespace}/${input.destinationImage}:${input.destinationImageVersion}"

                echo "Attempting to mirror; ${source} -> ${destination}"

                openshift.raw("image", "mirror", source, destination, "--insecure=${input.insecure}")
            }
        }
    }
}





