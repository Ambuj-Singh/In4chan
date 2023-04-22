# In4chan

## End-to-End Encrypted chat application

## System Design

In this project I used ***Firebase*** as the server and for frontend, I used android xml. 

* It has a persistent server which means even if you lose network, whenever the internet connection is live again it will receive and send message automatically.
* Chats are stored on the local database using ***Android*** and ***SQLite***.
```mermaid
sequenceDiagram
    Alice->>+John: Hello John, how are you?
    Alice->>+John: John, can you hear me?
    John-->>-Alice: Hi Alice, I can hear you!
    John-->>-Alice: I feel great!
```

* Server only holds the message as long as the receiver is offline.</br>
<img src="https://user-images.githubusercontent.com/39789077/233762402-9b339e08-a36a-4e4f-8da3-67c9947b8eca.png" ></br>
* Messages sent to server are encrypted on the client device first and then decoded on the receiver device. 
```mermaid
flowchart TD;
    A[Sender App] -->|Message| B(Encoder);
    B --> |Encoded Message| C{Server};
    C -->|Encoded Message| D[Receiver App];
    D --> |Encoded Message| E(Decoder);
    E --> |Decoded Message| F(Receiver);
```

## System GUI

The app is easy-to-use and user-friendly. Everything is labeled and easy to access.

* **Log in** and **Sign up**.</br>
<img src="https://user-images.githubusercontent.com/39789077/229834227-4926ab93-d8cb-482b-a354-b4c96f68f7f3.jpg" width="150"> <img src="https://user-images.githubusercontent.com/39789077/229834241-dce44c14-4cd9-4043-a100-1a19452489a3.jpg" width="150"></br>
* **Profile List** and **Chat List**.</br>
<img src="https://user-images.githubusercontent.com/39789077/233762520-fbed45eb-aee5-422e-9c9d-9813c06d342c.jpg" width="150"> <img src="https://user-images.githubusercontent.com/39789077/233762533-133aabbd-4369-4601-9ed6-6f0a1e1b4cdf.jpg" width="150"></br>
* **Sending Message** and **Clearing Chat** UI flow.</br>
<img src="https://user-images.githubusercontent.com/39789077/233762546-b33eabb4-5c70-40a7-bb92-69e7095961b8.jpg" width="150"> <img src="https://user-images.githubusercontent.com/39789077/233762550-8f9faa5a-600e-4128-8c9f-e8611e7384e0.jpg" width="150"> <img src="https://user-images.githubusercontent.com/39789077/233762557-9787ceaf-3829-403d-a5da-009f196a373b.jpg" width="150"></br>
* **Menu** for Browsing Profile and Chat List.</br>
  <img src="https://user-images.githubusercontent.com/39789077/233762737-82dee744-1262-4021-b150-4fd3cdbbcdbf.jpg" width="150">


