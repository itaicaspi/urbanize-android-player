function checkContentType(url){
    if (url.match(/\.(jpeg|jpg|gif|png|bmp|svg)$/) != null) {
        return "image";
    } else if (url.match(/\.(webm|mp4|mov|avi)$/) != null) {
        return "video";
    }
}


var videoToLoad = "front";
var imageToLoad = "front";
var nextContentType = "video";
function loadContent(content, delay=0) {
    var contentType = checkContentType(content);
    nextContentType = contentType;

    var frontImage = document.getElementById('image_front');
    var backImage = document.getElementById('image_back');
    var frontVideo = document.getElementById('video_front');
    var backVideo = document.getElementById('video_back');
    var backSource = document.getElementById('source_back');
    var frontSource = document.getElementById('source_front');

    // we swap between two video players such that none of them will be visible while loading a new video.
    // if we wouldn't do it this way, the screen would be black in between video swaps
    if (contentType == "video") {
        // load the next video into place (it is still hidden behind the current video)
        if (videoToLoad == "front") {
            frontSource.setAttribute('src', content);
            frontVideo.load();
            frontVideo.currentTime = 0.1;  // this is needed in order to show the video and prevent a black screen
            frontVideo.pause();
            videoToLoad = "back";
        } else {
            backSource.setAttribute('src', content);
            backVideo.load();
            backVideo.currentTime = 0.1; // this is needed in order to show the video and prevent a black screen
            backVideo.pause();
            videoToLoad = "front";
        }
    } else {
        if (imageToLoad == "front") {
            frontImage.setAttribute('src', content);
            imageToLoad = "back";
        } else {
            backImage.setAttribute('src', content);
            imageToLoad = "front";
        }
    }
}


var videoToShow = "front";
var imageToShow = "front";
function swapContent() {
    var playDelay = 1;
    var hideDelay = 300;
    var imagePlayTime = 6*1000;

    var frontImage = document.getElementById('image_front');
    var backImage = document.getElementById('image_back');
    var frontVideo = document.getElementById('video_front');
    var backVideo = document.getElementById('video_back');
    var backSource = document.getElementById('source_back');
    var frontSource = document.getElementById('source_front');

    var showBackVideo = function() {
        backVideo.style.display = "block";
        window.setTimeout(function() {
            frontVideo.style.display = "none";
            frontImage.style.display = "none";
            backImage.style.display = "none";
        }, hideDelay)
    }

    var showFrontVideo = function() {
        frontVideo.style.display = "block";
        window.setTimeout(function() {
            backVideo.style.display = "none";
            frontImage.style.display = "none";
            backImage.style.display = "none";
        }, hideDelay)
    }

    var showFrontImage = function() {
        frontImage.style.display = "block";
        window.setTimeout(function() {
            backVideo.style.display = "none";
            frontVideo.style.display = "none";
            backImage.style.display = "none";
        }, hideDelay);
    }

    var showBackImage = function() {
        backImage.style.display = "block";
        window.setTimeout(function() {
            backVideo.style.display = "none";
            frontVideo.style.display = "none";
            frontImage.style.display = "none";
        }, hideDelay);
    }

    // we swap between two video players such that none of them will be visible while loading a new video.
    // if we wouldn't do it this way, the screen would be black in between video swaps
    if (nextContentType == "video") {
        if (videoToShow == "front") {
            // if the video is ready, show it. otherwise, set an event handler to show it when it is ready.
            if (frontVideo.readyState >= 3) { // HAVE_CURRENT_DATA
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
            if (backVideo.readyState >= 3) { // HAVE_CURRENT_DATA
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
        if (imageToShow == "front") {
            showFrontImage();
            imageToShow = "back";
        } else {
            showBackImage();
            imageToShow = "front";
        }
        window.setTimeout(function() {
            swapContent();
            injectedObject.videoEnded();
        }, imagePlayTime);
    }
}

var sanitizeHTML = function (str) {
	var temp = document.createElement('div');
	temp.textContent = str;
	return temp.innerHTML;
};

function setInfoTicker(entries) {
    var infoTicker = document.getElementById("info-ticker");
    var numEntries = entries.length;
    while (infoTicker.firstChild) {
        infoTicker.removeChild(infoTicker.firstChild);
    }
    for (var i = 0; i < numEntries; i++) {
        var tickerItem = document.createElement("div");
        tickerItem.classList.add("ticker__item");
        tickerItem.setAttribute("dir", "auto")
        var tickerItemTitle = document.createElement("h4");
        tickerItemTitle.classList.add("font-weight-bold");
        tickerItemTitle.textContent = entries[i]["title"];
        var tickerItemText = document.createElement("h5");
        tickerItemText.textContent = entries[i]["text"];
        tickerItem.appendChild(tickerItemTitle);
        tickerItem.appendChild(tickerItemText);
        infoTicker.appendChild(tickerItem);
//        html += "<div class='ticker__item' dir=auto><h4><b>" + sanitizeHTML(entries[i]["title"]) + "</b></h4><h5>" + sanitizeHTML(entries[i]["text"]) + "</h5></div>\n";
    }
//    infoTicker.innerHTML = html;
}
