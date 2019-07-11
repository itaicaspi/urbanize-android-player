function checkContentType(url){
    if (url.match(/\.(jpeg|jpg|gif|png|bmp|svg)$/) != null) {
        return "image";
    } else if (url.match(/\.(webm|mp4|mov|avi)$/) != null) {
        return "video";
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
            window.setTimeout(function() {
                frontVideo.style.display = "block";
                backVideo.style.display = "none";
                frontImage.style.display = "none";
            }, 500);

            videoToChange = "back";
        } else {
            backSource.setAttribute('src', content);
            backVideo.load();
            backVideo.play();
            window.setTimeout(function() {
                backVideo.style.display = "block";
                frontVideo.style.display = "none";
                frontImage.style.display = "none";
            }, 500);
            videoToChange = "front";
        }
    } else {
        frontImage.setAttribute('src', content);
        frontImage.style.display = "block";
        backVideo.style.display = "none";
        frontVideo.style.display = "none";
    }
}
