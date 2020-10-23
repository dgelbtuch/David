let email = "js@mail.com"; //will be inputted dynamically
email = email.replace(".", "_");

AWS.config.update({
    accessKeyId: 'KEYID',
    secretAccessKey: 'ACCESSKEY',
    region: 'us-east-1' 
});
var s3 = new AWS.S3();
var params = {
    Bucket: 'macs-users',
    Key: email + ".json"
};
s3.getObject(params, function(err, data) {
    if (err) console.log(err, err.stack); // an error occurred
    else  // successful response
    {
       parse_user(data.Body.toString('ascii'));          
    }
});



function parse_user(user_json)
{        
    user_json = JSON.parse(user_json);
    
    console.log("CHAT HISTORY");
    for (address of user_json.transcript_address_list)
    {
        access_transcript_object(address);
    }

    console.log("NOTES");
    for (address of user_json.note_address_list)
    {
        access_note_object(address);
    }
   
}

function access_transcript_object(transcript_address)
{   
    let parameters = {
        Bucket: transcript_address.Bucket,
        Key: transcript_address.Key
    };
    
    s3.getObject(parameters, function(err, data) 
    {
        if (err) console.log(err, err.stack); // an error occurred
        else 
        {
            transcript = JSON.parse(data.Body.toString('ascii')).Transcript;    
            console.log(parse_transcript(transcript));    // successful response
        }    
    });


}

function access_note_object(note_address)
{   
    let parameters = {
        Bucket: note_address.Bucket,
        Key: note_address.Key
    };
    
    s3.getObject(parameters, function(err, data) 
    {
        if (err) console.log(err, err.stack); // an error occurred
        else 
        {
            console.log(JSON.parse(data.Body.toString('ascii')).Notes);   // successful response
        }    
    });
}

function parse_transcript(transcript, output)
{
    let date = parseDate(transcript[0].AbsoluteTime)
    let current_transcript_output = date + "\n";
    for (message of transcript)
    {
        if (message.ContentType == "application/vnd.amazonaws.connect.event.participant.joined")
        {
            current_transcript_output += ("** " + message.DisplayName + " has joined the chat **\n");
        }

        else if (message.ContentType == "text/plain")
        {
            name = (message.ParticipantRole == "SYSTEM")? "MACS" : message.DisplayName;
            current_transcript_output += (name + ": " + message.Content + "\n");
        }
    }
    //console.log(current_transcript_output);
    return current_transcript_output;
}

function parseDate(timeStamp)
{
    arr = timeStamp.split("");
    year = arr[0] + arr[1] + arr[2] + arr[3];
    month = arr[5] + arr[6]
    day = arr[8] + arr[9]
    return (month + "/" + day + "/" + year);
}



