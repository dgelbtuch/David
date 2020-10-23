# Macs Agent and Client UI

# Agent

The Agent UI is split up into three sections: the chat, previous notes and transcripts, and notes submissions. 

## Installation

The following javascript files are required for the CCP to initiate:

```javascript
    <script src = "amazon-connect-streams-master/amazon-connect-1.4.9-1-gf9242a0.js"></script>
    <script src = "amazon-connect-chat.js"></script>
    <script src = "https://sdk.amazonaws.com/js/aws-sdk-2.716.0.min.js"></script>
```
The Agent UI also requires additional code for receiving transcripts and notes as well as submitting notes:

```javascript
    <script src = 'get_transcriptandnotes.js'></script>
    <script src = 'submit_notes.js'></script>
```

## Login

When an agent accesses the agent page, he will be sent to an Amazon Connect login page and will close upon login. Agents' accounts should be set up through Amazon Connect.

## Chat

The chat uses Amazon's Contact Control Panel (CCP) where agents can chat with contacts on a single interface. The moment page loads, the init() function is called which launches the CCP.

For more information on the init() function and many other functions associated with Amazon Connect, please read the [Amazon Connect Streams Documentation](https://github.com/amazon-connect/amazon-connect-streams/blob/master/Documentation.md)

## Notes and Transcripts

All code for receiving previous notes and transcripts can be found in the get_transcriptandnotes.js file.

An agent has access to previous chat transcripts and notes. The agent can simply access transcripts and notes from the current contact he/she is speaking to. The agent need only select a current contact's email from the drop-down then click the "Get Previous Transcripts" button to receive all previous information needed with the associated contact. The agent also has the option to search previous transcripts/notes by typing in an email and clicking the "search" button right below the "Get Previous Transcripts" button.

*Please note that all notes and transcripts are associated with a contacts email.

Clicking the "Get Previous Transcripts" button calls the printTranscriptandNotes() function which, based on the contact's email, will access the macs-users S3 bucket. This bucket contains the S3 locations of the current contact's transcripts and notes. The code will then retrieve a json file from the respective S3 bucket and parse it in order to display it in one of the two text boxes located below the previously mentioned buttons.

*In order to access the S3 bucket, the js code must contain the account credentials credentials of the s3 buckets; however, this must be done in a secure way without publicly display the accounts sensitive information. Furthermore, the S3 buckets require CORES configuration in order to be accessed from a browser. Please look into the S3 bucket examples provided by [Amazon's Javascript SDK Documentation](https://docs.amazonaws.cn/en_us/sdk-for-javascript/v2/developer-guide/s3-example-photos-view.html).

## Submitting Notes

All code for submitting notes can be found in the submit_notes.js file.

During a chat, the agent is able to take notes about the current contact and submit them by clicking the "Submit For Current User" button. An Agent is also able to take notes on clients who are not currently in contact with the agent. The agent must type in the email of a previous client and click the "Submit For:" button.

Clicking either of these buttons will take the email of the current client or searched client, make it into a json file, and send it to the macs-notes 

# Client

## Instillation 

The following javascript files are required for the chat initiation:

 ```javascript
    <script src="https://code.jquery.com/jquery-3.1.0.min.js"></script>
    <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
    <script src="amazon-connect-chat-interface.js"></script>
 ```

## Chat Initiation

Once the page loads, the connect.ChatInterface.init() function is called to initialize the chat interface, Then once the
the chat widget icon is clicked, the connect.ChatInterface.initiateChat() is called. In order for the initiateChat() function to work, the function must be given, the Contact Flow ID, the Amazon Connect Instance ID, and the Amazon API Gateway URL.

For a complete understanding on how these functions and how to get the required information please read the [Amazon startChatContactAPI](https://github.com/amazon-connect/amazon-connect-chat-ui-examples/tree/master/cloudformationTemplates/startChatContactAPI). This will give you a step by step process on how to make the API Gateway for the chat to work and where to find the various ID's mentioned earlier.