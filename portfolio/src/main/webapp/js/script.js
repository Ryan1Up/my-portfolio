function loadNav() {
    
    $(".navbar").load("../html/nav.html");
}

function loadFooter() {
    $(".footer").load("../html/footer.html");
}

function onLinkLoad(){
    document.querySelectorAll(".about_me_nav").forEach(item => item.addEventListener("click", event => {
        event.preventDefault();
    }, false))
    
}

function loadAboutNavMain() {
    $("#about-content").load("../html/aboutMeContent.html");
    console.log("About Me");
}
function loadAboutNavEducation(event) {
    console.log("Education");
}
function loadAboutNavSkills(event) {
   
    $("#about-content").load("../html/languages.html");
}