function loadNav() {
    
    $(".navbar").load("../nav.html");
}

function loadFooter() {
    $(".footer").load("../footer.html");
}

function onLinkLoad(){
    document.querySelectorAll(".about_me_nav").forEach(item => item.addEventListener("click", event => {
        event.preventDefault();
    }, false))
}

function loadAboutNavMain() {
    $("#about-content").load("../aboutMeContent.html");
    console.log("About Me");
}

function loadAboutNavEducation() {
    $("#about-content").load("../aboutMeEducation.html");
}

function loadAboutNavSkills() {
   
    $("#about-content").load("../languages.html");
}