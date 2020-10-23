var current_date = new Date();

var current_agent = "Current Agent"; //store dynamically based on agent info
var subButton = document.getElementById('subButton');

subButton.addEventListener('click', build_note_json, false);

//s3 object needs to be initialized

function build_note_json()
{
    let note = {
        email: document.getElementById('email').value,
        agent: current_agent,
        date:  current_date.toDateString(),
        notes: document.getElementById('notes').value,
    }

    let filepath = "Notes/" + 
        current_date.getFullYear().toString() + "/" + 
        current_date.getMonth().toString() + "/" + 
        current_date.getDate().toString() + "/" + 
        current_date.getHours().toString() + ":" +
        current_date.getMinutes().toString + ":" +
        current_date.getSeconds().toString + ".json";

    store_note_json(note, filepath);
}

function store_note_json(note, filepath)
{
    let note_json = JSON.stringify(note);

    const note_binary = note_json.split(' ').map(i => String.fromCharCode(parseInt(i, 2)).toString(10)).join('');
    
    var params = {
        Body: note_binary, 
        Bucket: "macs_notes", 
        Key: filepath, 
        ServerSideEncryption: "AES256", //might need to be changed?
    };
    s3.putObject(params, function(err, data) {
        if (err) console.log(err, err.stack); // an error occurred
        else console.log(data);           // successful response
        /*
        data = {
        ETag: "\"6805f2cfc46c0f04559748bb039d69ae\"", 
        ServerSideEncryption: "AES256", 
        VersionId: "Ri.vC6qVlA4dEnjgRV4ZHsHoFIjqEMNt"
        }
        */
    });
}