function checkContentType(url){
    if (url.match(/\.(jpeg|jpg|gif|png|bmp|svg)$/) != null) {
        return "image";
    } else if (url.match(/\.(webm|mp4|mov|avi)$/) != null) {
        return "video";
    }
}


var videoToLoad = "front";
var nextContentType = "video";
function loadContent(content) {
    var contentType = checkContentType(content);
    nextContentType = contentType;

    var frontImage = document.getElementById('image_front');
    var frontVideo = document.getElementById('video_front');
    var backVideo = document.getElementById('video_back');
    var backSource = document.getElementById('source_back');
    var frontSource = document.getElementById('source_front');

    // we swap between two video players such that none of them will be visible while loading a new video.
    // if we wouldn't do it this way, the screen would be black in between video swaps
    if (contentType == "video") {
        if (videoToLoad == "front") {
            frontSource.setAttribute('src', content);
            frontVideo.load();
            frontVideo.pause();
            videoToLoad = "back";
        } else {
            backSource.setAttribute('src', content);
            backVideo.load();
            backVideo.pause();
            videoToLoad = "front";
        }
    } else {
        frontImage.setAttribute('src', content);
    }
}


var videoToShow = "front";
function swapContent() {
    var playDelay = 300;
    var imagePlayTime = 6*1000;

    var frontImage = document.getElementById('image_front');
    var frontVideo = document.getElementById('video_front');
    var backVideo = document.getElementById('video_back');
    var backSource = document.getElementById('source_back');
    var frontSource = document.getElementById('source_front');

    var showBackVideo = function() {
        backVideo.style.display = "block";
        frontVideo.style.display = "none";
        frontImage.style.display = "none";
    }

    var showFrontVideo = function() {
        frontVideo.style.display = "block";
        backVideo.style.display = "none";
        frontImage.style.display = "none";
    }

    var showImage = function() {
        frontImage.style.display = "block";
        backVideo.style.display = "none";
        frontVideo.style.display = "none";
    }

    // we swap between two video players such that none of them will be visible while loading a new video.
    // if we wouldn't do it this way, the screen would be black in between video swaps
    if (nextContentType == "video") {
        if (videoToShow == "front") {
            // if the video is ready, show it. otherwise, set an event handler to show it when it is ready.
            if (frontVideo.readyState >= 2) { // HAVE_CURRENT_DATA
                frontVideo.play();
                window.setTimeout(showFrontVideo, playDelay);
            } else {
                var frontVideoLoadedHandler = function() {
                    frontVideo.play();
                    window.setTimeout(showFrontVideo, playDelay);
                    frontVideo.removeEventListener("loadeddata", frontVideoLoadedHandler)
                }
                frontVideo.addEventListener("loadeddata", frontVideoLoadedHandler)
            }
            videoToShow = "back";
        } else {
            // if the video is ready, show it. otherwise, set an event handler to show it when it is ready.
            if (backVideo.readyState >= 2) { // HAVE_CURRENT_DATA
                backVideo.play();
                window.setTimeout(showBackVideo, playDelay);
            } else {
                var backVideoLoadedHandler = function() {
                    backVideo.play();
                    window.setTimeout(showBackVideo, playDelay);
                    backVideo.removeEventListener("loadeddata", frontVideoLoadedHandler)
                }
                frontVideo.addEventListener("loadeddata", frontVideoLoadedHandler)
            }
            videoToShow = "front";
        }
    } else {
        showImage();
        window.setTimeout(swapContent, imagePlayTime);
    }
}



var videoToChange = "front";
function setContent(content) {
    var contentType = checkContentType(content);

    var frontImage = document.getElementById('image_front');
    var frontVideo = document.getElementById('video_front');
    var backVideo = document.getElementById('video_back');
    var backSource = document.getElementById('source_back');
    var frontSource = document.getElementById('source_front');

    // we swap between two video players such that none of them will be visible while loading a new video.
    // if we wouldn't do it this way, the screen would be black in between video swaps
    if (contentType == "video") {
        if (videoToChange == "front") {
            frontSource.setAttribute('src', content);
            frontVideo.load();
            frontVideo.play();

            var frontVideoLoadedHandler = function() {
                frontVideo.style.display = "block";
                backVideo.style.display = "none";
                frontImage.style.display = "none";
                frontVideo.removeEventListener("loadeddata", frontVideoLoadedHandler)
            }
            frontVideo.addEventListener("loadeddata", frontVideoLoadedHandler)
            videoToChange = "back";
        } else {
            backSource.setAttribute('src', content);
            backVideo.load();
            backVideo.play();

            var backVideoLoadedHandler = function() {
                backVideo.style.display = "block";
                frontVideo.style.display = "none";
                frontImage.style.display = "none";
                backVideo.removeEventListener("loadeddata", frontVideoLoadedHandler)
            }
            frontVideo.addEventListener("loadeddata", frontVideoLoadedHandler)
            videoToChange = "front";
        }
    } else {
        frontImage.setAttribute('src', content);
        frontImage.style.display = "block";
        backVideo.style.display = "none";
        frontVideo.style.display = "none";
    }
}
