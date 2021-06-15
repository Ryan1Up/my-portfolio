

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

var servletList = {};
var hasRequested = false;

async function getJson() {
    hasRequested = true;
    const responseFromServer = await fetch("/JsonMsg");
    const jsonFromResponse = await responseFromServer.json();

    console.log(jsonFromResponse);

    const responseContainer = document.getElementById("jsonResponseP");

    var newServletList = Object.keys(jsonFromResponse);

    console.log(servletList);
    if(newServletList !== servletList){
        servletList = newServletList;
        responseContainer.innerHTML = "";

        for(field in servletList){
            responseContainer.innerHTML += "<div class=\"card btn btn-outline\">" + servletList[field] + "</div>";
        }
    }
    
    
    
}

function loadForm() {

    $(".mainForm").load("../form.html");

    if(sessionStorage.getItem("contacted") === "true"){
        console.log("Contacted");
        loadThankYou();
    }
    loadThankYou();
}

function loadThankYou() {
    document.getElementById("contactFromContainer").innerHTML = "<h3 style=\"padding: 30px;\">Thank You!</h3>";
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



const cDesc =  "<h3>C++</h3><p>I first started to learn to code using C++. I am familiar with most of its basic concepts enough" + 
    " to do basic data and file processing. Most of my early projects were in C++, and I still" +
    " plan to use it extensively in the future.</p>"; 

const javaDesc = "<h3>Java</h3><p>Most of my experience in programming has been in Java. I am familiar with the Swing and JavaFx libraries" +
                  " to help make GUI's. I am planning to use my experience in SPS to elevate my abilities with Java, so that I am " + 
                  "able to start writing programs the utlize transfer and authentication over the web. I am currently studying concurrency" + 
                  " in Java in hopes to make my programs more efficient if possible.</p>"; 

const rubyDesc = "<h3>Ruby</h3><p>Funnily enough, my motivations for learning Ruby was purely to fixed a buggy RPG game made with" + 
                  "RGSS3. My experience with it is very limited to the scope of the Ruby Game Scripting System, but I do have plans" + 
                  " to learn more about Ruby on Rails as well</p>";

const pythonDesc = "<h3>Python</h3><p>I have learned python in the past, but have rarely used it. Most of my experience around" + 
                    " python involves debugging other's programs and helping friends add features to their Ren'Py VNs." + 
                    " I do hope to use it more in the future for other programs.</p>"; 

function loadDesc(arg) {
    const card = document.getElementById("langDesc");
    if(arg === 'C++'){
        card.innerHTML = cDesc;
    }
    else if (arg === 'Java'){
        card.innerHTML = javaDesc;
    }
    else if (arg === 'Ruby'){
        card.innerHTML = rubyDesc;
    }
    else if (arg === 'Python'){
        card.innerHTML = pythonDesc;
    }
    

}

